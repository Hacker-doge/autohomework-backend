package com.example.autohomework

import com.slack.api.model.Conversation
import io.github.cdimascio.dotenv.dotenv
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
class PDF {
    private val uploadDir = "uploads"

    val dotenv = dotenv()
    private val openAiTokem = dotenv["OPENAI_TOKEM"].toString()
    private val slackBotTokem = dotenv["SLACK_TOKEM"].toString()
    private val slackChannelId =dotenv["CHANNEL"].toString()

    init {
        File(uploadDir).mkdirs()
    }

    @PostMapping("/upload")
    fun uploadPdf(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        // 1. Save uploaded file
        println("pass 0")
        val filename = "${System.currentTimeMillis()}_${file.originalFilename}"
        val inputPath = Paths.get(uploadDir, filename)
        Files.copy(file.inputStream, inputPath, StandardCopyOption.REPLACE_EXISTING)
        val inputFile = inputPath.toFile()

        println("pass 1")
        // 2. Process with AI agent
        val gptClient = GPTCilent(openAiTokem)
        val outputFile = File(uploadDir, "processed_$filename")
        val conversationHistory = mutableListOf<String>()
        var n = 1
        runCatching {
            n++
            while (true) {
                kotlinx.coroutines.runBlocking {
                    val result = gptClient.agent.createAgentAndRun(
                        "Process this PDF file: ${inputFile.absolutePath}. Write the result to: ${outputFile.absolutePath} and take in mind ${conversationHistory.size}",
                    )
                    conversationHistory.add(result.toString())
                    println(result.toString())

                    val review = gptClient.agent.createAgentAndRun(
                        "review ${outputFile.absolutePath} if it not done say no and give out reasoning if yes say Yes only"
                    )
                    val reviewText = review.toString().trim()
                    println(reviewText)
                    conversationHistory.add(reviewText)

                    if (reviewText.equals("Yes", ignoreCase = true)) {
                        return@runBlocking
                    }
                }

                // Check outside runBlocking to allow clean break
                if (conversationHistory.lastOrNull()?.equals("Yes", ignoreCase = true) == true) {
                    break
                }
                else if (n == 5) {break}
            }
        }.onFailure {
            return ResponseEntity.internalServerError().body("AI processing failed: ${it.message}")
        }

        println("pass 2")
        // 3. Send processed file to Slack
        val fileToSend = if (outputFile.exists()) outputFile else inputFile
        println("pass test")
        runCatching {
            sendPdfToSlack(
                botToken = slackBotTokem,
                channelId = slackChannelId,
                pdfFile = fileToSend,
                message = "Here is your processed PDF!"
            )
        }.onFailure {
            return ResponseEntity.internalServerError().body("Slack send failed: ${it.message}")
        }
        println("pass 3")
        return ResponseEntity.ok("PDF uploaded, processed, and sent to Slack successfully!")
    }
}