package com.example.vig

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vig.agent.core.AgentOrchestrator
import com.example.vig.agent.provider.MultiAIProvider
import com.example.vig.presentation.agent.AgentScreen
import com.example.vig.presentation.home.HomeScreen
import com.example.vig.presentation.settings.SettingsScreen
import com.example.vig.presentation.theme.WarmBeige
import com.example.vig.presentation.theme.VigTheme
import com.example.vig.security.KeyStoreManager
import com.example.vig.tools.DefaultToolRegistry
import com.example.vig.voice.AndroidSpeechToText
import com.example.vig.voice.AndroidTextToSpeech
import com.example.vig.voice.VoiceManager

class MainActivity : ComponentActivity() {
    private lateinit var keyStoreManager: KeyStoreManager
    private lateinit var orchestrator: AgentOrchestrator
    private lateinit var voiceManager: VoiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        keyStoreManager = KeyStoreManager(this)
        val aiProvider = MultiAIProvider(keyStoreManager)
        val toolRegistry = DefaultToolRegistry()
        
        orchestrator = AgentOrchestrator(
            aiProvider = aiProvider,
            toolRegistry = toolRegistry,
            androidContext = this
        )

        val stt = AndroidSpeechToText(this)
        val tts = AndroidTextToSpeech(this)
        voiceManager = VoiceManager(stt, tts, orchestrator)

        setContent {
            VigTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = WarmBeige) {
                    VigApp(keyStoreManager, orchestrator, voiceManager)
                }
            }
        }
    }

    override fun onDestroy() {
        voiceManager.destroy()
        super.onDestroy()
    }
}

@Composable
fun VigApp(keyStoreManager: KeyStoreManager, orchestrator: AgentOrchestrator, voiceManager: VoiceManager) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                orchestrator = orchestrator,
                onNavigateToAgent = { startVoice -> 
                    if (startVoice) voiceManager.startListening()
                    navController.navigate("agent")
                },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("agent") {
            AgentScreen(
                orchestrator = orchestrator,
                voiceManager = voiceManager,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                keyStoreManager = keyStoreManager,
                voiceManager = voiceManager,
                orchestrator = orchestrator,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
