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
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class GeminiProvider(val keyStoreManager: KeyStoreManager) : AIProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    override suspend fun isConfigured(): Boolean {
        return keyStoreManager.isGeminiConfigured()
    }

    suspend fun validateApiKey(keyToTest: String): Result<String> = withContext(Dispatchers.IO) {
        val trimmedKey = keyToTest.trim()
        if (trimmedKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("The Gemini API key cannot be blank."))
        }

        try {
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Ping")
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$trimmedKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("x-goog-api-key", trimmedKey)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val code = response.code

            if (response.isSuccessful) {
                Result.success("Gemini connected")
            } else if (code in listOf(400, 401, 403)) {
                Result.failure(IllegalStateException("Invalid Gemini API key"))
            } else if (code == 429) {
                Result.failure(IllegalStateException("Gemini is temporarily rate-limited. Please try again shortly."))
            } else if (code >= 500) {
                Result.failure(IllegalStateException("Gemini is temporarily unavailable."))
            } else {
                Result.failure(IllegalStateException("Invalid Gemini API key (Error $code)"))
            }
        } catch (e: UnknownHostException) {
            Result.failure(IOException("Unable to connect. Check your internet connection."))
        } catch (e: SocketTimeoutException) {
            Result.failure(IOException("Unable to connect. Check your internet connection."))
        } catch (e: IOException) {
            Result.failure(IOException("Unable to connect. Check your internet connection."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateResponse(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = keyStoreManager.getGeminiKey()
            ?: return@withContext Result.failure(IllegalStateException("Gemini is not connected. Open Settings to connect it."))

        try {
            val systemInstructions = """
                You are ViG, a helpful personal AI assistant.
                - Answer questions clearly and conversationally.
                - Be concise for simple questions, explain complex topics step-by-step.
                - Be honest when uncertain.
                - Understand follow-up questions using conversation context.
                - If the user asks you to control a device action or app that isn't connected yet (e.g. "open app"), state: "I can't control that app directly yet."
            """.trimIndent()

            val combinedPrompt = "$systemInstructions\n\nUser Question:\n$prompt"

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", combinedPrompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 1500)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("x-goog-api-key", apiKey)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val code = response.code
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext when {
                    code in listOf(400, 401, 403) -> {
                        Result.failure(IllegalStateException("The Gemini API key is invalid. Please update it in Settings."))
                    }
                    code == 429 -> {
                        Result.failure(IllegalStateException("Gemini is temporarily rate-limited. Please try again shortly."))
                    }
                    code >= 500 -> {
                        Result.failure(IllegalStateException("Gemini is temporarily unavailable."))
                    }
                    else -> {
                        Result.failure(IllegalStateException("Something went wrong while contacting Gemini (Error $code)."))
                    }
                }
            }

            val jsonResponse = JSONObject(body)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(IllegalStateException("No response generated by Gemini."))
            }
        } catch (e: UnknownHostException) {
            Result.failure(IOException("I can't reach Gemini right now. Check your internet connection."))
        } catch (e: SocketTimeoutException) {
            Result.failure(IOException("I can't reach Gemini right now. Check your internet connection."))
        } catch (e: IOException) {
            Result.failure(IOException("I can't reach Gemini right now. Check your internet connection."))
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.message ?: "Something went wrong while contacting Gemini."))
        }
    }
}
