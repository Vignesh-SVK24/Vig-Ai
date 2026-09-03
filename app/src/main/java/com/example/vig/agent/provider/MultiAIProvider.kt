package com.example.vig.agent.provider

import com.example.vig.domain.interfaces.AIProvider
import com.example.vig.security.KeyStoreManager

class MultiAIProvider(private val keyStoreManager: KeyStoreManager) : AIProvider {
    val geminiProvider = GeminiProvider(keyStoreManager)
    val openAIProvider = OpenAIProvider(keyStoreManager)
    val claudeProvider = ClaudeProvider(keyStoreManager)

    override suspend fun isConfigured(): Boolean {
        return when (keyStoreManager.getProvider()) {
            "openai" -> openAIProvider.isConfigured()
            "claude" -> claudeProvider.isConfigured()
            else -> geminiProvider.isConfigured()
        }
    }

    override suspend fun generateResponse(prompt: String): Result<String> {
        val selected = keyStoreManager.getProvider()
        return when (selected) {
            "openai" -> {
                if (openAIProvider.isConfigured()) {
                    openAIProvider.generateResponse(prompt)
                } else if (geminiProvider.isConfigured()) {
                    geminiProvider.generateResponse(prompt)
                } else {
                    Result.failure(IllegalStateException("OpenAI is not configured. Add your key in Settings or switch to Gemini."))
                }
            }
            "claude" -> {
                if (claudeProvider.isConfigured()) {
                    claudeProvider.generateResponse(prompt)
                } else if (geminiProvider.isConfigured()) {
                    geminiProvider.generateResponse(prompt)
                } else {
                    Result.failure(IllegalStateException("Claude is not configured. Add your key in Settings or switch to Gemini."))
                }
            }
            else -> {
                // Primary: Gemini
                geminiProvider.generateResponse(prompt)
            }
        }
    }
}
