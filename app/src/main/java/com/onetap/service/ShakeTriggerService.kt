package com.onetap.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import com.onetap.MainActivity
import kotlin.math.sqrt

/**
 * Shake-to-open trigger service
 * Detects phone shake and opens OneTap
 */
class ShakeTriggerService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    
    private var lastShakeTime: Long = 0
    private var shakeCount = 0
    
    companion object {
        const val ACTION_START = "com.onetap.START_SHAKE"
        const val ACTION_STOP = "com.onetap.STOP_SHAKE"
        
        private const val SHAKE_THRESHOLD = 12f
        private const val SHAKE_COOLDOWN_MS = 500L
        private const val SHAKES_TO_TRIGGER = 2
        
        fun start(context: Context) {
            val intent = Intent(context, ShakeTriggerService::class.java).apply {
                action = ACTION_START
            }
            context.startService(intent)
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, ShakeTriggerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                accelerometer?.let {
                    sensorManager.registerListener(
                        this, 
                        it, 
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                }
            }
            ACTION_STOP -> {
                sensorManager.unregisterListener(this)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        // Calculate acceleration (subtract gravity)
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat() - SensorManager.GRAVITY_EARTH
        
        if (acceleration > SHAKE_THRESHOLD) {
            val currentTime = System.currentTimeMillis()
            
            // Check cooldown
            if (currentTime - lastShakeTime > SHAKE_COOLDOWN_MS) {
                shakeCount++
                lastShakeTime = currentTime
                
                // Trigger after enough shakes
                if (shakeCount >= SHAKES_TO_TRIGGER) {
                    shakeCount = 0
                    onShakeDetected()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }

    private fun onShakeDetected() {
        // Vibrate to confirm
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        
        // Open OneTap
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}
