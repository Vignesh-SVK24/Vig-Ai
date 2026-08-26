package com.example.vig.tools

import com.example.vig.domain.models.RiskLevel

class OpenAppTool : Tool {
    override val name = "OpenApp"
    override val description = "Opens an application by name (requires Android context - not yet connected)"
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ appName: String }"
    override val outputSchema = "{ success: Boolean, message: String }"
    override suspend fun execute(input: String): ToolResult {
        // Not connected to Android PackageManager yet
        return ToolResult(success = false, message = "OpenApp tool is not yet connected to device. App launching will be available in a future update.")
    }
    override suspend fun verify(result: ToolResult): Boolean = false
}

class WebSearchTool : Tool {
    override val name = "WebSearch"
    override val description = "Searches the web for information (requires network API - not yet connected)"
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ query: String }"
    override val outputSchema = "{ results: List<String> }"
    override suspend fun execute(input: String): ToolResult {
        return ToolResult(success = false, message = "WebSearch tool is not yet connected to a search API.")
    }
    override suspend fun verify(result: ToolResult): Boolean = false
}
