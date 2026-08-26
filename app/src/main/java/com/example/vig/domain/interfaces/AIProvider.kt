package com.example.vig.domain.interfaces

interface AIProvider {
    suspend fun generateResponse(prompt: String): Result<String>
    suspend fun isConfigured(): Boolean
}
