package com.example.autohomework

import ai.koog.agents.core.agent.AIAgentService
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.file.ReadFileTool
import ai.koog.agents.ext.tool.file.WriteFileTool
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.rag.base.files.JVMFileSystemProvider

class GPTCilent(tokem: String)  {

    val toolRegistry = ToolRegistry {
        tool(ReadFileTool(JVMFileSystemProvider.ReadOnly))
        tool(WriteFileTool(JVMFileSystemProvider.ReadWrite))
    }

    val agent = AIAgentService(
        promptExecutor = simpleOpenAIExecutor(tokem),
        systemPrompt = "You do the pdf that I gave you",
        llmModel = OpenAIModels.Chat.GPT5Mini,
        toolRegistry = toolRegistry,
        maxIterations = 100

    )
}
