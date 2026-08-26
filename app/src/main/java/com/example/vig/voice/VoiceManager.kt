package com.example.vig.voice

import com.example.vig.agent.core.AgentOrchestrator
import com.example.vig.domain.interfaces.SpeechToTextProvider
import com.example.vig.domain.interfaces.TextToSpeechProvider
import com.example.vig.domain.models.AgentState
import com.example.vig.domain.models.VoiceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceManager(
    private val stt: SpeechToTextProvider,
    private val tts: TextToSpeechProvider,
    private val orchestrator: AgentOrchestrator
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    val transcription: StateFlow<String> = stt.transcription
    val sttError: StateFlow<String?> = stt.error

    var autoSpeakResponses = true
    private var lastInitiatedByVoice = false

    init {
        // Observe STT completion
        scope.launch {
            stt.isListening.collect { listening ->
                if (!listening && stt.transcription.value.isNotBlank() && _voiceState.value == VoiceState.LISTENING) {
                    _voiceState.value = VoiceState.PROCESSING_AUDIO
                    val cmd = stt.transcription.value
                    lastInitiatedByVoice = true
                    // Execute in orchestrator
                    orchestrator.processCommand(cmd)
                }
            }
        }
        
        // Map Agent Orchestrator state to VoiceState visually and trigger TTS on completion
        scope.launch {
            orchestrator.state.collect { agentState ->
                if (!lastInitiatedByVoice) return@collect
                
                when (agentState) {
                    AgentState.UNDERSTANDING -> _voiceState.value = VoiceState.PROCESSING_AUDIO
                    AgentState.PLANNING -> _voiceState.value = VoiceState.PROCESSING_AUDIO
                    AgentState.EXECUTING, AgentState.VERIFYING -> _voiceState.value = VoiceState.PROCESSING_AUDIO
                    AgentState.FAILED -> {
                        _voiceState.value = VoiceState.ERROR
                        if (autoSpeakResponses) tts.speak("Action failed. Please check the logs.")
                        lastInitiatedByVoice = false
                    }
                    AgentState.COMPLETED -> {
                        _voiceState.value = VoiceState.SPEAKING
                        val response = orchestrator.agentResponse.value
                        if (autoSpeakResponses && response.isNotBlank()) {
                            tts.speak(response) {
                                _voiceState.value = VoiceState.IDLE
                                lastInitiatedByVoice = false
                            }
                        } else {
                            _voiceState.value = VoiceState.IDLE
                            lastInitiatedByVoice = false
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun startListening() {
        tts.stop()
        lastInitiatedByVoice = true
        _voiceState.value = VoiceState.LISTENING
        stt.startListening()
    }

    fun stopListening() {
        stt.stopListening()
    }

    fun cancelAll() {
        stt.cancel()
        tts.stop()
        orchestrator.cancel()
        _voiceState.value = VoiceState.IDLE
        lastInitiatedByVoice = false
    }

    fun destroy() {
        stt.destroy()
        tts.destroy()
    }
}
