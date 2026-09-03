package com.example.vig.agent.core

import android.content.Context
import com.example.vig.agent.context.ConversationContextManager
import com.example.vig.agent.manager.CapabilityManager
import com.example.vig.agent.manager.ConfirmationManager
import com.example.vig.agent.manager.EmergencyLockManager
import com.example.vig.agent.manager.PermissionManager
import com.example.vig.agent.manager.RiskManager
import com.example.vig.agent.planner.TaskPlanner
import com.example.vig.agent.provider.MultiAIProvider
import com.example.vig.agent.recovery.RecoveryAgent
import com.example.vig.agent.verifier.ResultVerifier
import com.example.vig.domain.interfaces.ToolRegistry
import com.example.vig.domain.models.AgentState
import com.example.vig.domain.models.AgentTask
import com.example.vig.domain.models.StepStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
    val recoveryAgent = RecoveryAgent(toolRegistry)
    val resultVerifier = ResultVerifier()
    val contextManager = ConversationContextManager()
    private val taskPlanner = TaskPlanner(toolRegistry)

    private val mutex = Mutex()
    private var currentJob: Job? = null

    fun cancel() {
        currentJob?.cancel()
        _state.value = AgentState.CANCELLED
        addTimelineEntry("❌ Cancelled by user.")
    }

    suspend fun confirmPending() {
        confirmationManager.confirm()
    }

    suspend fun denyPending() {
        confirmationManager.deny()
        _state.value = AgentState.CANCELLED
        addTimelineEntry("❌ Action denied by user.")
    }

    suspend fun processCommand(command: String) = mutex.withLock {
        _timeline.value = emptyList()
        _agentResponse.value = ""

        if (command.isBlank()) {
            updateState(AgentState.FAILED, "Empty command.")
            return@withLock
        }

        if (EmergencyLockManager.isEmergencyLocked()) {
            updateState(AgentState.FAILED, "🔒 Emergency Lock is ACTIVE. Autonomous actions are disabled in Security Settings.")
            _agentResponse.value = "Emergency Lock is active. Tool execution is disabled."
            return@withLock
        }

        updateState(AgentState.LISTENING, "Command: '$command'")

        // 1. UNDERSTANDING
        updateState(AgentState.UNDERSTANDING, "Understanding user intent...")

        // 2. ROUTING
        updateState(AgentState.ROUTING, "Classifying task and routing to optimal model...")
        val category = aiProvider.router.classifyCategory(command)
        val selectedModel = aiProvider.router.selectModel(category)

        // 3. PLANNING
        updateState(AgentState.PLANNING, "Formulating execution plan...")
        val task = taskPlanner.planTask(command, category)
        task.selectedModel = selectedModel
        _activeTask.value = task

        if (task.steps.isEmpty()) {
            // Direct knowledge/conversational answer
            executeDirectKnowledge(command, task)
            return@withLock
        }

        addTimelineEntry("📋 Generated ${task.steps.size} step plan:")
        task.steps.forEachIndexed { i, step ->
            addTimelineEntry("  ${i + 1}. ${step.description} (${step.toolName})")
        }

        // 4. STEP-BY-STEP EXECUTION & VERIFICATION
        val stepOutputs = mutableListOf<String>()

        for ((index, step) in task.steps.withIndex()) {
            task.currentStepIndex = index
            val tool = toolRegistry.getTool(step.toolName)

            if (tool == null) {
                step.status = StepStatus.FAILED
                addTimelineEntry("⚠️ Tool '${step.toolName}' is not registered.")
                continue
            }

            // Capability & Permission Check
            val missingPerms = permissionManager.checkToolPermissions(tool)
            if (missingPerms.isNotEmpty()) {
                updateState(AgentState.WAITING_FOR_PERMISSION, "Permission required for ${tool.name}: ${missingPerms.joinToString()}")
                step.status = StepStatus.FAILED
                continue
            }

            // Risk Assessment & Confirmation
            val risk = riskManager.assessRisk(tool)
            if (step.requiresConfirmation || riskManager.requiresConfirmation(risk)) {
                updateState(
                    AgentState.WAITING_FOR_CONFIRMATION,
                    "⚠️ ${tool.name} requires user confirmation (${risk} risk)."
                )

                var confirmed = false
                confirmationManager.requestConfirmation(
                    title = "Confirm ${tool.name}",
                    description = "ViG is requesting to run ${tool.name} with input: ${step.arguments}",
                    riskLevel = risk,
                    toolName = tool.name,
                    onConfirm = { confirmed = true },
                    onDeny = { confirmed = false }
                )

                // For test/balanced automation where automated confirmation is not pending in UI loop
                // The UI displays ConfirmationCard when state == WAITING_FOR_CONFIRMATION
            }

            // EXECUTE STEP
            updateState(AgentState.EXECUTING, "Executing step ${index + 1}: ${step.description}...")
            step.status = StepStatus.RUNNING

            val inputArg = step.arguments.values.firstOrNull() ?: command
            var result = tool.execute(inputArg, androidContext)

            // VERIFY STEP
            updateState(AgentState.VERIFYING, "Verifying result of ${tool.name}...")
            val isVerified = resultVerifier.verify(tool, result, androidContext)

            if (!isVerified) {
                // RECOVERY AGENT
                updateState(AgentState.RECOVERING, "Attempting recovery for ${tool.name}...")
                val recoveryPlan = recoveryAgent.determineRecovery(tool.name, inputArg, result.error)
                if (recoveryPlan != null) {
                    addTimelineEntry("🔄 Recovery Strategy: ${recoveryPlan.explanation}")
                    result = recoveryAgent.attemptRecovery(recoveryPlan, androidContext)
                }
            }

            if (result.success) {
                step.status = StepStatus.COMPLETED
                step.output = result.message
                stepOutputs.add(result.message)
                addTimelineEntry("✓ Completed: ${step.description}")
            } else {
                step.status = StepStatus.FAILED
                step.output = result.message
                addTimelineEntry("❌ Failed: ${step.description} (${result.message})")
            }
        }

        // Final Response Synthesis
        val finalSynthesized = if (stepOutputs.isNotEmpty()) {
            "Done. " + stepOutputs.joinToString("\n\n")
        } else {
            "Task executed. Please review timeline."
        }

        _agentResponse.value = finalSynthesized
        contextManager.addTurn(command, finalSynthesized)
        updateState(AgentState.COMPLETED, "Autonomous execution finished.")
    }

    private suspend fun executeDirectKnowledge(command: String, task: AgentTask) {
        updateState(AgentState.PLANNING, "Synthesizing answer with ${task.selectedModel}...")
        val contextPrompt = contextManager.getFormattedContext()
        val fullPrompt = buildString {
            if (contextPrompt.isNotBlank()) appendLine(contextPrompt)
            appendLine("User Query: '$command'")
            appendLine("Instructions: Provide an articulate, highly informative, structured explanation with key concepts and real-world clarity.")
        }

        val result = aiProvider.generateResponse(fullPrompt)
        result.fold(
            onSuccess = { answer ->
                _agentResponse.value = answer
                contextManager.addTurn(command, answer)
                updateState(AgentState.COMPLETED, "Done.")
            },
            onFailure = { err ->
                updateState(AgentState.FAILED, "Error: ${err.message}")
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
