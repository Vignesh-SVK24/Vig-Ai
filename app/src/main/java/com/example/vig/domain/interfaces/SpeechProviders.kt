package com.example.vig.domain.interfaces

import kotlinx.coroutines.flow.StateFlow

interface SpeechToTextProvider {
    val transcription: StateFlow<String>
    val isListening: StateFlow<Boolean>
    val error: StateFlow<String?>
    fun startListening()
    fun stopListening()
    fun cancel()
    fun destroy()
}

interface TextToSpeechProvider {
    fun speak(text: String, onDone: (() -> Unit)? = null)
    fun stop()
    fun setSpeed(speed: Float)
    fun destroy()
}
