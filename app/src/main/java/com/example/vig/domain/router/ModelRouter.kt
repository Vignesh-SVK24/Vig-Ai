package com.example.vig.domain.router

import com.example.vig.domain.interfaces.AIProvider
import com.example.vig.domain.models.TaskCategory
import com.example.vig.security.KeyStoreManager

class ModelRouter(
    private val keyStoreManager: KeyStoreManager,
    private val geminiProvider: AIProvider,
    private val openAIProvider: AIProvider,
    private val claudeProvider: AIProvider
) {

    fun classifyCategory(command: String): TaskCategory {
        val lower = command.lowercase()
        return when {
            lower.contains("pdf") || lower.contains("document") || lower.contains("chapter") -> TaskCategory.PDF
            lower.contains("study") || lower.contains("quiz") || lower.contains("exam") || lower.contains("flashcard") -> TaskCategory.STUDY
            lower.contains("search") || lower.contains("news") || lower.contains("find out") || lower.contains("who is") -> TaskCategory.RESEARCH
            lower.contains("open") || lower.contains("call") || lower.contains("remind") || lower.contains("alarm") || lower.contains("calendar") -> TaskCategory.TOOL_EXECUTION
            lower.contains("calculate") || lower.contains("time") || lower.contains("weather") || lower.length < 25 -> TaskCategory.QUICK_ANSWER
            lower.contains("code") || lower.contains("program") || lower.contains("algorithm") || lower.contains("debug") -> TaskCategory.CODING
            lower.contains("why") || lower.contains("explain") || lower.contains("compare") || lower.contains("analyze") -> TaskCategory.REASONING
            else -> TaskCategory.CHAT
        }
    }

    fun selectModel(category: TaskCategory): String {
        val userPreference = keyStoreManager.getProvider()
        if (userPreference != "auto") {
            return userPreference
        }

        // Automatic routing based on category and configured availability
        return when (category) {
            TaskCategory.QUICK_ANSWER, TaskCategory.TOOL_EXECUTION, TaskCategory.VOICE_CONVERSATION -> {
                if (keyStoreManager.getApiKey() != null) "gemini" else "openai"
            }
            TaskCategory.REASONING, TaskCategory.CODING -> {
                "openai"
            }
            TaskCategory.PDF, TaskCategory.DOCUMENT, TaskCategory.STUDY -> {
                "claude"
            }
            else -> "gemini"
        }
    }

    suspend fun executeWithFallback(prompt: String, preferredModel: String): Result<Pair<String, String>> {
        val providers = mutableListOf<Pair<String, AIProvider>>()

        // Build priority order based on preferredModel
        val primary = getProviderByName(preferredModel)
        providers.add(preferredModel to primary)

        // Add fallbacks
        listOf("gemini", "openai", "claude")
            .filter { it != preferredModel }
            .forEach { name ->
                providers.add(name to getProviderByName(name))
            }

        var lastError: Throwable? = null

        for ((name, provider) in providers) {
            if (provider.isConfigured()) {
                val result = provider.generateResponse(prompt)
                if (result.isSuccess) {
                    return Result.success(Pair(result.getOrThrow(), name))
                } else {
                    lastError = result.exceptionOrNull()
                }
            }
        }

        return Result.failure(
            lastError ?: IllegalStateException("No AI providers configured or available. Please configure your API key in Settings.")
        )
    }

    private fun getProviderByName(name: String): AIProvider {
        return when (name.lowercase()) {
            "openai" -> openAIProvider
            "claude" -> claudeProvider
            else -> geminiProvider
        }
    }
}
