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
import com.example.vig.agent.manager.EmergencyLockManager
import com.example.vig.domain.models.AutonomyLevel
import com.example.vig.presentation.theme.*
import com.example.vig.security.KeyStoreManager
import com.example.vig.presentation.components.tealBeigeCard
import com.example.vig.presentation.components.tealGlowingButton
import com.example.vig.voice.VoiceManager

@Composable
fun SettingsScreen(
    keyStoreManager: KeyStoreManager,
    voiceManager: VoiceManager,
    orchestrator: AgentOrchestrator,
    onNavigateBack: () -> Unit
) {
    var apiKey by remember { mutableStateOf(keyStoreManager.getApiKey() ?: "") }
    var selectedProvider by remember { mutableStateOf(keyStoreManager.getProvider()) }
    var currentAutonomy by remember { mutableStateOf(orchestrator.riskManager.autonomyLevel) }
    var showKey by remember { mutableStateOf(false) }
    var autoSpeak by remember { mutableStateOf(voiceManager.autoSpeakResponses) }
    val isEmergencyLocked by EmergencyLockManager.isLocked.collectAsState()
    val scrollState = rememberScrollState()

    val capabilities = remember { orchestrator.capabilityManager.getCapabilitiesReport() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBeige)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Settings & Autonomy", style = MaterialTheme.typography.headlineMedium, color = DarkEspresso, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            
            // 1. EMERGENCY LOCK CARD
            Box(modifier = Modifier.fillMaxWidth().tealBeigeCard(elevation = 8.dp, cornerRadius = 22.dp, containerColor = if (isEmergencyLocked) MutedRed else DarkTealSurface).padding(18.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("EMERGENCY LOCK", style = MaterialTheme.typography.labelSmall, color = if (isEmergencyLocked) Color.White else GlowingTeal, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = isEmergencyLocked,
                            onCheckedChange = {
                                if (it) EmergencyLockManager.lock() else EmergencyLockManager.unlock()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = LightCream, checkedTrackColor = MutedRed)
                        )
                    }
                    Text(
                        if (isEmergencyLocked) "Active: All autonomous tool executions are blocked."
                        else "Inactive: Autonomous tool actions execute based on autonomy level.",
                        color = LightCream.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. AUTONOMY LEVEL SELECTOR
            Box(modifier = Modifier.fillMaxWidth().tealBeigeCard(elevation = 6.dp, cornerRadius = 22.dp, containerColor = LightCream).padding(18.dp)) {
                Column {
                    Text("AUTONOMY LEVEL", style = MaterialTheme.typography.labelSmall, color = DarkTealSurface, letterSpacing = 1.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        AutonomyChip("ASSISTED", currentAutonomy == AutonomyLevel.ASSISTED, Modifier.weight(1f)) {
                            currentAutonomy = AutonomyLevel.ASSISTED
                            orchestrator.riskManager.autonomyLevel = AutonomyLevel.ASSISTED
                        }
                        AutonomyChip("BALANCED", currentAutonomy == AutonomyLevel.BALANCED, Modifier.weight(1f)) {
                            currentAutonomy = AutonomyLevel.BALANCED
                            orchestrator.riskManager.autonomyLevel = AutonomyLevel.BALANCED
                        }
                        AutonomyChip("STRICT", currentAutonomy == AutonomyLevel.STRICT, Modifier.weight(1f)) {
                            currentAutonomy = AutonomyLevel.STRICT
                            orchestrator.riskManager.autonomyLevel = AutonomyLevel.STRICT
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        when (currentAutonomy) {
                            AutonomyLevel.ASSISTED -> "Assisted: ViG prompts for confirmation before most device/tool actions."
                            AutonomyLevel.BALANCED -> "Balanced (Default): Low-risk actions execute automatically; high-risk actions require confirmation."
                            AutonomyLevel.STRICT -> "Strict: ViG requires explicit user approval before every single external action."
                        },
                        color = WarmBrown,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. AI MODEL ROUTER & PROVIDER
            Box(modifier = Modifier.fillMaxWidth().tealBeigeCard(elevation = 8.dp, cornerRadius = 22.dp, containerColor = DarkTealSurface).padding(18.dp)) {
                Column {
                    Text("AI MODEL ROUTER", style = MaterialTheme.typography.labelSmall, color = GlowingTeal, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        ModelChip("Auto", selectedProvider == "auto", Modifier.weight(1f)) { selectedProvider = "auto" }
                        ModelChip("Gemini", selectedProvider == "gemini", Modifier.weight(1f)) { selectedProvider = "gemini" }
                        ModelChip("GPT-4o", selectedProvider == "openai", Modifier.weight(1f)) { selectedProvider = "openai" }
                        ModelChip("Claude", selectedProvider == "claude", Modifier.weight(1f)) { selectedProvider = "claude" }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    
                    OutlinedTextField(
                        value = apiKey, onValueChange = { apiKey = it },
                        label = { Text("API Key", color = LightCream) },
                        placeholder = { Text("Paste key for selected model...", color = LightCream.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth().background(DarkTealBase, RoundedCornerShape(14.dp)),
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = GlowingTeal, unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = LightCream, unfocusedTextColor = LightCream
                        )
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showKey = !showKey }) { Text(if (showKey) "Mask Key" else "Reveal Key", color = GlowingTeal) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. DEVICE CAPABILITIES REPORT
            Box(modifier = Modifier.fillMaxWidth().tealBeigeCard(elevation = 6.dp, cornerRadius = 22.dp, containerColor = LightCream).padding(18.dp)) {
                Column {
                    Text("DEVICE CAPABILITIES & PERMISSIONS", style = MaterialTheme.typography.labelSmall, color = DarkTealSurface, letterSpacing = 1.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    capabilities.forEach { (name, status) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, fontSize = 12.sp, color = DarkEspresso, fontWeight = FontWeight.Medium)
                            Text(status, fontSize = 11.sp, color = if (status.contains("AVAILABLE")) DarkTealSurface else MutedRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. VOICE RESPONSE TOGGLE
            Box(modifier = Modifier.fillMaxWidth().tealBeigeCard(elevation = 6.dp, cornerRadius = 22.dp, containerColor = LightCream).padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Spoken Voice Responses", color = DarkEspresso, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("ViG speaks final results using Text-to-Speech.", color = WarmBrown, fontSize = 12.sp)
                    }
                    Switch(
                        checked = autoSpeak,
                        onCheckedChange = { autoSpeak = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = LightCream, checkedTrackColor = DarkTealSurface)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.weight(1f).height(50.dp).tealBeigeCard(cornerRadius = 18.dp, containerColor = SoftSand).clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) { Text("Back", color = DarkEspresso, fontWeight = FontWeight.Bold) }
                
                Box(
                    modifier = Modifier.weight(1f).height(50.dp).tealGlowingButton(cornerRadius = 18.dp, color = DarkTealSurface).clickable {
                        keyStoreManager.saveApiKey(apiKey)
                        keyStoreManager.saveProvider(selectedProvider)
                        voiceManager.autoSpeakResponses = autoSpeak
                        onNavigateBack()
                    },
                    contentAlignment = Alignment.Center
                ) { Text("Save Config", color = GlowingTeal, fontWeight = FontWeight.Bold) }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun AutonomyChip(title: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(
                if (isSelected) DarkTealSurface else SoftSand,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = if (isSelected) GlowingTeal else DarkEspresso, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
fun ModelChip(title: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(
                if (isSelected) GlowingTeal else DarkTealBase,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = if (isSelected) DarkTealBase else LightCream, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}
