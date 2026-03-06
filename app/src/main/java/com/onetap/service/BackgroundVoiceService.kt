package com.onetap.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.onetap.MainActivity

/**
 * Background voice listening service
 * Listens for voice commands even when app is in background
 */
class BackgroundVoiceService : Service() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    
    companion object {
        const val CHANNEL_ID = "onetap_voice_channel"
        const val NOTIFICATION_ID = 2
        const val ACTION_START = "com.onetap.START_VOICE"
        const val ACTION_STOP = "com.onetap.STOP_VOICE"
        const val ACTION_LISTEN = "com.onetap.LISTEN"
        
        const val TAG = "BackgroundVoice"
        
        // App command patterns
        val APP_COMMANDS = mapOf(
            "whatsapp" to "com.whatsapp",
            "message" to "com.android.mms",
            "phone" to "com.android.contacts",
            "call" to "com.android.contacts",
            "camera" to "com.android.camera",
            "photo" to "com.google.android.apps.photos",
            "youtube" to "com.google.android.youtube",
            "music" to "com.google.android.music",
            "spotify" to "com.spotify.music",
            "gaana" to "com.gaana",
            "jio saavn" to "com.jio.media.jiobeats",
            "calculator" to "com.android.calculator2",
            "settings" to "com.android.settings",
            "chrome" to "com.android.chrome",
            "maps" to "com.google.android.apps.maps",
            "gmail" to "com.google.android.gm",
            "instagram" to "com.instagram.android",
            "facebook" to "com.facebook.katana",
            "telegram" to "org.telegram.messenger",
            "zoom" to "us.zoom.videomeetings",
            "meet" to "com.google.android.apps.tachyon",
            "paytm" to "com.paytm",
            "gpay" to "com.google.android.apps.nbu.paisa.user",
            "phonepe" to "com.phonepe.app",
            "amazon" to "com.amazon.mShop.android.shopping",
            "flipkart" to "com.flipkart.android",
            "vlc" to "org.videolan.vlc",
            "terminal" to "com.termux"
        )
        
        fun start(context: android.content.Context) {
            Log.d(TAG, "Starting background voice service...")
            val intent = Intent(context, BackgroundVoiceService::class.java).apply {
                action = ACTION_START
            }
            try {
                context.startForegroundService(intent)
                Log.d(TAG, "startForegroundService called successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service: ${e.message}")
            }
        }
        
        fun stop(context: android.content.Context) {
            val intent = Intent(context, BackgroundVoiceService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called with action: ${intent?.action}")
        when (intent?.action) {
            ACTION_START -> {
                Log.d(TAG, "Starting foreground...")
                startForeground(NOTIFICATION_ID, createNotification())
                Log.d(TAG, "Foreground started, notification shown")
            }
            ACTION_LISTEN -> {
                Log.d(TAG, "Listen action triggered")
                startListening()
            }
            ACTION_STOP -> {
                Log.d(TAG, "Stop action triggered")
                stopListening()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "OneTap Voice",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Listens for voice commands"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Listen action
        val listenIntent = Intent(this, BackgroundVoiceService::class.java).apply {
            action = ACTION_LISTEN
        }
        val listenPendingIntent = PendingIntent.getService(
            this, 1, listenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, BackgroundVoiceService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("OneTap Voice")
            .setContentText("Tap Listen to speak a command")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_btn_speak_now, "Listen", listenPendingIntent)
            .addAction(0, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    fun startListening() {
        Log.d(TAG, "startListening called")
        
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.e(TAG, "Speech recognition not available")
            return
        }

        Log.d(TAG, "Creating speech recognizer...")
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(createListener())

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            Log.d(TAG, "Started listening...")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignore
        }
        isListening = false
        Log.d(TAG, "Stopped listening")
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            
            override fun onError(error: Int) {
                isListening = false
                Log.e(TAG, "Speech error: $error")
                // Don't auto-restart - user taps Listen button instead
            }

            override fun onResults(results: android.os.Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val speech = matches[0].lowercase()
                    Log.d(TAG, "Heard: $speech")
                    processCommand(speech)
                }
                // Continue listening
                startListening()
            }

            override fun onPartialResults(partialResults: android.os.Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val speech = matches[0].lowercase()
                    // Check for wake word
                    if (speech.contains("hey one tap") || speech.contains("one tap")) {
                        Log.d(TAG, "Wake word detected: $speech")
                    }
                }
            }

            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        }
    }

    private fun processCommand(speech: String) {
        Log.d(TAG, "processCommand called with: $speech")
        val lowerSpeech = speech.lowercase()
        
        // Check for wake word
        val command = if (lowerSpeech.contains("hey one tap") || lowerSpeech.contains("one tap")) {
            lowerSpeech.replace("hey one tap", "").replace("one tap", "").trim()
        } else {
            lowerSpeech
        }
        
        Log.d(TAG, "Processing command: $command")
        
        // Find matching app
        for ((name, packageName) in APP_COMMANDS) {
            if (command.contains(name)) {
                Log.d(TAG, "Matched app: $name -> $packageName")
                openApp(packageName)
                return
            }
        }
        
        // Special commands
        when {
            command.contains("play") && command.contains("song") -> openApp("com.google.android.music")
            command.contains("play") && command.contains("youtube") -> openApp("com.google.android.youtube")
            command.contains("play") && command.contains("video") -> openApp("com.google.android.youtube")
            command.contains("call") -> openApp("com.android.contacts")
            command.contains("send message") -> openApp("com.whatsapp")
        }
        
        Log.d(TAG, "No matching command found for: $command")
    }

    private fun openApp(packageName: String) {
        Log.d(TAG, "openApp called with package: $packageName")
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Log.d(TAG, "Successfully launched: $packageName")
            } else {
                Log.e(TAG, "No launch intent found for: $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open app: ${e.message}")
        }
    }

    override fun onDestroy() {
        stopListening()
        speechRecognizer?.destroy()
        super.onDestroy()
    }
}
