package com.example.vig.agent.core

import com.example.vig.domain.interfaces.AIProvider
import com.example.vig.domain.interfaces.ToolRegistry

class IntentParser(
    private val aiProvider: AIProvider,
    private val toolRegistry: ToolRegistry
) {
    suspend fun parse(command: String): Result<String> {
        val toolsDesc = toolRegistry.getAllTools().joinToString("\n") { tool ->
            "- ${tool.name}: ${tool.description} (risk: ${tool.riskLevel})"
        }
        val prompt = buildString {
            appendLine("You are ViG (Your Personal Intelligence), an elite personal AI companion.")
            appendLine("User Query: '$command'")
            appendLine()
            appendLine("Available Android Tools:")
            appendLine(toolsDesc)
            appendLine()
            appendLine("CHAIN-OF-THOUGHT INSTRUCTIONS:")
            appendLine("1. Step-by-Step Reasoning: Deconstruct the request into core concepts and goals.")
            appendLine("2. If a device tool is needed, output a clear, verified action plan.")
            appendLine("3. If an educational or technical question, deliver an articulate, structured explanation with key concepts and real-world clarity.")
            appendLine("4. Respond with high intelligence, precision, and elegance.")
        }
        return aiProvider.generateResponse(prompt)
    }
}
