# File Master backend — arxitektura spetsifikatsiyasi

Bu hujjat kod bazasining YAGONA qonuni. Yangi modul/fayl qo'shishdan oldin shu yerga
qarang; bu qoidalardan chetga chiqqan kod PR bosqichida qaytariladi.
(`CLAUDE.md` shu hujjatning qisqartmasini ko'chirib yuradi — ikkalasini sinxron saqlang.)

## 1. Modul (feature) tuzilishi

Har bir biznes-modul `uz.murodjon.filemaster.<modul>` paketi ostida quyidagicha:

```
<modul>/
├── controller/
│   ├── XxxController.kt        ← interface: BARCHA mapping annotatsiyalari shu yerda
│   └── XxxControllerImpl.kt    ← @RestController, annotatsiyasiz overridelar, faqat delegatsiya
├── service/
│   ├── XxxService.kt           ← interface (kontrakt)
│   └── impl/
│       └── XxxServiceImpl.kt   ← @Service, BUTUN biznes-logika shu yerda
├── dto/                        ← request/response recordlar (bitta fayl = bitta class)
├── model/                      ← @Entity lar
├── repository/                 ← Spring Data interfacelar (+ Specification objectlar)
└── enums/                      ← modulga xos enumlar
```

Modulga xos qo'shimcha paketlarga ruxsat bor (mavjudlari):
- `conversion/engine/` — tashqi vositalar (ffmpeg, LibreOffice, whisper.cpp, ...) va
  in-JVM modellar (ONNX u2net — remove-background) bilan ishlaydigan converterlar
- `conversion/processor/` — kategoriya-processorlar + `ConversionSupport`
- `files/schedule/`, `billing/schedule/`, `conversion/schedule/`, `auth/schedule/` — @Scheduled joblar
- `auth/security/` — auth domeniga oid web-glue (`@CurrentUser` resolver, cookie helper, 401/403 handlerlar)
- `<modul>/config/` — o'sha feature'ning `*Properties` data classi (8-qoidaga qarang)

### Modullar ro'yxati va vazifasi

| Modul | Vazifa |
|---|---|
| `auth` | sessiya (access 60min + rotating refresh 30 kun, DB'da faqat SHA-256 hash), ro'yxat/login/Google, `/v1/me`, profil, `SessionCleanupSchedule` |
| `files` | yagona fayl entity (`StoredFile`), upload, "My files", retention |
| `conversion` | jobs, worker, SSE, enginelar — kategoriya-agnostik yadro |
| `audio` / `video` / `image` / `document` / `archive` | kategoriya-endpointlar (`/v1/{group}/conversions` va `/v1/{group}/tools` — `XxxToolService` ham shu modulda), option-validatsiya, per-kategoriya job subclasslari. **Hujjat moduli `document`, `pdf` EMAS** |
| `tools` | DB-backed tool katalogi, seeder, SEO kontent (`ToolSeo`) |
| `share` | ommaviy share-linklar (create/info/download/revoke) |
| `search` | header qidiruv (toollar + fayllar) |
| `billing` | checkout (PaymentProvider), webhook, plan muddati |
| `mail` | tashqi email (hozircha log-stub) |
| `storage` | `StorageService` interface + `impl/MinioStorageService` |
| `security` | HTTP security zanjiri (`SecurityConfig`, token filter) |
| `exception` | `ExcCode`, `BaseException`, konkret exceptionlar, global handler |
| `config` | root `AppProperties` + kesishuvchi `LimitsProperties` + Spring `*Config`lar (feature Properties'lar o'z modulida) |
| `common` / `util` / `pageable` | domen-umumiy enumlar / javob konvertlari / sahifalash |

## 2. Qat'iy qoidalar

1. **Bitta fayl = bitta top-level deklaratsiya.** Istisnolar: `exception/Exceptions.kt`
   (konkret exceptionlar) va top-level mapper funksiyalar (`ToolMappers.kt`).
2. **Controller interface'ida** mapping/param annotatsiyalari; **Impl'da hech qanday
   annotatsiya yo'q** (faqat `@RestController`). Impl faqat service'ga delegatsiya qiladi —
   if/else ham yozilmaydi.
3. **Service interface `service/`da, Impl `service/impl/`da** — LEKIN service package'da
   FAQAT BITTA interfeys bo'lsa, `impl/` ochilmaydi: Impl ham `service/`ning o'zida turadi
   (masalan `mail/service/LogMailService`, `search/service/SearchServiceImpl`,
   `storage/MinioStorageService`). Ikkinchi interfeys paydo bo'lgach Impl'lar `impl/`ga
   tushadi. Controller Impl esa har doim `controller/`ning o'zida (alohida `impl/` YO'Q).
4. **Har bir JSON endpoint** `ResponseEntity<ResponseData<T>>` qaytaradi; barcha yo'llar
   `/v1/` bilan boshlanadi. Binary/SSE bundan mustasno.
5. **Enumlarni DTOda `String`ga qo'lda yoymang** — enum maydonni to'g'ridan-to'g'ri
   qo'ying, Jackson `@get:JsonValue` orqali o'zi yozadi (`CategoryToken.token`,
   `ToolKind.value`, `FileFormat.value`, `FileSource.value`, ...).
6. **Entity konventsiyasi:** `Long IDENTITY` id, epoch-sekund `Long` timestamplar
   (`createdTimestamp`/`updatedTimestamp`/`deletedTimestamp`), soft-delete `active`
   boolean bilan; repositorylar `...ActiveTrue...` filtrlaydi.
7. **Exception pattern:** har xato turi uchun alohida class + `ExcCode` yozuvi.
   Factory-metodli yagona exception TAQIQLANGAN.
8. **Konfiguratsiya:** yangi sozlama = tegishli `*Properties` data class maydoni +
   `application.properties`da `app.*` kaliti (env override bilan). Feature'ga tegishli
   `*Properties` o'z modulining `config/` subpackage'ida turadi
   (`billing/config/BillingProperties`, `auth/config/GoogleProperties`,
   `storage/config/StorageProperties`, `security/config/CorsProperties`,
   `conversion/config/ToolsProperties`); top-level `config/`da faqat root `AppProperties`,
   kesishuvchi `LimitsProperties` va `*Config` (bean wiring) klasslar qoladi.

## 3. Qatlamlar orasidagi oqim

```
HTTP → Controller(interface) → ControllerImpl → Service(interface) → ServiceImpl
                                                     ↓
                             Repository / Storage / boshqa Service interfacelar
```

- ControllerImpl HECH QACHON repositoryga tegmaydi.
- ServiceImpl boshqa modulning ServiceImpl'iga emas, **interface**'iga bog'lanadi.
- `@Async` worker (`ConversionWorkerImpl`) JPA entitylarni threadlar orasida
  ko'chirmaydi — tool ma'lumotini `ToolProvider` (in-memory `ToolDef`) orqali oladi.

## 4. Kesishuvchi tartiblar

- **Kvota/limitlar:** yagona nuqta — `ConversionServiceImpl.submit` (kunlik kvota) va
  `FilesServiceImpl.upload` (hajm). Yangi kategoriya qo'shsangiz avtomatik qamrab olinadi,
  chunki hammasi `ConversionService.submit`dan o'tadi.
- **Retention:** plan-ga bog'liq (`LimitsProperties.retentionMinutesFor` — natijalar,
  `uploadRetentionMinutesFor` — uploadlar); tozalash `files/schedule/RetentionSchedule`
  (ishlayotgan jobga bog'langan upload o'chirilmaydi), plan muddati
  `billing/schedule/PlanExpirySchedule`. Zombie/stale joblar (`server restart yoki
  stale-job-minutes`dan oshgan) `conversion/schedule/StaleJobSchedule`da FAILED qilinadi,
  eski scratch-dirlar ham o'sha yerda startupda tozalanadi.
- **Email:** faqat `mail/service/MailService` orqali; chaqiruvlar `runCatching` bilan
  o'ralgan — xat yuborilmasa job yiqilmaydi.
- **API o'zgarsa:** `file-master-front/docs/API.md`ni yangilang + o'sha sessiyaning
  `docs/api/YYYY-MM-DD-*.md` changelog fayliga yozing (CLAUDE.md'dagi tartib).
