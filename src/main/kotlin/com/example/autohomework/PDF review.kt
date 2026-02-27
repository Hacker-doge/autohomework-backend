package com.example.autohomework

import ai.koog.agents.core.agent.AIAgentService
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tool
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.ext.tool.file.ReadFileTool
import ai.koog.agents.ext.tool.file.WriteFileTool
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.markdown.markdown
import ai.koog.rag.base.files.JVMFileSystemProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.io.File

class GPTCilentReview(tokem: String)  {

    val toolRegistry = ToolRegistry {
        tools(toolsList = listOf(ReadFile))
    }

    val agent = AIAgentService(
        promptExecutor = simpleOpenAIExecutor(tokem),
        systemPrompt = """
            Your a homework review assistant.. you mak sure the homework is done
          -  ReadFile: Read the pdf
            Use these tools to review the homework tasks in the given PDF.
            only say the world done in all lowercases if you think the pdf is done
        """.trimIndent(),
        llmModel = OpenAIModels.Chat.GPT5Mini,
        toolRegistry = toolRegistry,
        maxIterations = 100

    )
}