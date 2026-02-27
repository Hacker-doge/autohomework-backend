package com.example.autohomework

import ai.koog.agents.core.tools.Tool
import ai.koog.prompt.markdown.markdown
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.apache.pdfbox.Loader
import java.io.File

object ReadFile : Tool<ReadFile.Args, ReadFile.Result>(
    argsSerializer = serializer(),
    resultSerializer = serializer(),
    name = "read_file",
    description = """
        Reads and extracts the text content from a specific page of a PDF file.
        
        Use this tool when you need to:
        - Read the questions or instructions on a homework PDF page
        - Inspect the existing content of a page before editing
        - Retrieve text from a specific page to understand what needs to be answered
        
        IMPORTANT CONSTRAINTS:
        - Extraction is text-only — images, diagrams, and formatted layouts are not captured
        - Page index is 0-based (first page = 0)
        - This tool does NOT modify the file in any way
        
        Do NOT use this tool to write content — use edit_file instead.
    """.trimIndent()
) {
    @Serializable
    data class Args(
        val file: String,  // Absolute or relative path to the target PDF file (e.g. "homework/math.pdf")
        val page: Int      // 0-based page index to read from (0 = first page, 1 = second page, etc.)
    )

    @Serializable
    data class Result(
        private val readResult: ReadResult
    ) {
        @Serializable
        sealed interface ReadResult {
            @Serializable
            data class Success(val content: String) : ReadResult

            @Serializable
            data class Failure(val reason: String) : ReadResult
        }

        fun textForLLM(): String = markdown {
            if (readResult is ReadResult.Success) {
                line {
                    bold("Success:").text(" text was extracted from the PDF page.")
                }
                line {
                    text("Page content:")
                }
                line {
                    text((readResult as ReadResult.Success).content.ifBlank { "(No extractable text found on this page)" })
                }
                line {
                    text("You may now use edit_file to write answers, or read another page.")
                }
            } else {
                line {
                    bold("Failed:").text(" could not read the PDF page.")
                }
                line {
                    text("Reason: ${(readResult as ReadResult.Failure).reason}")
                }
                line {
                    text("Check that the file path is correct, the file is a valid PDF, and the page index exists. Then retry.")
                }
            }
        }

        override fun toString(): String = textForLLM()
    }

    override suspend fun execute(args: Args): Result {
        return try {
            val content = Loader.loadPDF(File(args.file)).use { document ->
                val stripper = org.apache.pdfbox.text.PDFTextStripper().apply {
                    startPage = args.page + 1  // PDFTextStripper is 1-based
                    endPage = args.page + 1
                }
                stripper.getText(document)
            }
            Result(Result.ReadResult.Success(content.trim()))
        } catch (e: Exception) {
            Result(Result.ReadResult.Failure(e.message ?: "Unknown error"))
        }
    }
}