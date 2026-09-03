package com.example.vig.presentation.agent

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vig.agent.core.AgentOrchestrator
import com.example.vig.agent.manager.EmergencyLockManager
import com.example.vig.domain.models.AgentState
import com.example.vig.domain.models.VoiceState
import com.example.vig.presentation.theme.*
import com.example.vig.presentation.components.tealBeigeCard
import com.example.vig.presentation.components.tealGlowingButton
import com.example.vig.voice.VoiceManager
import kotlinx.coroutines.launch

data class MessageBubble(val text: String, val isUser: Boolean)

@Composable
fun AgentScreen(orchestrator: AgentOrchestrator, voiceManager: VoiceManager) {
    val state by orchestrator.state.collectAsState()
    val agentResponse by orchestrator.agentResponse.collectAsState()
    val timeline by orchestrator.timeline.collectAsState()
    val activeTask by orchestrator.activeTask.collectAsState()
    val voiceState by voiceManager.voiceState.collectAsState()
    val sttTranscription by voiceManager.transcription.collectAsState()
    val pendingConfirmation by orchestrator.confirmationManager.pendingConfirmation.collectAsState()
    val isEmergencyLocked by EmergencyLockManager.isLocked.collectAsState()

    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf(MessageBubble("Hello Vignesh. ViG is ready for your autonomous commands.", false)) }
    val listState = rememberLazyListState()

    val isProcessing = state != AgentState.IDLE && state != AgentState.COMPLETED &&
            state != AgentState.FAILED && state != AgentState.CANCELLED ||
            voiceState == VoiceState.PROCESSING_AUDIO

    LaunchedEffect(sttTranscription) {
        if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.PROCESSING_AUDIO) {
            input = sttTranscription
        }
    }

    LaunchedEffect(agentResponse) {
        if (agentResponse.isNotBlank()) {
            messages.add(MessageBubble(agentResponse, false))
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) voiceManager.startListening()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBeige)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // EMERGENCY LOCK BANNER
        if (isEmergencyLocked) {
            Surface(
                color = MutedRed,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    text = "🔒 EMERGENCY LOCK ACTIVE — Tool executions disabled",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // VIG GLOWING TEAL AI CORE HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .tealBeigeCard(elevation = 10.dp, cornerRadius = 28.dp, containerColor = DarkTealSurface)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val displayState = when {
                    voiceState == VoiceState.LISTENING -> AgentState.LISTENING
                    voiceState == VoiceState.SPEAKING -> AgentState.EXECUTING
                    voiceState == VoiceState.ERROR -> AgentState.FAILED
                    else -> state
                }
                ViGCore(state = displayState)
                
                Spacer(modifier = Modifier.height(6.dp))
                
                val statusText = when (state) {
                    AgentState.IDLE -> if (voiceState == VoiceState.LISTENING) "Listening..." else "Ready"
                    AgentState.LISTENING -> "Listening..."
                    AgentState.UNDERSTANDING -> "Understanding Intent..."
                    AgentState.ROUTING -> "Routing to Model..."
                    AgentState.PLANNING -> "Formulating Plan..."
                    AgentState.WAITING_FOR_PERMISSION -> "Waiting for Permission..."
                    AgentState.WAITING_FOR_CONFIRMATION -> "Awaiting Approval..."
                    AgentState.EXECUTING -> "Executing Tools..."
                    AgentState.VERIFYING -> "Verifying Result..."
                    AgentState.RECOVERING -> "Recovering with Alternative..."
                    AgentState.SPEAKING -> "Speaking Response..."
                    AgentState.COMPLETED -> "Task Completed ✓"
                    AgentState.FAILED -> "Action Failed"
                    AgentState.CANCELLED -> "Cancelled by User"
                    else -> state.name
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = statusText,
                        color = if (state == AgentState.FAILED) MutedRed else GlowingTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (isProcessing) {
                        Surface(
                            color = MutedRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { orchestrator.cancel() }
                        ) {
                            Text(
                                text = "Cancel",
                                color = MutedRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // CONFIRMATION DIALOG / CARD IF PENDING
        pendingConfirmation?.let { conf ->
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .tealBeigeCard(elevation = 12.dp, cornerRadius = 20.dp, containerColor = LightCream)
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚠️ USER CONFIRMATION REQUIRED",
                            color = MutedRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${conf.riskLevel} RISK",
                            color = DarkEspresso,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = conf.title, color = DarkEspresso, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = conf.description, color = WarmBrown, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { scope.launch { orchestrator.denyPending() } },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftSand),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reject", color = DarkEspresso, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { scope.launch { orchestrator.confirmPending() } },
                            colors = ButtonDefaults.buttonColors(containerColor = MutedRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Approve", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // CHAT CONVERSATION & TIMELINE DISPLAY
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .tealBeigeCard(elevation = 6.dp, cornerRadius = 24.dp, containerColor = LightCream)
                .padding(12.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Multi-step Execution Timeline (if active or recently populated)
                if (timeline.isNotEmpty()) {
                    item {
                        Surface(
                            color = DarkTealBase.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "⚡ AGENT EXECUTION TIMELINE",
                                    color = DarkTealSurface,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                timeline.forEach { entry ->
                                    Text(
                                        text = entry,
                                        color = DarkEspresso,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }

                items(messages) { msg ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (msg.isUser) DarkTealSurface else SoftSand,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = if (msg.isUser) GlowingTeal else DarkEspresso,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // INPUT & VOICE CONTROLS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .tealBeigeCard(elevation = 8.dp, cornerRadius = 24.dp, containerColor = LightCream)
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                // Mic Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .tealGlowingButton(
                            isPressed = voiceState == VoiceState.LISTENING,
                            cornerRadius = 24.dp,
                            color = if (voiceState == VoiceState.LISTENING) MutedRed else DarkTealSurface
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    tryAwaitRelease()
                                    voiceManager.stopListening()
                                },
                                onTap = {
                                    if (voiceState == VoiceState.LISTENING) voiceManager.stopListening()
                                    else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎤", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Command ViG...", fontSize = 13.sp, color = WarmBrown) },
                    modifier = Modifier.weight(1f).background(SoftSand, RoundedCornerShape(16.dp)),
                    enabled = !isProcessing,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = DarkEspresso,
                        unfocusedTextColor = DarkEspresso
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .height(46.dp)
                        .tealGlowingButton(isPressed = isProcessing || input.isBlank(), color = DarkTealSurface)
                        .clickable(enabled = !isProcessing && input.isNotBlank()) {
                            val cmd = input.trim()
                            messages.add(MessageBubble(cmd, true))
                            input = ""
                            scope.launch {
                                orchestrator.processCommand(cmd)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Act", color = GlowingTeal, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp))
                }
            }
        }
    }
}
