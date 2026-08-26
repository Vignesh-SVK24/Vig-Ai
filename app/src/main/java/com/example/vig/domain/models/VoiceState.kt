package com.example.vig.domain.models

enum class VoiceState {
    IDLE,
    ACTIVATING,
    LISTENING,
    PROCESSING_AUDIO,
    TRANSCRIBING,
    SPEAKING,
    ERROR
}
