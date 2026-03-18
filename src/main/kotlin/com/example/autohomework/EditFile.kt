package com.example.autohomework

import ai.koog.agents.core.tools.Tool
import ai.koog.prompt.markdown.markdown
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import java.io.File

object EditFile : Tool<EditFile.Args, EditFile.Result>(
    argsSerializer = serializer(),
    resultSerializer = serializer(),
    name = "edit_file",
    description = """
        Appends Markdown content to a Markdown (.md) file.
        
        Use this tool when you need to:
        - Add answers, annotations, or Markdown-formatted text to a homework MD file
        - Append new paragraphs, headings, lists, or code blocks to an existing Markdown file
        - Write structured Markdown content onto an existing MD file
        
        IMPORTANT CONSTRAINTS:
        - This APPENDS to the end of the file — existing content is preserved
        - If the file does not exist, it will be created automatically
        - The file is modified in-place; there is no undo
        - Supports any valid Markdown syntax (headings, lists, bold, code blocks, etc.)
        
        Do NOT use this tool to read file contents — use read_file instead.
    """.trimIndent()
) {
    @Serializable
    data class Args(
        val file: String,   // Absolute or relative path to the target MD file (e.g. "homework/math.md")
        val edit: String    // Markdown content to append (e.g. "## Answer\n\nThe result is **42**.")
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
                    bold("Success:").text(" Markdown content was appended to the file. The file has been saved.")
                }
                line {
                    text("Modified file: ${(patchApplyResult as PatchApplyResult.Success).updatedContent}")
                }
                line {
                    text("You may call this tool again to append additional Markdown, or proceed to the next task.")
                }
            } else {
                line {
                    bold("Failed:").text(" the file was NOT modified.")
                }
                line {
                    text("Reason: ${(patchApplyResult as PatchApplyResult.Failure).reason}")
                }
                line {
                    text("Check that the file path is correct and you have write permissions. Then retry.")
                }
            }
        }

        override fun toString(): String = textForLLM()
    }

    override suspend fun execute(args: Args): Result {
        return try {
            val file = File(args.file)
            file.parentFile?.mkdirs()

            // Add a blank line separator if the file already has content
            val prefix = if (file.exists() && file.length() > 0) "\n\n" else ""
            file.appendText(prefix + args.edit)

            Result(Result.PatchApplyResult.Success(args.file))
        } catch (e: Exception) {
            Result(Result.PatchApplyResult.Failure(e.message ?: "Unknown error"))
        }
    }
}
