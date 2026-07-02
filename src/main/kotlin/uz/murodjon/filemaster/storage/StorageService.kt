package uz.murodjon.filemaster.storage

import java.io.InputStream
import java.nio.file.Path

/**
 * Object storage abstraction. Keys are app-defined strings foldered by format,
 * such as `documents/<uuid>/annual-report.pdf` or `image/<uuid>/photo.jpg`.
 */
interface StorageService {
    fun putStream(key: String, stream: InputStream, size: Long, contentType: String?)
    fun putFile(key: String, file: Path, contentType: String?)
    fun get(key: String): InputStream
    /** Downloads an object to a local file (for tools that need a real path). */
    fun download(key: String, dest: Path)
    fun delete(key: String)
}
