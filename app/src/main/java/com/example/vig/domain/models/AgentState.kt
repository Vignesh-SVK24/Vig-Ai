package com.example.vig.domain.models

enum class AgentState {
    IDLE,
    LISTENING,
    UNDERSTANDING,
    PLANNING,
    WAITING_FOR_PERMISSION,
    WAITING_FOR_CONFIRMATION,
    EXECUTING,
    VERIFYING,
    COMPLETED,
    FAILED,
    CANCELLED
}
