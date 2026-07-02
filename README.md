# File Master — Backend

**File Master** — fayllarni onlayn konvertatsiya va tahrirlash platformasi. Bu repo uning
Spring Boot backend qismi; Next.js front-end alohida repoda (`file-master-front`) turadi.

> API kontraktining manbasi: `../file-master-front/docs/API.md`. Endpoint shakli o'zgarsa,
> o'sha fayl ham yangilanadi (qarang: `CLAUDE.md` dagi "API change log" qoidasi).

## Stack

| Qatlam | Texnologiya |
|--------|-------------|
| Til / Framework | Kotlin, Spring Boot 4.1 (Java 17 toolchain) |
| Ma'lumotlar bazasi | PostgreSQL (`filemaster`) |
| Fayl saqlash | MinIO (S3-ga mos, remote) |
| Hujjatlar | LibreOffice (`soffice`) |
| Media | ffmpeg |
| PDF siqish | Ghostscript |
| OCR | Tesseract |
| PDF tahrirlash | Apache PDFBox (+ BouncyCastle — parol shifrlash) |
| DjVu | DjVuLibre (`ddjvu`) |

Tashqi tool yo'llari `application.properties` da **absolyut** ko'rsatilgan (ilova eski PATH
meros qilib olishi mumkin).

## Ishga tushirish

```bash
# MUHIM: Gradle standart JVM ni rad etadi — Microsoft JDK 17 bilan ishlating
export JAVA_HOME="/c/Users/Surface PC/.jdks/ms-17.0.18"

./gradlew compileKotlin              # tez tip-tekshiruv
./gradlew compileTestKotlin assemble # to'liq build
./gradlew bootRun                    # ilovani ishga tushirish (port 7788)
```

Talablar:
- Lokal **PostgreSQL** ishlab turishi kerak (`filemaster` bazasi). MinIO remote.
- Startupda `ToolSeeder` katalogni bazaga yozadi — logda `Tool catalog seeded: N ...` chiqadi.
- Port 7788 ni eski jarayon band qilib turgan bo'lsa (masalan IntelliJ run), yangi `bootRun`
  jimgina ishlamay qoladi — eskisini to'xtating yoki `--args=--server.port=7799` ishlating.

**Ochiq endpointlar:** `GET /v1/tools/**`, `POST /v1/auth/session`, swagger, `/actuator/health`.
Qolgan hammasi `Authorization: Bearer <token>` talab qiladi.

## Arxitektura (qisqacha)

- **Per-feature modul:** har modulda `controller/`, `service/`, `model/`, `repository/`,
  `dto/`, `enums/`. Controller = `interface` (mapping annotatsiyalari shu yerda) + yalang'och
  `@RestController` impl. Biznes-logika faqat service da.
- **Javob konverti:** har endpoint `ResponseEntity<ResponseData<T>>` qaytaradi; barcha yo'llar
  `/v1/` bilan boshlanadi.
- **Tool katalogi bazada:** `tools` jadvali. Kanonik ro'yxat `tools/ToolSeeder.kt` da — har
  startupda slug bo'yicha upsert qilinadi. O'qish faqat `ToolProvider` orqali.
- **Konversiya oqimi:** kategoriya endpointi (`POST /v1/{audio|video|image|document|archive}/conversions`)
  → validatsiya → `ConversionJob` (kategoriya subclass) → `@Async ConversionWorker` →
  `ToolGroup` bo'yicha processor → `ToolEngine`/`EditOperation` bo'yicha engine.
  Progress SSE orqali: `GET /v1/conversions/{jobId}/events`.
- **Fayllar:** `StoredFile` (jadval `files`) — yuklangan va natija fayllar bitta entityda
  (`FileSource` bilan farqlanadi), MinIO da saqlanadi.

## Tool katalogi (50 ta)

### PDF (14)

| Slug | Nomi | Turi | Izoh |
|------|------|------|------|
| pdf-to-word | PDF to Word | convert | PDF → DOCX/DOC/TXT/ODT |
| compress-pdf | Compress PDF | compress | Ghostscript siqish |
| merge-pdf | Merge PDF | merge | Bir nechta PDF → bitta |
| pdf-to-images | PDF to Images | convert | Har sahifa → JPG/PNG |
| images-to-pdf | Images to PDF | merge | Rasmlar → bitta PDF |
| rotate-pdf | Rotate PDF | edit | 90/180/270° burish |
| split-pdf | Split PDF | edit | Sahifa/diapazonlarga bo'lish |
| delete-pdf-pages 🆕 | Delete PDF Pages | edit | Tanlangan sahifalarni o'chirish (`"2,5-7"`) |
| extract-pdf-pages 🆕 | Extract PDF Pages | edit | Tanlangan sahifalarni yangi PDF ga ajratish |
| reorder-pdf-pages 🆕 | Reorder PDF Pages | edit | Sahifa tartibini o'zgartirish (`"3,1,2"`) |
| watermark-pdf 🆕 | Watermark PDF | edit | Matnli watermark (pozitsiya, shaffoflik, o'lcham) |
| page-numbers-pdf 🆕 | Page Numbers | edit | Sahifa raqamlarini qo'shish |
| protect-pdf 🆕 | Protect PDF | edit | Parol qo'yish (256-bit AES) |
| unlock-pdf 🆕 | Unlock PDF | edit | Parolni olib tashlash |

### Hujjat / Jadval / Slayd (5)

| Slug | Nomi | Izoh |
|------|------|------|
| djvu-to-pdf | DjVu to PDF | DjVu skanlar → PDF |
| word-to-pdf | Word to PDF | DOC/DOCX/ODT/RTF/TXT → PDF |
| excel-to-pdf | Excel to PDF | XLS/XLSX/CSV/ODS → PDF |
| ppt-to-pdf | PPT to PDF | PPT/PPTX/ODP → PDF |
| ocr-scan | OCR Scan | Rasmdan matn (eng/rus/uzb) |

### Rasm (9)

| Slug | Nomi | Turi | Izoh |
|------|------|------|------|
| compress-image | Compress Image | compress | JPG/PNG siqish (target hajmgacha ham) |
| convert-image | Convert Image | convert | JPG/PNG/WEBP/HEIC va boshqalar |
| resize-image | Resize Image | edit | O'lchamni px da o'zgartirish |
| rotate-image | Rotate Image | edit | 90/180/270° burish |
| crop-image 🆕 | Crop Image | edit | To'rtburchak kesish (x, y, width, height) |
| flip-image 🆕 | Flip Image | edit | Gorizontal/vertikal ko'zgu |
| watermark-image 🆕 | Watermark Image | edit | Matnli watermark |
| image-filter 🆕 | Image Filters | edit | grayscale / sepia / invert / blur / sharpen |
| adjust-image 🆕 | Adjust Image | edit | Yorqinlik / kontrast / to'yinganlik (-100..100) |

### Audio (9)

| Slug | Nomi | Turi | Izoh |
|------|------|------|------|
| convert-audio | Convert Audio | convert | MP3/WAV/FLAC/AAC/M4A/OGG (+bitrate, volume, normalize) |
| video-to-audio | Video to Audio | convert | Videodan audio ajratish |
| trim-audio | Trim Audio | edit | Kesib olish (start/end) |
| merge-audio 🆕 | Merge Audio | merge | Bir nechta audio → bitta (concat) |
| speed-audio 🆕 | Change Audio Speed | edit | Tezlik 0.25x–4x (atempo) |
| volume-audio 🆕 | Change Volume | edit | Ovoz balandligi 0x–4x |
| fade-audio 🆕 | Fade Audio | edit | Fade-in / fade-out (soniyalarda) |
| reverse-audio 🆕 | Reverse Audio | edit | Teskari o'ynatish |
| normalize-audio 🆕 | Normalize Audio | edit | EBU loudnorm bilan tekislash |

### Video (11)

| Slug | Nomi | Turi | Izoh |
|------|------|------|------|
| convert-video | Convert Video | convert | MP4/MOV/WEBM/MKV (+codec, fps, resolution, mute) |
| compress-video | Compress Video | compress | Hajmni kichraytirish |
| trim-video | Trim Video | edit | Kesib olish (start/end) |
| crop-video 🆕 | Crop Video | edit | Kadrdan to'rtburchak kesish (x, y, width, height) |
| rotate-video 🆕 | Rotate Video | edit | 90/180/270° burish |
| flip-video 🆕 | Flip Video | edit | Gorizontal/vertikal ko'zgu |
| speed-video 🆕 | Change Video Speed | edit | Tezlik 0.25x–4x (video+audio birga) |
| mute-video 🆕 | Mute Video | edit | Audio trekni olib tashlash |
| watermark-video 🆕 | Watermark Video | edit | Matnli watermark (drawtext) |
| merge-videos 🆕 | Merge Videos | merge | Bir nechta video → bitta (birinchisining o'lchamiga moslanadi) |
| video-to-gif 🆕 | Video to GIF | convert | Animatsion GIF (palette bilan, sifatli ranglar) |

### Arxiv (2)

| Slug | Nomi | Izoh |
|------|------|------|
| unzip-files | Unzip files | ZIP ichini chiqarish |
| zip-files | Zip files | Fayllarni ZIP ga yig'ish |

🆕 = 2026-07-02 dagi edit to'lqinlarida qo'shilgan: Bosqich 1 — 12 ta PDF/rasm edit tool,
Bosqich 2 — 14 ta video/audio edit tool. Ikkala bosqich ham amalga oshirilgan va testdan
o'tgan.

## Yangi tool qo'shish (qisqa yo'riqnoma)

1. `tools/enums/EditOperation.kt` — yangi interaktiv forma bo'lsa, yangi qiymat
2. `tools/enums/ToolSlug.kt` + `tools/ToolSeeder.kt` — slug va `ToolDef`
3. `conversion/engine/ConversionSettings.kt` + kategoriya options DTO + job subclass
4. Engine metodi (`FfmpegConverter` / `ImageConverter` / `PdfEditor` / ...)
5. Processor `when` branchi (`DocumentProcessor` / `ImageProcessor` / ...)
6. `MediaValidator` qoidalari + `ToolsServiceImpl`/`EditOptions` surfacing

> Eslatma: `@Enumerated(STRING)` ustunlarga yangi enum qiymati qo'shilganda Postgres dagi
> eski `tools_<ustun>_check` constraintni tekshiring (`ddl-auto=update` rejimida uni drop
> qilish kerak bo'ladi; hozirgi dev rejim `create` — bu muammo yo'q, lekin data har
> restartda o'chadi).
