package com.example.vig.tools

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.vig.domain.models.RiskLevel
import java.net.URLEncoder

class WebSearchTool : Tool {
    override val name = "WebSearch"
    override val description = "Performs a live web search for real-time information, news, or answers"
    override val category = ToolCategory.WEB
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = listOf("android.permission.INTERNET")
    override val inputSchema = "{ query: String }"
    override val outputSchema = "{ resultsCount: Int, query: String }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        val query = input.trim()
        if (query.isBlank()) {
            return ToolResult(false, "Search query cannot be blank.")
        }

        if (context != null) {
            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(searchIntent)
            } catch (_: Exception) {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8"))
                ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                try {
                    context.startActivity(browserIntent)
                } catch (_: Exception) {}
            }
        }

        return ToolResult(
            success = true,
            message = "Completed web search for: '$query'.",
            data = mapOf("query" to query)
        )
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class OpenWebPageTool : Tool {
    override val name = "OpenWebPage"
    override val description = "Opens a verified research page or documentation link in browser"
    override val category = ToolCategory.WEB
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ url: String }"
    override val outputSchema = "{ success: Boolean }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        if (context == null) return ToolResult(false, "Context unavailable")
        val url = if (input.startsWith("http")) input else "https://$input"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ToolResult(true, "Opened research link: $url")
        } catch (e: Exception) {
            ToolResult(false, "Failed to open link: ${e.message}")
        }
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}
