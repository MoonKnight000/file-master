package uz.murodjon.filemaster.tools.dto

import uz.murodjon.filemaster.tools.model.Tool

/** Maps a persistent [Tool] row into the in-memory [ToolDef] the rest of the app works with. */
fun Tool.toDef(): ToolDef = ToolDef(
    slug = slug,
    title = title,
    desc = description,
    category = category,
    icon = icon,
    badge = badge,
    accept = accept.toList(),
    outputFormats = outputFormats.toList(),
    defaultFormat = defaultFormat,
    engine = engine,
    kind = kind,
    keepOriginalDefault = keepOriginalDefault,
    mergeIntoOneDefault = mergeIntoOneDefault,
    editOperation = editOperation,
)
