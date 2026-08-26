package com.example.vig.agent.provider

import com.example.vig.domain.interfaces.AIProvider
import com.example.vig.security.KeyStoreManager

class MultiAIProvider(private val keyStoreManager: KeyStoreManager) : AIProvider {
    private val geminiProvider = GeminiProvider(keyStoreManager)
    private val openAIProvider = OpenAIProvider(keyStoreManager)
    private val claudeProvider = ClaudeProvider(keyStoreManager)

    override suspend fun isConfigured(): Boolean {
        return when (keyStoreManager.getProvider()) {
            "openai" -> openAIProvider.isConfigured()
            "claude" -> claudeProvider.isConfigured()
            else -> geminiProvider.isConfigured()
        }
    }

    override suspend fun generateResponse(prompt: String): Result<String> {
        return when (keyStoreManager.getProvider()) {
            "openai" -> openAIProvider.generateResponse(prompt)
            "claude" -> claudeProvider.generateResponse(prompt)
            else -> geminiProvider.generateResponse(prompt)
        }
    }
}
