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
        Appends text content to a plain text (.txt) file.
        
        Use this tool when you need to:
        - Add answers, annotations, or text to a homework TXT file
        - Append responses to an existing text document
        - Write content onto an existing TXT file
        
        IMPORTANT CONSTRAINTS:
        - This APPENDS to the file — existing content is preserved
        - The file is modified in-place; there is no undo
        - If the file does not exist, it will be created automatically
        
        Do NOT use this tool to read file contents — use read_file instead.
    """.trimIndent()
) {
    @Serializable
    data class Args(
        val file: String,   // Absolute or relative path to the target TXT file (e.g. "homework/math.txt")
        val edit: String    // The text string to append to the file. Newlines (\n) are supported.
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
                    bold("Success:").text(" text was appended to the file. The file has been saved.")
                }
                line {
                    text("Modified file: ${(patchApplyResult as PatchApplyResult.Success).updatedContent}")
                }
                line {
                    text("You may call this tool again to append additional text, or proceed to the next task.")
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
            file.appendText(args.edit + "\n")
            Result(Result.PatchApplyResult.Success(args.file))
        } catch (e: Exception) {
            Result(Result.PatchApplyResult.Failure(e.message ?: "Unknown error"))
        }
    }
}