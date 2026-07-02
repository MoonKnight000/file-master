# File Master — Tool Installation Guide

External tools required to run the backend locally. All paths must match
`application.properties` (or be overridden via env vars).

---

## 1. JDK 17 (Microsoft Build)

Gradle toolchain is pinned to JDK 17. Using any other version fails the build.

### Windows
1. Download **Microsoft Build of OpenJDK 17** from
   https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.msi
2. Run the installer — it places the JDK at `C:\Users\<you>\.jdks\ms-17.0.x`.
3. Set `JAVA_HOME` before every Gradle run (or add it to System Environment Variables):
   ```powershell
   $env:JAVA_HOME = "C:\Users\Surface PC\.jdks\ms-17.0.18"
   .\gradlew bootRun
   ```

### Linux
```bash
# Ubuntu / Debian
wget https://packages.microsoft.com/config/ubuntu/22.04/packages-microsoft-prod.deb -O pkg.deb
sudo dpkg -i pkg.deb
sudo apt-get update
sudo apt-get install -y msopenjdk-17

export JAVA_HOME=/usr/lib/jvm/msopenjdk-17
./gradlew bootRun
```

---

## 2. PostgreSQL

Database name: `filemaster`, default user/password: `postgres` / `9654`
(override via `DB_URL`, `DB_USER`, `DB_PASSWORD` env vars).

### Windows
1. Download installer: https://www.postgresql.org/download/windows/
2. Install (default port 5432).
3. After install, open **pgAdmin** or `psql` and create the database:
   ```sql
   CREATE DATABASE filemaster;
   ```

### Linux
```bash
sudo apt-get install -y postgresql postgresql-contrib
sudo systemctl enable --now postgresql

sudo -u postgres psql -c "ALTER USER postgres PASSWORD '9654';"
sudo -u postgres psql -c "CREATE DATABASE filemaster;"
```

---

## 3. LibreOffice

Used for: Word → PDF, Excel → PDF, PowerPoint → PDF, and other document conversions.

Property key: `app.tools.soffice-path`

### Windows
1. Download: https://www.libreoffice.org/download/download/
2. Install (default path: `C:\Program Files\LibreOffice\`).
3. Verify:
   ```powershell
   & "C:\Program Files\LibreOffice\program\soffice.exe" --version
   ```

### Linux
```bash
sudo apt-get install -y libreoffice

# Verify
soffice --version

# Default binary path (update application.properties if different):
which soffice   # typically /usr/bin/soffice
```

> **Linux `application.properties` override:**
> ```properties
> app.tools.soffice-path=/usr/bin/soffice
> ```

---

## 4. FFmpeg

Used for: audio conversion (MP3/WAV/OGG/FLAC), video conversion (MP4/AVI/MKV/MOV),
and image-format conversion via ffmpeg.

Property key: `app.tools.ffmpeg-path`

### Windows
1. Download a build from https://www.gyan.dev/ffmpeg/builds/ (get the "release full" zip).
2. Extract to `C:\Program Files\ffmpeg\`.
3. Verify:
   ```powershell
   & "C:\Program Files\ffmpeg\bin\ffmpeg.exe" -version
   ```

### Linux
```bash
sudo apt-get install -y ffmpeg

# Verify
ffmpeg -version

# Path (update application.properties):
which ffmpeg    # typically /usr/bin/ffmpeg
```

> **Linux `application.properties` override:**
> ```properties
> app.tools.ffmpeg-path=/usr/bin/ffmpeg
> ```

---

## 5. Ghostscript

Used for: PDF compression (`compress-pdf` tool).

Property key: `app.tools.gs-path`

### Windows
1. Download: https://www.ghostscript.com/releases/gsdnld.html
   (choose the 64-bit installer, e.g. `gs10071w64.exe`)
2. Install — default path: `C:\Program Files\gs\gs10.07.1\bin\gswin64c.exe`
3. Verify:
   ```powershell
   & "C:\Program Files\gs\gs10.07.1\bin\gswin64c.exe" --version
   ```

### Linux
```bash
sudo apt-get install -y ghostscript

# Verify
gs --version

# Path (update application.properties):
which gs    # typically /usr/bin/gs
```

> **Linux `application.properties` override:**
> ```properties
> app.tools.gs-path=/usr/bin/gs
> ```

---

## 6. Tesseract OCR

Used for: `ocr-scan` tool (image/PDF → searchable text).

Property key: `app.tools.tesseract-path`

### Windows
1. Download installer from https://github.com/UB-Mannheim/tesseract/wiki
   (e.g. `tesseract-ocr-w64-setup-5.x.x.exe`)
2. Install — default path: `C:\Program Files\Tesseract-OCR\tesseract.exe`
3. To support multiple languages, during install select extra language packs
   (at minimum **English** and **Uzbek** if needed).
4. Verify:
   ```powershell
   & "C:\Program Files\Tesseract-OCR\tesseract.exe" --version
   ```

### Linux
```bash
sudo apt-get install -y tesseract-ocr
# Extra language packs (optional):
sudo apt-get install -y tesseract-ocr-eng tesseract-ocr-uzb

# Verify
tesseract --version

# Path (update application.properties):
which tesseract    # typically /usr/bin/tesseract
```

> **Linux `application.properties` override:**
> ```properties
> app.tools.tesseract-path=/usr/bin/tesseract
> ```

---

## 7. DjVuLibre

Used for: `djvu-to-pdf` conversion tool.

Property key: `app.tools.djvu-path`

### Windows
1. Download: https://sourceforge.net/projects/djvu/files/DjVuLibre_Windows/
2. Install — default path: `C:\Program Files (x86)\DjVuLibre\ddjvu.exe`
3. Verify:
   ```powershell
   & "C:\Program Files (x86)\DjVuLibre\ddjvu.exe" --help
   ```

### Linux
```bash
sudo apt-get install -y djvulibre-bin

# Verify
ddjvu --help

# Path (update application.properties):
which ddjvu    # typically /usr/bin/ddjvu
```

> **Linux `application.properties` override:**
> ```properties
> app.tools.djvu-path=/usr/bin/ddjvu
> ```

---

## 8. MinIO (object storage)

The default config points to a **remote MinIO** instance — no local install needed
during development. To run MinIO locally instead:

```bash
# Docker (both platforms)
docker compose up -d
# Console: http://localhost:9001  (minioadmin / minioadmin)
# S3 API:  http://localhost:9000
```

Then uncomment the local block in `application.properties`:
```properties
app.storage.endpoint=http://localhost:9000
app.storage.access-key=minioadmin
app.storage.secret-key=minioadmin
app.storage.bucket=file-master
```

---

## Summary table

| Tool         | Windows default path                                        | Linux path          | Env var override     |
|--------------|-------------------------------------------------------------|---------------------|----------------------|
| LibreOffice  | `C:/Program Files/LibreOffice/program/soffice.exe`          | `/usr/bin/soffice`  | `SOFFICE_PATH`       |
| FFmpeg       | `C:/Program Files/ffmpeg/bin/ffmpeg.exe`                    | `/usr/bin/ffmpeg`   | `FFMPEG_PATH`        |
| Ghostscript  | `C:/Program Files/gs/gs10.07.1/bin/gswin64c.exe`            | `/usr/bin/gs`       | `GS_PATH`            |
| Tesseract    | `C:/Program Files/Tesseract-OCR/tesseract.exe`              | `/usr/bin/tesseract`| `TESSERACT_PATH`     |
| DjVuLibre    | `C:/Program Files (x86)/DjVuLibre/ddjvu.exe`                | `/usr/bin/ddjvu`    | `DJVU_PATH`          |

All paths can be overridden with the corresponding env var so no code change is needed
when deploying to a different machine.
