package com.example.vig.agent.core

import com.example.vig.domain.models.AgentState
import com.example.vig.domain.interfaces.AIProvider
import com.example.vig.domain.interfaces.ToolRegistry
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.CancellationException

class AgentOrchestrator(
    private val aiProvider: AIProvider,
    private val toolRegistry: ToolRegistry
) {
    private val _state = MutableStateFlow(AgentState.IDLE)
    val state: StateFlow<AgentState> = _state.asStateFlow()

    private val _timeline = MutableStateFlow<List<String>>(emptyList())
    val timeline: StateFlow<List<String>> = _timeline.asStateFlow()

    private val _agentResponse = MutableStateFlow("")
    val agentResponse: StateFlow<String> = _agentResponse.asStateFlow()

    private val mutex = Mutex()
    private var currentJob: Job? = null

    fun cancel() {
        currentJob?.cancel()
        _state.value = AgentState.CANCELLED
        addTimelineEntry("Cancelled by user.")
    }

    suspend fun processCommand(command: String) = mutex.withLock {
        _timeline.value = emptyList()
        _agentResponse.value = ""

        if (command.isBlank()) {
            updateState(AgentState.FAILED, "Empty command.")
            return@withLock
        }

        updateState(AgentState.LISTENING, "Received: $command")

        if (!aiProvider.isConfigured()) {
            updateState(AgentState.FAILED, "AI Provider not configured. Please add your API key in Settings.")
            return@withLock
        }

        // UNDERSTANDING
        updateState(AgentState.UNDERSTANDING, "Parsing intent...")
        val intentParser = IntentParser(aiProvider, toolRegistry)
        val result = intentParser.parse(command)

        result.fold(
            onSuccess = { response ->
                // PLANNING
                updateState(AgentState.PLANNING, "Plan created.")
                _agentResponse.value = response

                // VERIFICATION (for now: mark as complete since no real tool execution yet)
                updateState(AgentState.VERIFYING, "Verifying response...")
                updateState(AgentState.COMPLETED, "Done.")
            },
            onFailure = { error ->
                updateState(AgentState.FAILED, "Error: ${error.message}")
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
