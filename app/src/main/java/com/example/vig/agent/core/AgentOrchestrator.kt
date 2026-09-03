package com.example.vig.agent.core

import android.content.Context
import com.example.vig.agent.context.ConversationContextManager
import com.example.vig.agent.manager.CapabilityManager
import com.example.vig.agent.manager.ConfirmationManager
import com.example.vig.agent.manager.EmergencyLockManager
import com.example.vig.agent.manager.PermissionManager
import com.example.vig.agent.manager.RiskManager
import com.example.vig.agent.provider.MultiAIProvider
import com.example.vig.domain.interfaces.ToolRegistry
import com.example.vig.domain.models.AgentState
import com.example.vig.domain.models.AgentTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AgentOrchestrator(
    val aiProvider: MultiAIProvider,
    val toolRegistry: ToolRegistry,
    var androidContext: Context? = null
) {
    private val _state = MutableStateFlow(AgentState.IDLE)
    val state: StateFlow<AgentState> = _state.asStateFlow()

    private val _timeline = MutableStateFlow<List<String>>(emptyList())
    val timeline: StateFlow<List<String>> = _timeline.asStateFlow()

    private val _agentResponse = MutableStateFlow("")
    val agentResponse: StateFlow<String> = _agentResponse.asStateFlow()

    private val _activeTask = MutableStateFlow<AgentTask?>(null)
    val activeTask: StateFlow<AgentTask?> = _activeTask.asStateFlow()

    val capabilityManager = CapabilityManager(androidContext)
    val permissionManager = PermissionManager(androidContext)
    val riskManager = RiskManager()
    val confirmationManager = ConfirmationManager()
    val contextManager = ConversationContextManager()

    private val mutex = Mutex()
    private var currentJob: Job? = null

    fun isAIConfigured(): Boolean {
        return aiProvider.geminiProvider.keyStoreManager.isGeminiConfigured()
    }

    fun cancel() {
        currentJob?.cancel()
        _state.value = AgentState.CANCELLED
        addTimelineEntry("Cancelled by user.")
    }

    suspend fun confirmPending() {
        confirmationManager.confirm()
    }

    suspend fun denyPending() {
        confirmationManager.deny()
        _state.value = AgentState.CANCELLED
        addTimelineEntry("Action denied by user.")
    }

    suspend fun processCommand(command: String) = mutex.withLock {
        _timeline.value = emptyList()
        _agentResponse.value = ""

        val trimmedCmd = command.trim()
        if (trimmedCmd.isBlank()) {
            updateState(AgentState.FAILED, "Empty command.")
            return@withLock
        }

        if (EmergencyLockManager.isEmergencyLocked()) {
            updateState(AgentState.FAILED, "Emergency Lock is active.")
            _agentResponse.value = "Emergency Lock is active. AI actions are temporarily disabled."
            return@withLock
        }

        // Check if Gemini or active provider is configured
        if (!isAIConfigured()) {
            updateState(AgentState.FAILED, "Gemini is not connected.")
            _agentResponse.value = "Gemini is not connected. Open Settings to connect it."
            return@withLock
        }

        updateState(AgentState.LISTENING, "Sending...")

        // Handle direct greetings without unnecessary API latency
        val lower = trimmedCmd.lowercase()
        if (lower.matches(Regex("""^(hi|hello|hey|greetings|hola)\s*(vig|vig-ai)?[\.!\?]*$"""))) {
            updateState(AgentState.UNDERSTANDING, "Thinking...")
            delay(250)
            val greeting = "Hello! I'm ViG, your personal intelligence. How can I help?"
            _agentResponse.value = greeting
            contextManager.addTurn(trimmedCmd, greeting)
            updateState(AgentState.COMPLETED, "Ready")
            return@withLock
        }

        // Device tool safety: if user says "open youtube", "open app"
        if (lower.startsWith("open ") || lower.startsWith("launch ")) {
            val appTarget = lower.replace("open ", "").replace("launch ", "").trim()
            val tool = toolRegistry.getTool("OpenApp")
            if (tool != null && androidContext != null) {
                updateState(AgentState.EXECUTING, "Opening $appTarget...")
                val res = tool.execute(appTarget, androidContext)
                if (res.success) {
                    _agentResponse.value = res.message
                    contextManager.addTurn(trimmedCmd, res.message)
                    updateState(AgentState.COMPLETED, "Ready")
                } else {
                    val msg = "I can't control that app yet."
                    _agentResponse.value = msg
                    contextManager.addTurn(trimmedCmd, msg)
                    updateState(AgentState.COMPLETED, "Ready")
                }
                return@withLock
            } else {
                val msg = "I can't control that app yet."
                _agentResponse.value = msg
                contextManager.addTurn(trimmedCmd, msg)
                updateState(AgentState.COMPLETED, "Ready")
                return@withLock
            }
        }

        // Call Gemini for real AI answering with conversational continuity
        updateState(AgentState.UNDERSTANDING, "Thinking...")

        val contextHistory = contextManager.getFormattedContext()
        val fullPrompt = buildString {
            if (contextHistory.isNotBlank()) {
                appendLine(contextHistory)
            }
            appendLine("User Question:")
            appendLine(trimmedCmd)
        }

        updateState(AgentState.PLANNING, "Responding...")

        val result = aiProvider.generateResponse(fullPrompt)
        result.fold(
            onSuccess = { answer ->
                _agentResponse.value = answer
                contextManager.addTurn(trimmedCmd, answer)
                updateState(AgentState.COMPLETED, "Ready")
            },
            onFailure = { error ->
                val errorMsg = error.message ?: "Something went wrong while contacting Gemini."
                _agentResponse.value = errorMsg
                updateState(AgentState.FAILED, errorMsg)
            }
        )
    }

    private fun updateState(newState: AgentState, log: String) {
        _state.value = newState
        addTimelineEntry(log)
    }

    private fun addTimelineEntry(entry: String) {
        if (entry.isNotEmpty()) {
            _timeline.value = _timeline.value + entry
        }
    }
}
