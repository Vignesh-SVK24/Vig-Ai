package com.example.vig.agent.provider

import com.example.vig.domain.interfaces.AIProvider
import com.example.vig.security.KeyStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenAIProvider(private val keyStoreManager: KeyStoreManager) : AIProvider {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun isConfigured(): Boolean {
        return !keyStoreManager.getApiKey().isNullOrBlank()
    }

    override suspend fun generateResponse(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = keyStoreManager.getApiKey()
            ?: return@withContext Result.failure(IllegalStateException("OpenAI API key not configured."))

        try {
            val systemPrompt = """
                You are ViG (Your Personal Intelligence), an elite personal AI agent powered by GPT-4o.
                
                CHAIN-OF-THOUGHT SYSTEM INSTRUCTIONS:
                1. REASONING: Analyze the user's intent, context, and requirements step-by-step.
                2. STRUCTURE: Provide an articulate, highly structured explanation with key concepts and bold formatting.
                3. ELEGANCE: Maintain an intelligent, warm, personal companion tone.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("model", "gpt-4o")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("temperature", 0.7)
                put("max_tokens", 1024)
            }

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer ")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val bodyStr = response.body?.string() ?: ""
            
            if (!response.isSuccessful) {
                return@withContext Result.failure(IllegalStateException("OpenAI API Error (): "))
            }

            val jsonResponse = JSONObject(bodyStr)
            val text = jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
