package com.example.vig.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vig.agent.core.AgentOrchestrator
import com.example.vig.presentation.theme.*
import com.example.vig.security.KeyStoreManager
import com.example.vig.presentation.components.tealBeigeCard
import com.example.vig.presentation.components.tealGlowingButton
import com.example.vig.voice.VoiceManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    keyStoreManager: KeyStoreManager,
    voiceManager: VoiceManager,
    orchestrator: AgentOrchestrator,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var geminiKey by remember { mutableStateOf(keyStoreManager.getGeminiKey() ?: "") }
    var isGeminiConnected by remember { mutableStateOf(keyStoreManager.isGeminiConfigured()) }
    var showGeminiKey by remember { mutableStateOf(false) }

    var validationStatus by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var isValidatingSuccess by remember { mutableStateOf(false) }

    var selectedProvider by remember { mutableStateOf(keyStoreManager.getProvider()) }
    var autoSpeak by remember { mutableStateOf(voiceManager.autoSpeakResponses) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBeige)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = DarkEspresso,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .tealBeigeCard(elevation = 2.dp, cornerRadius = 12.dp, containerColor = SoftSand)
                        .clickable { onNavigateBack() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Done", color = DarkEspresso, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. PRIMARY AI PROVIDER: GEMINI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .tealBeigeCard(elevation = 8.dp, cornerRadius = 24.dp, containerColor = DarkTealSurface)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "PRIMARY AI MODEL",
                                style = MaterialTheme.typography.labelSmall,
                                color = GlowingTeal,
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Google Gemini",
                                style = MaterialTheme.typography.titleLarge,
                                color = LightCream,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Connection Status Badge
                        Surface(
                            color = if (isGeminiConnected) GlowingTeal.copy(alpha = 0.2f) else SoftSand.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (isGeminiConnected) "Connected ✓" else "Not Connected",
                                color = if (isGeminiConnected) GlowingTeal else LightCream.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        "Gemini API Key",
                        color = LightCream.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = geminiKey,
                        onValueChange = {
                            geminiKey = it
                            validationStatus = null
                        },
                        placeholder = { Text("Paste your Gemini API Key...", color = LightCream.copy(alpha = 0.4f), fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkTealBase, RoundedCornerShape(14.dp)),
                        visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = GlowingTeal,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = LightCream,
                            unfocusedTextColor = LightCream
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showGeminiKey = !showGeminiKey }) {
                            Text(
                                if (showGeminiKey) "Mask Key" else "Reveal Key",
                                color = GlowingTeal,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Validation Feedback Text
                    validationStatus?.let { status ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = status,
                            color = if (isValidatingSuccess) GlowingTeal else MutedRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons: Test Connection, Save, Remove Key
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Test Connection
                        Button(
                            onClick = {
                                if (geminiKey.isBlank()) {
                                    validationStatus = "Please enter an API key first."
                                    isValidatingSuccess = false
                                    return@Button
                                }
                                isTestingConnection = true
                                validationStatus = "Testing connection to Gemini..."
                                scope.launch {
                                    val testResult = orchestrator.aiProvider.geminiProvider.validateApiKey(geminiKey)
                                    isTestingConnection = false
                                    testResult.fold(
                                        onSuccess = { msg ->
                                            validationStatus = msg
                                            isValidatingSuccess = true
                                        },
                                        onFailure = { err ->
                                            validationStatus = err.message ?: "Invalid Gemini API key"
                                            isValidatingSuccess = false
                                        }
                                    )
                                }
                            },
                            enabled = !isTestingConnection && geminiKey.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftSand),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (isTestingConnection) "Testing..." else "Test",
                                color = DarkEspresso,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Save Key
                        Button(
                            onClick = {
                                if (geminiKey.isBlank()) {
                                    validationStatus = "API key cannot be blank."
                                    isValidatingSuccess = false
                                    return@Button
                                }
                                isTestingConnection = true
                                validationStatus = "Validating and saving..."
                                scope.launch {
                                    val testResult = orchestrator.aiProvider.geminiProvider.validateApiKey(geminiKey)
                                    isTestingConnection = false
                                    testResult.fold(
                                        onSuccess = { msg ->
                                            keyStoreManager.saveGeminiKey(geminiKey)
                                            keyStoreManager.saveProvider("gemini")
                                            isGeminiConnected = true
                                            validationStatus = "Gemini connected ✓"
                                            isValidatingSuccess = true
                                        },
                                        onFailure = { err ->
                                            validationStatus = err.message ?: "Invalid Gemini API key"
                                            isValidatingSuccess = false
                                        }
                                    )
                                }
                            },
                            enabled = !isTestingConnection && geminiKey.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = GlowingTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Save",
                                color = DarkTealBase,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }

                        // Remove Key
                        if (isGeminiConnected) {
                            Button(
                                onClick = {
                                    keyStoreManager.removeGeminiKey()
                                    geminiKey = ""
                                    isGeminiConnected = false
                                    validationStatus = "Gemini key removed."
                                    isValidatingSuccess = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MutedRed.copy(alpha = 0.8f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "Remove",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. OTHER PROVIDERS (OpenAI & Claude - Preserved Architecture)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .tealBeigeCard(elevation = 4.dp, cornerRadius = 20.dp, containerColor = LightCream)
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        "ADDITIONAL PROVIDERS (OPTIONAL)",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkTealSurface,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("OpenAI (GPT-4o)", color = DarkEspresso, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            if (keyStoreManager.isOpenAIConfigured()) "Connected" else "Not Connected",
                            color = if (keyStoreManager.isOpenAIConfigured()) DarkTealSurface else WarmBrown,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Anthropic (Claude 3.5)", color = DarkEspresso, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            if (keyStoreManager.isClaudeConfigured()) "Connected" else "Not Connected",
                            color = if (keyStoreManager.isClaudeConfigured()) DarkTealSurface else WarmBrown,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. VOICE SPEECH SETTINGS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .tealBeigeCard(elevation = 4.dp, cornerRadius = 20.dp, containerColor = LightCream)
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Spoken Responses (TTS)", color = DarkEspresso, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("ViG speaks Gemini's responses aloud.", color = WarmBrown, fontSize = 12.sp)
                    }
                    Switch(
                        checked = autoSpeak,
                        onCheckedChange = {
                            autoSpeak = it
                            voiceManager.autoSpeakResponses = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = LightCream, checkedTrackColor = DarkTealSurface)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
