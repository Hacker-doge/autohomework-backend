package com.example.autohomework

import com.slack.api.Slack
import java.io.File

fun sendPdfToSlack(
    botToken: String,
    channelId: String,
    pdfFile: File,
    message: String = "Here is your PDF!"
) {
    val slack = Slack.getInstance()
    val client = slack.methods(botToken)

    // Upload the PDF file
    val uploadResponse = client.filesUploadV2 { req ->
        req
            .channel(channelId)
            .file(pdfFile)
            .filename(pdfFile.name)
            .initialComment(message)
            .title(pdfFile.nameWithoutExtension)
    }

    if (uploadResponse.isOk) {
        println("PDF sent successfully: ${uploadResponse.file?.name}")
    } else {
        println("Error sending PDF: ${uploadResponse.error}")
    }
}