package com.example.vig.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.vig.domain.interfaces.TextToSpeechProvider
import java.util.Locale
import java.util.UUID

class AndroidTextToSpeech(context: Context) : TextToSpeechProvider, TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingOnDone: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isReady = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    pendingOnDone?.invoke()
                    pendingOnDone = null
                }
                override fun onError(utteranceId: String?) {
                    pendingOnDone?.invoke()
                    pendingOnDone = null
                }
            })
        }
    }

    override fun speak(text: String, onDone: (() -> Unit)?) {
        if (!isReady) {
            onDone?.invoke()
            return
        }
        pendingOnDone = onDone
        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun stop() {
        tts?.stop()
        pendingOnDone = null
    }

    override fun setSpeed(speed: Float) {
        tts?.setSpeechRate(speed)
    }

    override fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
