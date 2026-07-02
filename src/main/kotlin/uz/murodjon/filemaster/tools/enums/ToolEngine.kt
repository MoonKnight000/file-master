package uz.murodjon.filemaster.tools.enums

/** Which engine performs a tool's conversion. */
enum class ToolEngine {
    LIBREOFFICE,
    FFMPEG,
    IMAGE,        // pure-Java ImageIO (compress/convert jpg/png)
    GHOSTSCRIPT,  // pdf compression (external `gs`/`gswin64c`)
    OCR,          // text extraction (external `tesseract`)
    PDF_MERGE,    // combine several PDFs into one (PDFBox)
    UNZIP,        // extract a .zip into its files (pure Java)
    PDF_RASTER,   // render each PDF page to an image (PDFBox)
    IMAGES_TO_PDF,// combine several images into one PDF (PDFBox)
    PDF_EDIT,     // rotate / split PDF pages (PDFBox)
    ZIP_CREATE,   // pack several files into one .zip (pure Java)
    DJVU,         // convert DjVu to PDF (external `ddjvu` from DjVuLibre)
    NONE,         // catalog-only / not wired yet
}
