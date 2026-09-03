package com.example.vig.agent.provider

import com.example.vig.domain.interfaces.AIProvider
import com.example.vig.domain.router.ModelRouter
import com.example.vig.security.KeyStoreManager

class MultiAIProvider(private val keyStoreManager: KeyStoreManager) : AIProvider {
    val geminiProvider = GeminiProvider(keyStoreManager)
    val openAIProvider = OpenAIProvider(keyStoreManager)
    val claudeProvider = ClaudeProvider(keyStoreManager)

    val router = ModelRouter(
        keyStoreManager = keyStoreManager,
        geminiProvider = geminiProvider,
        openAIProvider = openAIProvider,
        claudeProvider = claudeProvider
    )

    override suspend fun isConfigured(): Boolean {
        return geminiProvider.isConfigured() || openAIProvider.isConfigured() || claudeProvider.isConfigured()
    }

    override suspend fun generateResponse(prompt: String): Result<String> {
        val category = router.classifyCategory(prompt)
        val selectedModel = router.selectModel(category)
        return router.executeWithFallback(prompt, selectedModel).map { it.first }
    }
}
