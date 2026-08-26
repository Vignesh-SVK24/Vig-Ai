package com.example.vig.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.vig.presentation.theme.*
import com.example.vig.security.KeyStoreManager
import com.example.vig.presentation.components.tealBeigeCard
import com.example.vig.presentation.components.tealGlowingButton
import com.example.vig.voice.VoiceManager

@Composable
fun SettingsScreen(keyStoreManager: KeyStoreManager, voiceManager: VoiceManager, onNavigateBack: () -> Unit) {
    var apiKey by remember { mutableStateOf(keyStoreManager.getApiKey() ?: "") }
    var selectedProvider by remember { mutableStateOf(keyStoreManager.getProvider()) }
    var showKey by remember { mutableStateOf(false) }
    var autoSpeak by remember { mutableStateOf(voiceManager.autoSpeakResponses) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBeige)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Preferences", style = MaterialTheme.typography.headlineMedium, color = DarkEspresso, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            
            Box(modifier = Modifier.fillMaxWidth().tealBeigeCard(elevation = 8.dp, cornerRadius = 24.dp, containerColor = DarkTealSurface).padding(24.dp)) {
                Column {
                    Text("AI MODEL PROVIDER", style = MaterialTheme.typography.labelSmall, color = GlowingTeal, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProviderChip(title = "Gemini", isSelected = selectedProvider == "gemini") { selectedProvider = "gemini" }
                        ProviderChip(title = "GPT-4o", isSelected = selectedProvider == "openai") { selectedProvider = "openai" }
                        ProviderChip(title = "Claude 3.5", isSelected = selectedProvider == "claude") { selectedProvider = "claude" }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = apiKey, onValueChange = { apiKey = it },
                        label = { Text("API Key", color = LightCream) },
                        placeholder = { 
                            Text(
                                when(selectedProvider) {
                                    "openai" -> "sk-..."
                                    "claude" -> "sk-ant-..."
                                    else -> "AIzaSy... or AQ.Ab8..."
                                },
                                color = LightCream.copy(alpha = 0.5f)
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth().background(DarkTealBase, androidx.compose.foundation.shape.RoundedCornerShape(16.dp)),
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

            Box(modifier = Modifier.fillMaxWidth().tealBeigeCard(elevation = 8.dp, cornerRadius = 24.dp, containerColor = DarkTealSurface).padding(24.dp)) {
                Column {
                    Text("VOICE & REASONING", style = MaterialTheme.typography.labelSmall, color = GlowingTeal, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Chain-of-Thought System Prompting: ACTIVE", color = GlowingTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Spoken Voice Responses", color = LightCream, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        Switch(
                            checked = autoSpeak,
                            onCheckedChange = { autoSpeak = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = LightCream, checkedTrackColor = GlowingTeal)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.weight(1f).height(52.dp).tealBeigeCard(cornerRadius = 20.dp, containerColor = SoftSand).clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) { Text("Cancel", color = DarkEspresso, fontWeight = FontWeight.Bold) }
                
                Box(
                    modifier = Modifier.weight(1f).height(52.dp).tealGlowingButton(cornerRadius = 20.dp, color = DarkTealSurface).clickable {
                        keyStoreManager.saveApiKey(apiKey)
                        keyStoreManager.saveProvider(selectedProvider)
                        voiceManager.autoSpeakResponses = autoSpeak
                        onNavigateBack()
                    },
                    contentAlignment = Alignment.Center
                ) { Text("Save Config", color = GlowingTeal, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun ProviderChip(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) GlowingTeal else DarkTealBase,
                androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = if (isSelected) DarkTealBase else LightCream, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
