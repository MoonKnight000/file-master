# CLAUDE.md — file-master backend

Spring Boot 4.1 / Kotlin / Gradle backend for **File Master** (convert & edit files).
Package root: `uz.murodjon.filemaster`. The Next.js front-end lives in a sibling repo
`file-master-front`; its `docs/API.md` is the **source of truth for the API contract** —
keep it in sync whenever an endpoint's request/response shape changes.

## API change log (REQUIRED every session that touches the API)

Whenever a chat changes the API (any endpoint's request/response shape, a new/removed
endpoint, enum or DTO change), record it in the front-end repo:
- Write ONE markdown file **per chat session** (NOT one per individual change) into
  `file-master-front/docs/api/`.
- Name it `YYYY-MM-DD-<short-slug>.md` (e.g. `2026-06-21-tools-list-full-tooldef.md`).
- Accumulate ALL of that session's API changes into that single file; if you already
  created the session's file earlier in the same chat, APPEND to it — do not make a new one.
- Each entry: the endpoint(s) affected, before → after, and why. This is the human-readable
  changelog; `docs/API.md` itself still gets updated in place as the canonical contract.
- `file-master-front/docs/API-QOLLANMA.md` is the plain-language (Uzbek) endpoint guide —
  update its affected section too when an endpoint's purpose/shape changes.

## Build & run (READ FIRST)

- **JDK gotcha:** Gradle defaults to a JVM it rejects (15). Always build/run with the
  Microsoft JDK 17 toolchain:
  ```bash
  export JAVA_HOME="/c/Users/Surface PC/.jdks/ms-17.0.18"
  ./gradlew compileKotlin        # quick type-check
  ./gradlew compileTestKotlin assemble   # full build
  ./gradlew bootRun              # start the app
  ```
- **Server port: 7788.** `GET /v1/tools/**`, `POST /v1/auth/**` (incl. `/refresh`), swagger
  and `/actuator/health` are public; everything else needs `Authorization: Bearer <token>`
  (opaque ACCESS token, 60 min; renew via `POST /v1/auth/refresh` with the 30-day rotating
  refresh token; DB stores only SHA-256 hashes — see `auth/model/Session`).
- Local **Postgres** (`filemaster` db, `ddl-auto=update`) must be running; MinIO is remote.
  On startup `ToolSeeder` upserts the tool catalog (log: "Tool catalog seeded: N …").
- A stale app holding port 7788 (e.g. an IntelliJ run) makes a new `bootRun` silently
  fail while requests hit the OLD build — stop it or use `--args=--server.port=7799`.

## Architecture rules (STRICT — the user rejected the alternatives)

The full architecture spec lives in `docs/ARCHITECTURE.md` — keep it in sync. Summary:

1. **Per-feature folder layout:** each module has `controller/`, `service/` (+
   `service/impl/`), `model/`, `repository/`, `dto/`, `enums/`, plus feature-specific
   extras (`engine/`, `processor/`, `schedule/`, `security/`). The document-tools module
   is `document/` (matches ToolGroup), NOT `pdf/`.
2. **Controllers = `interface XxxController` + `class XxxControllerImpl`, BOTH directly in
   `controller/`** (no `controller/impl/`). Mapping annotations (`@RequestMapping("/v1/...")`,
   `@GetMapping`, `@RequestBody`, `@RequestParam`, `@PathVariable`, `@CurrentUser`) live on
   the **interface**. The impl is a bare `@RestController` (NO `@RequestMapping`) and its
   overrides carry NO annotations — Spring 6+ merges them from the interface. Controllers
   are thin and only delegate.
3. **Services = `interface XxxService` in `service/` + `class XxxServiceImpl`.** The impl
   goes in `service/impl/` ONLY when the service package has 2+ interfaces; with a single
   interface the impl sits directly in `service/` (no `impl/` folder) — e.g.
   `mail/service/LogMailService`, `storage/MinioStorageService`. ALL business logic lives
   in the service, never the controller.
4. **Every endpoint returns `ResponseEntity<ResponseData<T>>`** (the `util/ResponseData`
   success envelope). All paths start with `/v1/` (never `/api/`).
5. **One class per file (STRICT):** every top-level class/interface/enum/data class in its
   own file named after it. Only exception: the `exception` package may group concrete
   exception classes (`Exceptions.kt`). Top-level mapper funcs (`ToolMappers.kt`) are fine.

## Exceptions

Do NOT use one generic exception with factory methods. Instead:
- `enum class ExcCode(val code: Int, val status: HttpStatus)` — one entry per error kind
  (the enum name is the string the front-end sees).
- `abstract class BaseException(message) : RuntimeException(message)` with
  `abstract val code: ExcCode`, `open val status get() = code.status`, `open val details`.
- One concrete class per case, e.g. `class ToolNotFoundException(...) : BaseException(...)`.
- All in `uz.murodjon.filemaster.exception`; `GlobalExceptionHandler` maps `BaseException`
  → `{error:{code,message,details}}` (matches `docs/API.md`).

## Entities

Long `IDENTITY` ids, epoch-second `Long` timestamps (`createdTimestamp`/`updatedTimestamp`/
`deletedTimestamp`), soft-delete via an `active` boolean. Repositories filter on
`...ActiveTrue...`. `StoredFile` (table `files`) is the single unified file entity
(uploads + results, discriminated by `FileSource`) and holds ONLY pure file metadata.

## Enums & DTOs

- Domain enums carry their wire value via `@get:JsonValue` (`CategoryToken.token`,
  `ToolKind.value`, `ToolBadge.value`, `FileFormat.value`). `ToolEngine` has none, so it
  serializes by enum name.
- **Do NOT hand-flatten enums to `String` in DTOs** (no `category.token` / `kind.value` in
  mapper code). Expose the enum field directly and let Jackson serialize via `@JsonValue`.
  The `/v1/tools` list returns full `ToolDef`s (enums intact), not a stringified summary.

## Tool catalog is DB-backed

The catalog lives in the `tools` table, not in code:
- `tools/model/Tool` (@Entity) ← `tools/repository/ToolRepository` (Spring Data).
- `tools/ToolSeeder` holds the canonical `catalog: List<ToolDef>` (mirrors the front-end's
  `src/data/tools.ts`) and **upserts by slug on every startup** — edit a def there to change
  the catalog. To add/change a tool, edit the seeder, not random reads.
- Read tools ONLY through `tools/service/ToolProvider` (maps `Tool` rows → in-memory
  `ToolDef` so callers — incl. the @Async `ConversionWorkerImpl` — never touch JPA entities).
  `ToolCatalog` holds only the shared option vocabularies (quality/audio/video/image), not
  per-tool data.

## Conversion

Engines in `conversion/engine/` selected by `ToolEngine`: LibreOffice (docs), ffmpeg
(audio/video/convert-image), pure-Java ImageIO (compress-image), Ghostscript (compress-pdf),
Tesseract (ocr), PDFBox (merge-pdf), java.util.zip (unzip), in-JVM ONNX Runtime + u2net
(remove-background), whisper.cpp CLI (audio-to-text). External tool paths are ABSOLUTE
in `application.properties` (the app inherits a stale PATH). AI model files live OUTSIDE
the repo in `C:/Users/Surface PC/file-master-models/` (u2net.onnx, ggml-small.bin,
whisper/Release/whisper-cli.exe) — paths in `app.tools.*`. Conversion runs async via
`ConversionWorker`, progress streamed over SSE.
