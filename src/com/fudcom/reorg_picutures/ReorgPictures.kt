package com.fudcom.reorg_picutures

import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.YearMonth
import java.time.format.DateTimeFormatter

fun main() {

    val DATE_FORMAT_FILE = DateTimeFormatter.ofPattern( "yyyy-MM-dd" )
    val DATE_FORMAT_YEAR = DateTimeFormatter.ofPattern( "yyyy" )
    val DATE_FORMAT_MONTH = DateTimeFormatter.ofPattern( "MM - MMMM" )

    val outputDirectory = Path.of( "/run/media/kfud/My Passport/GEP" )
    val logFile = outputDirectory.resolve( "error.log" )

    Files.deleteIfExists( logFile )

    val uris = listOf(
        URI.create("jar:file:/run/media/kfud/My%20Passport/takeout-20200628T113832Z-001.zip"),
        URI.create("jar:file:/run/media/kfud/My%20Passport/takeout-20200628T113832Z-002.zip"),
        URI.create("jar:file:/run/media/kfud/My%20Passport/takeout-20200628T113832Z-003.zip"),
        URI.create("jar:file:/run/media/kfud/My%20Passport/takeout-20200627T065148Z-001.zip"),
        URI.create("jar:file:/run/media/kfud/My%20Passport/takeout-20200627T065148Z-002.zip"),
        URI.create("jar:file:/run/media/kfud/My%20Passport/takeout-20200627T065148Z-003.zip")
    )

    uris.map { FileSystems.newFileSystem(it, emptyMap<String,String>() ) }.forEach { fs ->
        try {
            Files.walk(fs.getPath("/")).forEach {
                if (Files.isRegularFile(it) && !it.toString().endsWith(".json" )) {
                    try {
                        val dateDir = it.parent.toString().substring(23, 33)
                        val date = YearMonth.from(DATE_FORMAT_FILE.parse(dateDir))
                        var newPath = outputDirectory
                            .resolve(date.format(DATE_FORMAT_YEAR))
                            .resolve(date.format(DATE_FORMAT_MONTH))
                            .resolve(it.fileName.toString())
                        println("Copying file: $fs$it -> $newPath")
                        Files.createDirectories(newPath.parent)
                        Files.copy(it, newPath)
                    } catch (e: Exception) {
                        println("ERROR: $fs$it -> ${e.toString()}")
                        Files.write( logFile, listOf( "$fs$it -> ${e.toString()}" ), StandardOpenOption.CREATE, StandardOpenOption.APPEND )
                    }
                }
            }
        } finally {
            fs.close()
        }
    }

}