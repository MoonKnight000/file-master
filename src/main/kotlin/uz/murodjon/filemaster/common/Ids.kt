package uz.murodjon.filemaster.common

import java.util.UUID

/** Helpers for opaque tokens and storage object naming (entity ids are DB-generated Longs). */
object Ids {
    fun token(): String =
        UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "")

    /** Stored object leaf name — the file is named by its UUID, never the original name. */
    fun objectName(uuid: String, ext: String): String =
        if (ext.isBlank()) uuid else "$uuid.$ext"

    /** Full storage key, foldered by format: e.g. "documents/<uuid>.pdf". */
    fun absolutePath(folder: String, uuid: String, ext: String): String =
        "$folder/${objectName(uuid, ext)}"
}
