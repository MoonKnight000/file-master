# File Master — Funksional Rejalar

> **Maqsad (ikki narsa):**
> 1. **Retention** — foydalanuvchi bir marta kirsa, yana qaytib kelsin
> 2. **SEO** — Google'da "PDF convert online", "compress video online" kabi so'rovlarda chiqsin
>
> **Release sanasi: 7-iyul 2026** (bugun 27-iyun — 10 kun qoldi)
> Deploy/infra bu yerda emas, alohida `PLANS.md`'da.

---

## 0. Hozirgi holat (27-iyun)

### ✅ Backend tayyor
| Modul | Holat |
|-------|-------|
| 12 ta tool (audio/video/image/pdf/archive) | ✅ ishlaydi |
| Auth (guest, register, login, Google) | ✅ |
| File upload/download/delete/list/patch | ✅ |
| Conversion jobs (SSE progress, zip download) | ✅ |
| `GET /v1/me/jobs` — job history | ✅ (bu sessiyada qo'shildi) |
| `POST /v1/files/{id}/share` + `GET /v1/share/{token}` | ✅ (bu sessiyada) |
| `GET /v1/me/tools` + `POST /v1/me/tools/{slug}/favorite` | ✅ (bu sessiyada) |
| `GET /v1/tools/suggest?mime=` | ✅ (bu sessiyada) |
| Search (`GET /v1/search`) | ✅ |
| Billing stub | ✅ |

### ⏳ Frontend qilinishi kerak (10 kun)
Quyidagi barcha bo'limlarda belgilanadi.

---

## 1. RETENTION — Foydalanuvchi qayta kelishi

> Odamlar faqat "bir martacha" ishlatadigan tool emas, **hisobi bor platforma** deb bilishi kerak.

### 1A. Akkaunt qiymati (nima uchun ro'yxatdan o'tsin?)

| Funksiya | Backend | Frontend |
|----------|---------|----------|
| Job history sahifasi — o'tgan konversiyalar ro'yxati | ✅ `GET /v1/me/jobs` | ⏳ `/history` sahifasi |
| Natijalar saqlanadi (retention window) — "faylingiz 7 kun saqlanadi" | ✅ `RetentionService` | ⏳ Countdown badge faylda |
| "My files" sahifasi — barcha yuklangan/natija fayllar | ✅ `GET /v1/files` | ⏳ `/files` sahifasi |
| Favorites + Recent tools (sidebar'da) | ✅ `GET /v1/me/tools` | ⏳ Sidebar komponenti |
| Guest → Register: "faylingizni saqlash uchun ro'yxatdan o'ting" | ✅ guest→upgrade flow | ⏳ Prompt banner |
| Usage ko'rsatish — "10 dan 3 tasi ishlatildi" | ✅ `GET /v1/me` | ⏳ Progress bar `MeResponse` dan |

### 1B. UX yopishqoqligi (sifat = qayta kelish sababi)

| Funksiya | Backend | Frontend |
|----------|---------|----------|
| Drag & drop upload + clipboard paste | ✅ `POST /v1/files` | ⏳ DropZone komponenti |
| Real-time progress bar (SSE) | ✅ `/events` endpoint | ⏳ SSE connection + animatsiya |
| Natija preview — rasm/audio/video/PDF ko'rinishi | ✅ download endpoint | ⏳ Preview modal/panel |
| Share link — "do'stingga yubor" tugmasi | ✅ `POST /v1/files/{id}/share` | ⏳ Share button + copy link |
| Format auto-suggest — yuklangan faylga mos toollar | ✅ `GET /v1/tools/suggest?mime=` | ⏳ "Bu fayl bilan nimalar qilish mumkin?" |
| Tool chain — natijani keyingi toolga uzatish | ❌ backend kerak | ⏳ v1.1 ga qoldirildi |

### 1C. Free vs Pro farqi (monetizatsiya = retention sababi)

| Funksiya | Backend | Frontend |
|----------|---------|----------|
| Free: max 50MB, 5 fayl/kun, 3 kun saqlash | ✅ `LimitsProperties` | ⏳ Limitga yetganda `PLAN_LIMIT` xatosi UI |
| Pro: max 500MB, cheksiz, 30 kun saqlash | ✅ plan logikasi bor | ⏳ "Upgrade to Pro" CTA |
| Checkout (`POST /v1/billing/checkout`) | ✅ stub bor | ⏳ Pro to'lov sahifasi |

> **Aniq raqamlar qaror** (sizdan kerak): Free limiti qancha? Hozircha `application.properties`'da.

---

## 2. SEO — Google'da chiqish

> **Asosiy qoida:** Google tool sahifasini ko'rishi va indeksga olishi uchun har bir tool uchun
> o'z URL'i, to'liq meta tag'lar va tez yuklanadigan sahifa bo'lishi shart.

### 2A. Texnik SEO (bular bo'lmasa Google ko'rmaydi)

| Narsa | Kim qiladi | Holat |
|-------|-----------|-------|
| `robots.txt` — crawl ruxsati | Frontend (static fayl) | ⏳ |
| `sitemap.xml` — barcha tool URL'lari | Frontend (Next.js `sitemap.ts`) | ⏳ |
| Canonical URL har sahifada | Frontend | ⏳ |
| HTTPS (Google HTTPS saytlarni afzal ko'radi) | Deploy/infra | PLANS.md da |
| Mobile-responsive dizayn | Frontend | ⏳ |
| Core Web Vitals: LCP < 2.5s, CLS < 0.1 | Frontend (Next.js SSG) | ⏳ |

### 2B. Tool sahifalari — eng muhim SEO ishi

Har bir tool uchun `/tools/[slug]` sahifasi bo'lishi kerak. Masalan:

```
/tools/convert-audio
Title:       "Audio Konvertatsiya — MP3, WAV, FLAC | File Master"
Description: "Online ravishda audio fayllarni bepul konvert qiling. MP3, WAV,
              FLAC, AAC, OGG formatlarini qo'llab-quvvatlaydi."
og:image:    /og/convert-audio.png (har tool uchun alohida karta)
```

| SEO elementi | Kim qiladi | Holat |
|-------------|-----------|-------|
| `/tools/[slug]` — har tool sahifasi | Frontend (`GET /v1/tools/{slug}` dan data) | ⏳ |
| `/tools/pdf`, `/tools/audio` — kategoriya sahifalari | Frontend | ⏳ |
| Har sahifada `<title>`, `<meta description>` | Frontend | ⏳ |
| `og:image` (social preview karta) | Frontend (statik yoki Edge) | ⏳ |
| `hreflang` (agar ko'p tilli bo'lsa) | — | v1.2 |

### 2C. Structured Data (Schema.org) — Google rich result uchun

```json
{
  "@type": "WebApplication",
  "name": "File Master — Audio Converter",
  "url": "https://filemaster.uz/tools/convert-audio",
  "applicationCategory": "MultimediaApplication",
  "operatingSystem": "Web",
  "offers": { "@type": "Offer", "price": "0" }
}
```

| Schema turi | Sahifalar | Holat |
|------------|----------|-------|
| `WebApplication` | Har tool sahifasi | ⏳ |
| `SoftwareApplication` | Bosh sahifa | ⏳ |
| `BreadcrumbList` | Navigatsiya | ⏳ |
| `HowTo` ("MP3'ga qanday o'tkazish") | Tool sahifasi (opsional) | v1.1 |

### 2D. Kontent SEO (uzuq muddatda muhim)

Google faqat texnik belgilar emas, **kontent**ga ham qaraydi:

| Kontent | Qayerda | Holat |
|---------|---------|-------|
| Tool tavsifi (150–200 so'z) hozir `desc` — qisqa | ToolSeeder | ✅ (ammo kengaytirish kerak) |
| "Nima uchun file-master?" bloki bosh sahifada | Frontend | ⏳ |
| FAQ qismi har tool sahifasida | Frontend (statik) | v1.1 |
| Blog (opsional, uzoq muddatda katta kuch) | — | v1.2 |

---

## 3. Yangi toollar (7-iyulga)

> **Strategiya:** ko'p tool = ko'p SEO sahifa = ko'p organik traffic.
> Har yangi tool = yangi `/tools/[slug]` sahifa = yangi Google indeks.

### P0 — albatta (backend + frontend birga)

| Tool | Engine | Backend | Frontend |
|------|--------|---------|----------|
| `pdf-to-images` | PDF_RASTER (PDFBox) | ✅ `PdfRasterizer` + `DocumentProcessor` | ⏳ |
| `images-to-pdf` | IMAGES_TO_PDF (PDFBox) | ✅ `ImagesToPdfConverter` + `DocumentProcessor` | ⏳ |
| `zip-files` | ZIP_CREATE | ✅ `ZipArchiver` + `ArchiveProcessor` | ⏳ |

### P1 — vaqt bo'lsa (7-iyulga)

| Tool | Engine | Backend | Frontend |
|------|--------|---------|----------|
| `compress-video` | FFMPEG | ✅ `FfmpegConverter` + `VideoProcessor` | ⏳ |
| `video-to-audio` | FFMPEG | ✅ `FfmpegConverter` + `AudioProcessor` | ⏳ |
| `resize-image` | IMAGE | ✅ `ImageConverter` + `ImageProcessor` | ⏳ |

### P2 — release'dan keyin (v1.1)

`split-pdf`, `rotate-pdf`, `rotate-image`, `trim-audio`, `trim-video`, `video-to-gif`,
`merge-audio`, `crop-image`, `watermark-pdf`, `protect-pdf`

---

## 4. "Edit rejimi" — v1.1

Interaktiv parametr talab qiluvchi toollar (trim, crop, rotate, split):
- Frontend: slider, crop-box, sahifa selektori
- Backend: `ToolKind.EDIT`, maxsus options DTO
- **7-iyulga emas** — v1.1 da to'liq qilinadi

---

## 5. 7-iyul MVP — nima kiradi (10 kun)

### Backend ✅ to'liq tayyor
- [x] `pdf-to-images` processor — `PdfRasterizer` + `DocumentProcessor`
- [x] `images-to-pdf` processor — `ImagesToPdfConverter` + `DocumentProcessor`
- [x] `zip-files` processor — `ZipArchiver` + `ArchiveProcessor`

### Frontend (asosiy ish — 10 kun)
**SEO (dastlab, chunki Google indekslash vaqt oladi):**
- [ ] `robots.txt` + `sitemap.xml`
- [ ] Har tool sahifasi `/tools/[slug]` — `<title>`, `<meta description>`, `og:image`
- [ ] Schema.org `WebApplication` har tool sahifasida
- [ ] Kategoriya sahifalari `/tools/pdf`, `/tools/audio`, ...

**Core UX:**
- [ ] Drag & drop upload + clipboard paste
- [ ] SSE progress bar (real-time)
- [ ] Natija preview (image/audio/video)
- [ ] "Yuklab olish" + "Ulashish" tugmalari natijada

**Retention UX:**
- [ ] Sidebar: Favorites + Recent tools (`GET /v1/me/tools`)
- [ ] `/history` sahifasi (`GET /v1/me/jobs`)
- [ ] `/files` sahifasi (`GET /v1/files`)
- [ ] Guest → Register prompt ("fayllaringizni saqlang")
- [ ] Free/Pro limit xatosi UI ("Upgrade to Pro" CTA)
- [ ] Fayl countdown timer (retention window)

**P0 yangi toollar UI:**
- [ ] `pdf-to-images`, `images-to-pdf`, `zip-files` uchun formalar

---

## 6. Tavsiya etilgan tartib (10 kun)

```
1-2-kun  →  SEO texnik asosi: robots.txt, sitemap, tool sahifalar meta tag
             (Google indekslash vaqt oladi — BOSHIDA qiling!)
3-4-kun  →  Core UX: drag&drop, SSE progress, preview, download
5-6-kun  →  P0 backend + frontend: pdf-to-images, images-to-pdf, zip-files
7-8-kun  →  Retention: history, my-files, favorites, share link UI
9-kun    →  Free/Pro limit UI, Guest→Register prompt
10-kun   →  Mobile check, xato holatlari, yakuniy test
```

> **Nima uchun SEO birinchi?**
> Google yangi sahifani indekslash uchun 3–7 kun vaqt oladi.
> Agar release kuni `robots.txt`/`sitemap` bo'lmasa, hech kim Google'dan kelmaydi.
> Hoziroq deploy qilinsa, release kunga Google allaqachon indekslab bo'lgan bo'ladi.

---

## 7. Relizdan keyin — v1.1 (iyul oxiri)

| Narsa | Sabab |
|-------|-------|
| Edit rejimi (trim/crop/split/rotate) | Foydalanuvchi uchrashadigan katta cheklov |
| FAQ va tool tavsifi kengroq | Kontent SEO uchun |
| Blog (2-3 maqola) | Long-tail SEO traffic |
| Tool chain (convert → compress) | Retention + UX yaxshilash |
| `HowTo` schema.org | Google rich snippet |
| P1/P2 yangi toollar | Ko'proq SEO sahifa |
| Email notification | Retention: "konversiyangiz tayyor" |

---

## 8. Ochiq savollar (sizdan qaror kerak)

| Savol | Qiymat |
|-------|--------|
| Free plan: max fayl hajmi? | ? MB (hozir `application.properties`'da) |
| Free plan: kunlik limit? | ? ta konversiya |
| Free plan: fayl saqlanish muddati? | ? kun |
| Pro plan: narxi? | ? $/oy |
| Sayt tili? | O'zbek / Rus / Ingliz (SEO uchun muhim) |
| Domen? | filemaster.uz yoki boshqa? |
