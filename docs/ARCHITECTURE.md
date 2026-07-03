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
- `conversion/engine/` — tashqi vositalar (ffmpeg, LibreOffice, ...) bilan ishlaydigan converterlar
- `conversion/processor/` — kategoriya-processorlar + `ConversionSupport`
- `files/schedule/`, `billing/schedule/` — @Scheduled joblar
- `auth/security/` — auth domeniga oid web-glue (`@CurrentUser` resolver, cookie helper, 401/403 handlerlar)

### Modullar ro'yxati va vazifasi

| Modul | Vazifa |
|---|---|
| `auth` | sessiya/ro'yxat/login/Google, `/v1/me`, profil |
| `files` | yagona fayl entity (`StoredFile`), upload, "My files", retention |
| `conversion` | jobs, worker, SSE, enginelar — kategoriya-agnostik yadro |
| `audio` / `video` / `image` / `document` / `archive` | kategoriya-endpointlar (`/v1/{group}/conversions`), option-validatsiya, per-kategoriya job subclasslari. **Hujjat moduli `document`, `pdf` EMAS** |
| `tools` | DB-backed tool katalogi, seeder, SEO kontent (`ToolSeo`) |
| `share` | ommaviy share-linklar (create/info/download/revoke) |
| `search` | header qidiruv (toollar + fayllar) |
| `billing` | checkout (PaymentProvider), webhook, plan muddati |
| `mail` | tashqi email (hozircha log-stub) |
| `storage` | `StorageService` interface + `impl/MinioStorageService` |
| `security` | HTTP security zanjiri (`SecurityConfig`, token filter) |
| `exception` | `ExcCode`, `BaseException`, konkret exceptionlar, global handler |
| `config` | `@ConfigurationProperties` klasslar + Spring configlar |
| `common` / `util` / `pageable` | domen-umumiy enumlar / javob konvertlari / sahifalash |

## 2. Qat'iy qoidalar

1. **Bitta fayl = bitta top-level deklaratsiya.** Istisnolar: `exception/Exceptions.kt`
   (konkret exceptionlar) va top-level mapper funksiyalar (`ToolMappers.kt`).
2. **Controller interface'ida** mapping/param annotatsiyalari; **Impl'da hech qanday
   annotatsiya yo'q** (faqat `@RestController`). Impl faqat service'ga delegatsiya qiladi —
   if/else ham yozilmaydi.
3. **Service interface `service/`da, Impl `service/impl/`da.** Controller Impl esa
   `controller/`ning o'zida (alohida `impl/` YO'Q — service bilan adashtirmang).
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
8. **Konfiguratsiya:** yangi sozlama = `config/`dagi tegishli `*Properties` data class
   maydoni + `application.properties`da `app.*` kaliti (env override bilan).

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
- **Retention:** plan-ga bog'liq (`LimitsProperties.retentionMinutesFor`); tozalash
  `files/schedule/RetentionSchedule`, plan muddati `billing/schedule/PlanExpirySchedule`.
- **Email:** faqat `mail/service/MailService` orqali; chaqiruvlar `runCatching` bilan
  o'ralgan — xat yuborilmasa job yiqilmaydi.
- **API o'zgarsa:** `file-master-front/docs/API.md`ni yangilang + o'sha sessiyaning
  `docs/api/YYYY-MM-DD-*.md` changelog fayliga yozing (CLAUDE.md'dagi tartib).
