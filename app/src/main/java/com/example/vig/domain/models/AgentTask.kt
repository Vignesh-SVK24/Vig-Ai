package com.example.vig.domain.models

import java.util.UUID

enum class StepStatus {
    PENDING,
    RUNNING,
    WAITING_CONFIRMATION,
    COMPLETED,
    FAILED,
    SKIPPED
}

enum class AutonomyLevel {
    ASSISTED, // ViG asks before executing most actions
    BALANCED, // Default: Low risk auto-executes, medium/high risk asks confirmation
    STRICT    // ViG asks before every external or device action
}

enum class TaskCategory {
    CHAT,
    QUICK_ANSWER,
    REASONING,
    PLANNING,
    DOCUMENT,
    PDF,
    RESEARCH,
    CODING,
    VISION,
    VOICE_CONVERSATION,
    STUDY,
    TOOL_EXECUTION
}

data class TaskStep(
    val stepId: String = UUID.randomUUID().toString(),
    val description: String,
    val toolName: String,
    val arguments: Map<String, String> = emptyMap(),
    var status: StepStatus = StepStatus.PENDING,
    var output: String? = null,
    var verificationResult: Boolean? = null,
    var retryCount: Int = 0,
    val requiresConfirmation: Boolean = false
)

data class AgentTask(
    val taskId: String = UUID.randomUUID().toString(),
    val originalCommand: String,
    val interpretedIntent: String = "",
    val category: TaskCategory = TaskCategory.CHAT,
    var selectedModel: String = "Gemini",
    val steps: MutableList<TaskStep> = mutableListOf(),
    var currentStepIndex: Int = 0,
    var riskLevel: RiskLevel = RiskLevel.LOW,
    var status: AgentState = AgentState.IDLE,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    var result: String? = null,
    var error: String? = null,
    var retryCount: Int = 0
)
