package com.example.autohomework

import ai.koog.agents.core.tools.Tool
import ai.koog.prompt.markdown.markdown
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.io.File

object EditFile : Tool<EditFile.Args, EditFile.Result>(
    argsSerializer = serializer(),
    resultSerializer = serializer(),
    name = "edit_file",
    description = """
        Appends text content to a specific page of a PDF file at a fixed position (x=100, y=700).
        
        Use this tool when you need to:
        - Add answers, annotations, or text to a homework PDF
        - Fill in responses on a specific page of a document
        - Write content onto an existing PDF page
        
        IMPORTANT CONSTRAINTS:
        - Text is always placed at coordinates (100, 700) in Helvetica Bold 12pt — do NOT use for precise layout
        - This APPENDS to the page — existing content is preserved
        - The file is overwritten in-place; there is no undo
        - Page index is 0-based (first page = 0)
        
        Do NOT use this tool to read file contents — use read_file instead.
    """.trimIndent()
) {
    @Serializable
    data class Args(
        val file: String,       // Absolute or relative path to the target PDF file (e.g. "homework/math.pdf")
        val page: Int,          // 0-based page index to write to (0 = first page, 1 = second page, etc.)
        val edit: String        // The plain text string to append to the page. Keep concise — no newlines supported, text will clip if too long.
    )

    @Serializable
    data class Result(
        private val patchApplyResult: PatchApplyResult
    ) {
        @Serializable
        sealed interface PatchApplyResult {
            @Serializable
            data class Success(val updatedContent: String) : PatchApplyResult

            @Serializable
            data class Failure(val reason: String) : PatchApplyResult
        }

        fun textForLLM(): String = markdown {
            if (patchApplyResult is PatchApplyResult.Success) {
                line {
                    bold("Success:").text(" text was appended to PDF page. The file has been saved.")
                }
                line {
                    text("Modified file: ${(patchApplyResult as PatchApplyResult.Success).updatedContent}")
                }
                line {
                    text("You may call this tool again to append additional text to another page, or proceed to the next task.")
                }
            } else {
                line {
                    bold("Failed:").text(" the PDF was NOT modified.")
                }
                line {
                    text("Reason: ${(patchApplyResult as PatchApplyResult.Failure).reason}")
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
            Loader.loadPDF(File(args.file)).use { document ->
                val pdPage = document.getPage(args.page)
                PDPageContentStream(
                    document,
                    pdPage,
                    PDPageContentStream.AppendMode.APPEND,
                    true
                ).use { contentStream ->
                    contentStream.beginText()
                    val font = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                    contentStream.setFont(font, 12f)
                    contentStream.newLineAtOffset(100f, 700f)
                    contentStream.showText(args.edit)
                    contentStream.endText()
                }
                document.save(args.file)
            }
            Result(Result.PatchApplyResult.Success(args.file))
        } catch (e: Exception) {
            Result(Result.PatchApplyResult.Failure(e.message ?: "Unknown error"))
        }
    }
}