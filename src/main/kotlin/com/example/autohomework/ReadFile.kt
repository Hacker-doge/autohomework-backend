package com.example.autohomework

import ai.koog.agents.core.tools.Tool
import ai.koog.prompt.markdown.markdown
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import java.io.File

object ReadFile : Tool<ReadFile.Args, ReadFile.Result>(
    argsSerializer = serializer(),
    resultSerializer = serializer(),
    name = "read_file",
    description = """
        Reads and returns the text content from a plain text (.txt) file.
        
        Use this tool when you need to:
        - Read the questions or instructions in a homework TXT file
        - Inspect the existing content of a file before editing
        - Retrieve text from a file to understand what needs to be answered
        
        IMPORTANT CONSTRAINTS:
        - Reads the entire file content as plain text
        - This tool does NOT modify the file in any way
        
        Do NOT use this tool to write content — use edit_file instead.
    """.trimIndent()
) {
    @Serializable
    data class Args(
        val file: String  // Absolute or relative path to the target TXT file (e.g. "homework/math.txt")
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
                    bold("Success:").text(" text was read from the file.")
                }
                line {
                    text("File content:")
                }
                line {
                    text((readResult as ReadResult.Success).content.ifBlank { "(File is empty)" })
                }
                line {
                    text("You may now use edit_file to write answers, or read another file.")
                }
            } else {
                line {
                    bold("Failed:").text(" could not read the file.")
                }
                line {
                    text("Reason: ${(readResult as ReadResult.Failure).reason}")
                }
                line {
                    text("Check that the file path is correct and the file exists. Then retry.")
                }
            }
        }

        override fun toString(): String = textForLLM()
    }

    override suspend fun execute(args: Args): Result {
        return try {
            val content = File(args.file).readText()
            Result(Result.ReadResult.Success(content.trim()))
        } catch (e: Exception) {
            Result(Result.ReadResult.Failure(e.message ?: "Unknown error"))
        }
    }
}