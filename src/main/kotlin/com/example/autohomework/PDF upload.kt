package com.example.autohomework

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://localhost:3000"])
class PDF() {
    private val uploadDir = "uploads"

    init {
        // Create uploads directory if it doesn't exist
        File(uploadDir).mkdirs()
    }
    @PostMapping("/upload")
    fun DownloadPdf(@RequestParam("file") file: MultipartFile): MultipartFile {
        val filename = "${System.currentTimeMillis()}_${file.originalFilename}"
        val filepath = Paths.get(uploadDir, filename)

        // Save file
        Files.copy(file.inputStream, filepath, StandardCopyOption.REPLACE_EXISTING)

        return file
    }
}