package com.example.vig.tools

import android.content.Context
import com.example.vig.domain.models.RiskLevel
import java.util.concurrent.ConcurrentHashMap

object GlobalMemoryStore {
    val items = ConcurrentHashMap<String, String>()
    init {
        items["study_preference"] = "Prefers studying at 7:00 PM with 45-minute focus intervals."
        items["user_name"] = "Vignesh"
    }
}

class RememberTool : Tool {
    override val name = "Remember"
    override val description = "Stores user-approved context or facts in long-term memory"
    override val category = ToolCategory.MEMORY
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ key: String, fact: String }"
    override val outputSchema = "{ saved: Boolean, key: String }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        val key = if (input.contains(":")) input.substringBefore(":").trim() else "fact_${System.currentTimeMillis()}"
        val value = if (input.contains(":")) input.substringAfter(":").trim() else input.trim()
        GlobalMemoryStore.items[key] = value

        return ToolResult(
            success = true,
            message = "Remembered: '$value'",
            data = mapOf("key" to key, "value" to value)
        )
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class RecallTool : Tool {
    override val name = "Recall"
    override val description = "Retrieves stored facts or user preferences from memory"
    override val category = ToolCategory.MEMORY
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ query: String }"
    override val outputSchema = "{ facts: List<String> }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        val query = input.lowercase().trim()
        val matches = GlobalMemoryStore.items.filter { (k, v) ->
            k.lowercase().contains(query) || v.lowercase().contains(query)
        }

        val resultMsg = if (matches.isNotEmpty()) {
            "Found in memory:\n" + matches.entries.joinToString("\n") { "• ${it.key}: ${it.value}" }
        } else {
            "No specific memory found matching '$input'."
        }

        return ToolResult(
            success = true,
            message = resultMsg,
            data = mapOf("count" to matches.size.toString())
        )
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class ForgetTool : Tool {
    override val name = "Forget"
    override val description = "Removes a specific memory item or preference"
    override val category = ToolCategory.MEMORY
    override val riskLevel = RiskLevel.MEDIUM
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{ key: String }"
    override val outputSchema = "{ removed: Boolean }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        val query = input.lowercase().trim()
        val removed = GlobalMemoryStore.items.keys.filter { it.lowercase().contains(query) }
        removed.forEach { GlobalMemoryStore.items.remove(it) }

        return ToolResult(
            success = true,
            message = if (removed.isNotEmpty()) "Removed from memory: ${removed.joinToString()}" else "Item not found in memory.",
            data = mapOf("removedCount" to removed.size.toString())
        )
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}

class ListMemoryTool : Tool {
    override val name = "ListMemory"
    override val description = "Lists all remembered preferences and facts"
    override val category = ToolCategory.MEMORY
    override val riskLevel = RiskLevel.LOW
    override val requiredPermissions = emptyList<String>()
    override val inputSchema = "{}"
    override val outputSchema = "{ memories: Map<String, String> }"

    override suspend fun execute(input: String, context: Context?): ToolResult {
        val all = GlobalMemoryStore.items
        val msg = if (all.isNotEmpty()) {
            "Stored Memories:\n" + all.entries.joinToString("\n") { "• ${it.key}: ${it.value}" }
        } else {
            "No memories saved."
        }
        return ToolResult(success = true, message = msg)
    }

    override suspend fun verify(result: ToolResult, context: Context?): Boolean = result.success
}
