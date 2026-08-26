package com.example.vig.presentation.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vig.presentation.theme.*
import com.example.vig.presentation.components.tealBeigeCard
import com.example.vig.presentation.components.tealGlowingButton

@Composable
fun HomeScreen(onNavigateToAgent: (startVoice: Boolean) -> Unit, onNavigateToSettings: () -> Unit) {
    val scrollState = rememberScrollState()
    
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) onNavigateToAgent(true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBeige)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Top Bar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(GlowingTeal, androidx.compose.foundation.shape.CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ViG", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DarkEspresso)
                }
                
                Box(
                    modifier = Modifier.tealBeigeCard(elevation = 4.dp, cornerRadius = 16.dp, containerColor = LightCream).clickable { onNavigateToSettings() }
                ) {
                    Text("Settings", color = DarkEspresso, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Good Evening,", style = MaterialTheme.typography.headlineMedium, color = WarmBrown, fontWeight = FontWeight.Normal)
            Text("Vignesh", style = MaterialTheme.typography.headlineLarge, color = DarkEspresso, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp)
            Text("What would you like me to do?", style = MaterialTheme.typography.bodyLarge, color = WarmBrown, modifier = Modifier.padding(top = 4.dp))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Hero Card in Dark Teal with Glowing Accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .tealBeigeCard(elevation = 12.dp, cornerRadius = 28.dp, containerColor = DarkTealSurface)
                    .clickable { onNavigateToAgent(false) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ask ViG anything...", color = GlowingTeal, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your Personal Intelligence Companion", color = LightCream.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            Text("QUICK SHORTCUTS", style = MaterialTheme.typography.labelSmall, color = WarmBrown, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                TealShortcutChip(title = "??? Plan My Day", modifier = Modifier.weight(1f)) { onNavigateToAgent(false) }
                TealShortcutChip(title = "?? Read PDF", modifier = Modifier.weight(1f)) { onNavigateToAgent(false) }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                TealShortcutChip(title = "?? Search Web", modifier = Modifier.weight(1f)) { onNavigateToAgent(false) }
                TealShortcutChip(title = "?? Explain Topic", modifier = Modifier.weight(1f)) { onNavigateToAgent(false) }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Floating Glowing Dark Teal Voice Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(28.dp)
                .size(72.dp)
                .tealGlowingButton(cornerRadius = 36.dp, color = DarkTealSurface)
                .clickable { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            contentAlignment = Alignment.Center
        ) {
            Text("??", fontSize = 32.sp)
        }
    }
}

@Composable
fun TealShortcutChip(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .tealBeigeCard(elevation = 4.dp, cornerRadius = 20.dp, containerColor = LightCream)
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = DarkEspresso, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
