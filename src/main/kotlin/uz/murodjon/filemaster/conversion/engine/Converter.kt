package uz.murodjon.filemaster.conversion.engine

import java.nio.file.Path

/** Converts a single input file to [outputFormat], returning the produced file. */
interface Converter {
    fun convert(input: Path, outputFormat: String, settings: ConversionSettings, workDir: Path): Path
}
