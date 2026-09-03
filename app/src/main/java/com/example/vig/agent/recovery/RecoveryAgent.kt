package com.example.vig.agent.recovery

import android.content.Context
import com.example.vig.domain.interfaces.ToolRegistry
import com.example.vig.tools.ToolResult

data class RecoveryPlan(
    val alternativeTool: String,
    val alternativeInput: String,
    val explanation: String
)

class RecoveryAgent(private val toolRegistry: ToolRegistry) {

    fun determineRecovery(toolName: String, failedInput: String, error: String?): RecoveryPlan? {
        val lowerTool = toolName.lowercase()
        val lowerInput = failedInput.lowercase()

        return when {
            lowerTool == "openapp" && error == "APP_NOT_INSTALLED" -> {
                when {
                    lowerInput.contains("youtube") -> RecoveryPlan(
                        alternativeTool = "OpenUrl",
                        alternativeInput = "https://youtube.com",
                        explanation = "YouTube app isn't installed. Opening YouTube in browser instead."
                    )
                    lowerInput.contains("maps") -> RecoveryPlan(
                        alternativeTool = "OpenUrl",
                        alternativeInput = "https://maps.google.com",
                        explanation = "Maps app isn't installed. Opening Google Maps in browser instead."
                    )
                    else -> RecoveryPlan(
                        alternativeTool = "WebSearch",
                        alternativeInput = failedInput,
                        explanation = "$failedInput app isn't installed. Searching the web instead."
                    )
                }
            }
            else -> null
        }
    }

    suspend fun attemptRecovery(
        recoveryPlan: RecoveryPlan,
        context: Context?
    ): ToolResult {
        val tool = toolRegistry.getTool(recoveryPlan.alternativeTool)
            ?: return ToolResult(false, "Alternative tool ${recoveryPlan.alternativeTool} not found.")

        val res = tool.execute(recoveryPlan.alternativeInput, context)
        return if (res.success) {
            ToolResult(
                success = true,
                message = "${recoveryPlan.explanation} ${res.message}",
                data = res.data
            )
        } else {
            res
        }
    }
}
