package uz.murodjon.filemaster

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class FileMasterApplication

fun main(args: Array<String>) {
    runApplication<FileMasterApplication>(*args)
}
