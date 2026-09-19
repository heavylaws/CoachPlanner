package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceNoteManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription.asStateFlow()

    private val _waveformAmplitudes = MutableStateFlow<List<Float>>(List(16) { 0.2f })
    val waveformAmplitudes: StateFlow<List<Float>> = _waveformAmplitudes.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(0)
    val recordingDurationSec: StateFlow<Int> = _recordingDurationSec.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    fun startListening(onResult: (String) -> Unit) {
        _speechError.value = null
        _transcription.value = ""
        _recordingDurationSec.value = 0

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _speechError.value = "Speech recognition service unavailable on device. Using coach quick-voice presets."
            _isRecording.value = true
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isRecording.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _isRecording.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Update waveform
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.15f, 1f)
                        _waveformAmplitudes.value = List(16) { idx ->
                            val phase = (idx % 4) * 0.1f
                            (normalized * (0.5f + Math.random().toFloat() * 0.5f) + phase).coerceIn(0.1f, 1f)
                        }
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isRecording.value = false
                    }

                    override fun onError(error: Int) {
                        _isRecording.value = false
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Speak clearly into the mic."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network issue for voice note."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            else -> "Recording finished."
                        }
                        _speechError.value = msg
                    }

                    override fun onResults(results: Bundle?) {
                        _isRecording.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull().orEmpty()
                        if (text.isNotBlank()) {
                            _transcription.value = text
                            onResult(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let {
                            _transcription.value = it
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _speechError.value = "Voice note error: ${e.message}"
            _isRecording.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _isRecording.value = false
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    /**
     * Coach Voice Note Presets for instant tactical generation
     */
    val coachVoicePresets = listOf(
        "Overload the right wing with a full-back overlap and driven cross to the near post.",
        "Set up a 4v2 high press rondo with fast 1-touch splitting passes.",
        "Rapid 3v2 counter attack starting from a midfield interception to far post tap-in.",
        "Midfield pressing trap when opposing CB plays into central pivot.",
        "Diagonal switch of play from left fullback to isolated right winger."
    )
}
