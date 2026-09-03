package com.example.vig.tools

import android.content.Context
import com.example.vig.domain.models.RiskLevel

data class ToolResult(
    val success: Boolean,
    val message: String,
    val data: Map<String, String> = emptyMap(),
    val error: String? = null
)

enum class ToolCategory {
    DEVICE,
    COMMUNICATION,
    DOCUMENTS,
    WEB,
    STUDY,
    MEMORY,
    UTILITY
}

interface Tool {
    val name: String
    val description: String
    val category: ToolCategory
    val riskLevel: RiskLevel
    val requiredPermissions: List<String>
    val inputSchema: String
    val outputSchema: String
    val isAvailable: Boolean
        get() = true

    suspend fun execute(input: String, context: Context? = null): ToolResult
    suspend fun verify(result: ToolResult, context: Context? = null): Boolean
}
