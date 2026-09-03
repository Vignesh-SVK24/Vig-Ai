package com.example.vig.domain.models

data class AIResponse(
    val text: String,
    val provider: String,
    val model: String,
    val success: Boolean,
    val error: String? = null
)
