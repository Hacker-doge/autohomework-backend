package com.example.autohomework

import ai.koog.agents.core.agent.AIAgentService
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.file.ReadFileTool
import ai.koog.agents.ext.tool.file.WriteFileTool
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleAnthropicExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.rag.base.files.JVMFileSystemProvider

class GPTCilent(tokem: String)  {

    val toolRegistry = ToolRegistry {
        tool(ReadFileTool(JVMFileSystemProvider.ReadOnly))
        tool(WriteFileTool(JVMFileSystemProvider.ReadWrite))
    }

    val agent = AIAgentService(
        promptExecutor = simpleAnthropicExecutor(tokem),
        systemPrompt = "If your task is to teach, please review the worksheet. If your task is to complete it, provide as many answers as possible in the text file.",
        llmModel = AnthropicModels.Haiku_4_5,
        toolRegistry = toolRegistry,
        maxIterations = 100

    )
}