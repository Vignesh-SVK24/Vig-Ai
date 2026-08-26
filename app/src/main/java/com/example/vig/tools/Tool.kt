package com.example.vig.tools

import com.example.vig.domain.models.RiskLevel

data class ToolResult(
    val success: Boolean,
    val message: String,
    val data: Map<String, String> = emptyMap()
)

interface Tool {
    val name: String
    val description: String
    val riskLevel: RiskLevel
    val requiredPermissions: List<String>
    val inputSchema: String
    val outputSchema: String

    suspend fun execute(input: String): ToolResult
    suspend fun verify(result: ToolResult): Boolean
}
