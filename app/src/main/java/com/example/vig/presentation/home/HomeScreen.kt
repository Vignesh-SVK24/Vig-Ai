package com.example.vig.presentation.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vig.agent.core.AgentOrchestrator
import com.example.vig.domain.models.AgentState
import com.example.vig.presentation.theme.*
import com.example.vig.presentation.components.tealBeigeCard
import com.example.vig.presentation.components.tealGlowingButton
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    orchestrator: AgentOrchestrator,
    onNavigateToAgent: (startVoice: Boolean) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val agentState by orchestrator.state.collectAsState()
    val activeTask by orchestrator.activeTask.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) onNavigateToAgent(true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBeige)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(44.dp))
            
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(GlowingTeal, androidx.compose.foundation.shape.CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ViG", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = DarkEspresso)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AGENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GlowingTeal, modifier = Modifier.background(DarkTealSurface, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                }
                
                Box(
                    modifier = Modifier.tealBeigeCard(elevation = 4.dp, cornerRadius = 14.dp, containerColor = LightCream).clickable { onNavigateToSettings() }
                ) {
                    Text("Settings", color = DarkEspresso, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            
            Text("Good Evening,", style = MaterialTheme.typography.headlineMedium, color = WarmBrown, fontWeight = FontWeight.Normal)
            Text("Vignesh", style = MaterialTheme.typography.headlineLarge, color = DarkEspresso, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
            Text("What would you like me to accomplish?", style = MaterialTheme.typography.bodyLarge, color = WarmBrown, modifier = Modifier.padding(top = 4.dp))
            
            Spacer(modifier = Modifier.height(24.dp))

            // Active / Recent Task Card
            if (activeTask != null && agentState != AgentState.IDLE) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tealBeigeCard(elevation = 8.dp, cornerRadius = 22.dp, containerColor = LightCream)
                        .clickable { onNavigateToAgent(false) }
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ACTIVE GOAL", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = DarkTealSurface, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(agentState.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GlowingTeal, modifier = Modifier.background(DarkTealBase, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(activeTask!!.originalCommand, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkEspresso)
                        if (activeTask!!.steps.isNotEmpty()) {
                            Text("Progress: ${activeTask!!.currentStepIndex + 1}/${activeTask!!.steps.size} steps", fontSize = 12.sp, color = WarmBrown)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Hero Command Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .tealBeigeCard(elevation = 10.dp, cornerRadius = 26.dp, containerColor = DarkTealSurface)
                    .clickable { onNavigateToAgent(false) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Command ViG...", color = GlowingTeal, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("One command · Understand · Plan · Act · Verify", color = LightCream.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            
            Text("AUTONOMOUS SHORTCUTS", style = MaterialTheme.typography.labelSmall, color = WarmBrown, letterSpacing = 1.5.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeShortcutChip(title = "🗓️ Plan My Day", modifier = Modifier.weight(1f)) {
                    onNavigateToAgent(false)
                    scope.launch { orchestrator.processCommand("Plan my study session and day schedule") }
                }
                HomeShortcutChip(title = "📄 Read AI PDF", modifier = Modifier.weight(1f)) {
                    onNavigateToAgent(false)
                    scope.launch { orchestrator.processCommand("Find my AI assignment PDF and summarize chapter 2") }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeShortcutChip(title = "🧠 Quiz Me", modifier = Modifier.weight(1f)) {
                    onNavigateToAgent(false)
                    scope.launch { orchestrator.processCommand("Generate 10 quiz questions on Artificial Intelligence") }
                }
                HomeShortcutChip(title = "🌐 Search Web", modifier = Modifier.weight(1f)) {
                    onNavigateToAgent(false)
                    scope.launch { orchestrator.processCommand("Search the web for AI agent frameworks and compare them") }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeShortcutChip(title = "⚡ Open YouTube", modifier = Modifier.weight(1f)) {
                    onNavigateToAgent(false)
                    scope.launch { orchestrator.processCommand("Open YouTube") }
                }
                HomeShortcutChip(title = "📞 Call Contact", modifier = Modifier.weight(1f)) {
                    onNavigateToAgent(false)
                    scope.launch { orchestrator.processCommand("Call Dad") }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Floating Voice Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(68.dp)
                .tealGlowingButton(cornerRadius = 34.dp, color = DarkTealSurface)
                .clickable { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            contentAlignment = Alignment.Center
        ) {
            Text("🎤", fontSize = 30.sp)
        }
    }
}

@Composable
fun HomeShortcutChip(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .tealBeigeCard(elevation = 4.dp, cornerRadius = 18.dp, containerColor = LightCream)
            .clickable { onClick() }
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = DarkEspresso, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
