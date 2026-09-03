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

class ClaudeProvider(private val keyStoreManager: KeyStoreManager) : AIProvider {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun isConfigured(): Boolean {
        return keyStoreManager.isClaudeConfigured()
    }

    override suspend fun generateResponse(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = keyStoreManager.getClaudeKey()
            ?: return@withContext Result.failure(IllegalStateException("Claude API key not configured."))

        try {
            val systemPrompt = """
                You are ViG, a helpful personal AI assistant.
                Answer questions clearly, be concise for simple questions, explain complex topics step-by-step.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("model", "claude-3-5-sonnet-20241022")
                put("max_tokens", 1024)
                put("system", systemPrompt)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
            }

            val request = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val bodyStr = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(IllegalStateException("Claude API Error (${response.code}): $bodyStr"))
            }

            val jsonResponse = JSONObject(bodyStr)
            val text = jsonResponse.getJSONArray("content")
                .getJSONObject(0)
                .getString("text")

            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
