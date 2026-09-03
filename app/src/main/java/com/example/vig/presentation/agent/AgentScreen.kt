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
import com.example.vig.domain.models.AgentState
import com.example.vig.domain.models.VoiceState
import com.example.vig.presentation.theme.*
import com.example.vig.presentation.components.tealBeigeCard
import com.example.vig.presentation.components.tealGlowingButton
import com.example.vig.voice.VoiceManager
import kotlinx.coroutines.launch

data class MessageBubble(val text: String, val isUser: Boolean, val isError: Boolean = false)

@Composable
fun AgentScreen(
    orchestrator: AgentOrchestrator,
    voiceManager: VoiceManager,
    onNavigateToSettings: () -> Unit = {}
) {
    val state by orchestrator.state.collectAsState()
    val agentResponse by orchestrator.agentResponse.collectAsState()
    val voiceState by voiceManager.voiceState.collectAsState()
    val sttTranscription by voiceManager.transcription.collectAsState()

    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf(MessageBubble("Hello Vignesh. ViG is ready. How can I help you today?", false)) }
    val listState = rememberLazyListState()

    val isConnected = orchestrator.isAIConfigured()
    val isProcessing = state == AgentState.LISTENING ||
            state == AgentState.UNDERSTANDING ||
            state == AgentState.PLANNING ||
            state == AgentState.EXECUTING ||
            voiceState == VoiceState.PROCESSING_AUDIO

    LaunchedEffect(sttTranscription) {
        if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.PROCESSING_AUDIO) {
            input = sttTranscription
        }
    }

    LaunchedEffect(agentResponse) {
        if (agentResponse.isNotBlank()) {
            val isErr = state == AgentState.FAILED
            messages.add(MessageBubble(agentResponse, false, isError = isErr))
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

        // SETUP CARD IF GEMINI IS NOT CONNECTED
        if (!isConnected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .tealBeigeCard(elevation = 8.dp, cornerRadius = 20.dp, containerColor = LightCream)
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Gemini isn't connected yet.",
                            color = DarkEspresso,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Connect your API key to chat with ViG.",
                            color = WarmBrown,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = onNavigateToSettings,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkTealSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Connect Gemini", color = GlowingTeal, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
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
                
                val statusText = when {
                    voiceState == VoiceState.LISTENING -> "Listening to you..."
                    voiceState == VoiceState.SPEAKING -> "Responding..."
                    state == AgentState.LISTENING -> "Sending..."
                    state == AgentState.UNDERSTANDING -> "Thinking..."
                    state == AgentState.PLANNING -> "Responding..."
                    state == AgentState.EXECUTING -> "Working..."
                    state == AgentState.COMPLETED -> "Ready"
                    state == AgentState.FAILED -> if (!isConnected) "Not Connected" else "Error"
                    else -> "Ready"
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

        Spacer(modifier = Modifier.height(10.dp))

        // CHAT CONVERSATION AREA
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
                items(messages) { msg ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when {
                                msg.isUser -> DarkTealSurface
                                msg.isError -> MutedRed.copy(alpha = 0.15f)
                                else -> SoftSand
                            },
                            modifier = Modifier.widthIn(max = 290.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = when {
                                    msg.isUser -> GlowingTeal
                                    msg.isError -> MutedRed
                                    else -> DarkEspresso
                                },
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
                    placeholder = { Text("Ask ViG anything...", fontSize = 13.sp, color = WarmBrown) },
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
                    Text("Send", color = GlowingTeal, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp))
                }
            }
        }
    }
}
