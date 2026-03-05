package com.onetap.service

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.Context
import android.widget.Toast
import java.util.Locale

/**
 * Voice recognition service for OneTap
 * Android Uses's built-in offline speech recognition
 */
class VoiceService(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    var onResult: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    
    companion object {
        // Command patterns
        val APP_PATTERNS = mapOf(
            "whatsapp" to "com.whatsapp",
            "message" to "com.android.mms",
            "messages" to "com.android.mms",
            "phone" to "com.android.contacts",
            "call" to "com.android.contacts",
            "camera" to "com.android.camera",
            "photo" to "com.google.android.apps.photos",
            "photos" to "com.google.android.apps.photos",
            "youtube" to "com.google.android.youtube",
            "music" to "com.google.android.music",
            "spotify" to "com.spotify.music",
            "gaana" to "com.gaana",
            "jiosaavn" to "com.jio.media.jiobeats",
            "calculator" to "com.android.calculator2",
            "settings" to "com.android.settings",
            "chrome" to "com.android.chrome",
            "browser" to "com.android.chrome",
            "maps" to "com.google.android.apps.maps",
            "calendar" to "com.google.android.calendar",
            "gmail" to "com.google.android.gm",
            "mail" to "com.google.android.gm",
            "instagram" to "com.instagram.android",
            "facebook" to "com.facebook.katana",
            "twitter" to "com.twitter.android",
            "telegram" to "org.telegram.messenger",
            "zoom" to "us.zoom.videomeetings",
            "meet" to "com.google.android.apps.tachyon",
            "paytm" to "com.paytm",
            "gpay" to "com.google.android.apps.nbu.paisa.user",
            "phonepe" to "com.phonepe.app",
            "amazon" to "com.amazon.mShop.android.shopping",
            "flipkart" to "com.flipkart.android",
            "youtube music" to "com.google.android.apps.youtube.music",
            "vlc" to "org.videolan.vlc",
            "mx player" to "com.mxtech.videoplayer.ad",
            "file manager" to "com.estrongs.android.pop",
            "files" to "com.estrongs.android.pop",
            "terminal" to "com.termux"
        )
        
        val ACTION_PATTERNS = mapOf(
            "play" to "play",
            "pause" to "pause",
            "stop" to "stop",
            "open" to "open",
            "call" to "call",
            "message" to "message",
            "whatsapp" to "whatsapp"
        )
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError?.invoke("Speech recognition not available")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(createRecognitionListener())

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            onError?.invoke("Failed to start listening")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            
            override fun onError(error: Int) {
                isListening = false
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error"
                }
                onError?.invoke(errorMessage)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult?.invoke(matches[0].lowercase())
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult?.invoke(matches[0].lowercase())
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    /**
     * Parse voice command and return package name
     */
    fun parseCommand(speech: String): CommandResult {
        val lowerSpeech = speech.lowercase()
        
        // Check for app names
        for ((name, packageName) in APP_PATTERNS) {
            if (lowerSpeech.contains(name)) {
                return CommandResult(
                    type = CommandType.OPEN_APP,
                    packageName = packageName,
                    displayText = name.replaceFirstChar { it.uppercase() }
                )
            }
        }
        
        // Check for specific action commands
        when {
            lowerSpeech.contains("play") && (lowerSpeech.contains("song") || lowerSpeech.contains("music") || lowerSpeech.contains("youtube")) -> {
                return CommandResult(
                    type = CommandType.OPEN_APP,
                    packageName = "com.google.android.youtube",
                    displayText = "YouTube"
                )
            }
            lowerSpeech.contains("play") && lowerSpeech.contains("video") -> {
                return CommandResult(
                    type = CommandType.OPEN_APP,
                    packageName = "com.google.android.youtube",
                    displayText = "YouTube"
                )
            }
            lowerSpeech.startsWith("call ") || lowerSpeech.contains("call ") -> {
                return CommandResult(
                    type = CommandType.OPEN_APP,
                    packageName = "com.android.contacts",
                    displayText = "Phone"
                )
            }
            lowerSpeech.contains("whatsapp") -> {
                return CommandResult(
                    type = CommandType.OPEN_APP,
                    packageName = "com.whatsapp",
                    displayText = "WhatsApp"
                )
            }
        }
        
        return CommandResult(
            type = CommandType.UNKNOWN,
            packageName = null,
            displayText = speech
        )
    }
}

data class CommandResult(
    val type: CommandType,
    val packageName: String?,
    val displayText: String
)

enum class CommandType {
    OPEN_APP,
    CALL_CONTACT,
    SEND_MESSAGE,
    UNKNOWN
}
