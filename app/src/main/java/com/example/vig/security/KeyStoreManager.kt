package com.example.vig.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class KeyStoreManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Gemini
    fun saveGeminiKey(key: String) {
        sharedPreferences.edit().putString("gemini_api_key", key.trim()).apply()
        // Also update legacy key
        sharedPreferences.edit().putString("api_key", key.trim()).apply()
    }

    fun getGeminiKey(): String? {
        val key = sharedPreferences.getString("gemini_api_key", null)
        if (!key.isNullOrBlank()) return key.trim()
        // Legacy fallback
        val legacy = sharedPreferences.getString("api_key", null)
        if (!legacy.isNullOrBlank()) {
            saveGeminiKey(legacy.trim())
            return legacy.trim()
        }
        return null
    }

    fun removeGeminiKey() {
        sharedPreferences.edit().remove("gemini_api_key").remove("api_key").apply()
    }

    fun isGeminiConfigured(): Boolean {
        return !getGeminiKey().isNullOrBlank()
    }

    // OpenAI
    fun saveOpenAIKey(key: String) {
        sharedPreferences.edit().putString("openai_api_key", key.trim()).apply()
    }

    fun getOpenAIKey(): String? {
        return sharedPreferences.getString("openai_api_key", null)?.trim()
    }

    fun removeOpenAIKey() {
        sharedPreferences.edit().remove("openai_api_key").apply()
    }

    fun isOpenAIConfigured(): Boolean {
        return !getOpenAIKey().isNullOrBlank()
    }

    // Claude
    fun saveClaudeKey(key: String) {
        sharedPreferences.edit().putString("claude_api_key", key.trim()).apply()
    }

    fun getClaudeKey(): String? {
        return sharedPreferences.getString("claude_api_key", null)?.trim()
    }

    fun removeClaudeKey() {
        sharedPreferences.edit().remove("claude_api_key").apply()
    }

    fun isClaudeConfigured(): Boolean {
        return !getClaudeKey().isNullOrBlank()
    }

    // General / Legacy
    fun saveApiKey(key: String) {
        saveGeminiKey(key)
    }

    fun getApiKey(): String? {
        return getGeminiKey()
    }

    fun saveProvider(provider: String) {
        sharedPreferences.edit().putString("ai_provider", provider).apply()
    }

    fun getProvider(): String {
        return sharedPreferences.getString("ai_provider", "gemini") ?: "gemini"
    }
}
