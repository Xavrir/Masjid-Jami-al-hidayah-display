package com.masjiddisplay.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Service for playing sound notifications during adhan and iqamah times
 */
class SoundNotificationService(private val context: Context) {
    
    companion object {
        private const val TAG = "SoundNotificationService"
        private const val ADHAN_DURATION_MS = 10_000L
        private const val IQAMAH_DURATION_MS = 15_000L
    }
    
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying: Boolean = false
    private var stopJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val lock = ReentrantLock()
    
    /**
     * Initialize the media player with the beep sound
     */
    fun initialize() {
        lock.withLock {
            try {
                releaseInternal()
                
                val resourceId = context.resources.getIdentifier(
                    "beep_alarm_sound_effect",
                    "raw",
                    context.packageName
                )
                
                if (resourceId != 0) {
                    mediaPlayer = MediaPlayer.create(context, resourceId)?.apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .build()
                        )
                        isLooping = true
                        setVolume(1.0f, 1.0f)
                    }
                    Log.d(TAG, "Sound notification service initialized successfully")
                } else {
                    Log.w(TAG, "Beep sound resource not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing sound notification service", e)
            }
        }
    }
    
    /**
     * Play adhan alert sound for 10 seconds
     */
    fun playAdhanAlert() {
        playAlert(ADHAN_DURATION_MS, "Adhan")
    }
    
    /**
     * Play iqamah alert sound for 15 seconds
     */
    fun playIqamahAlert() {
        playAlert(IQAMAH_DURATION_MS, "Iqamah")
    }
    
    private fun playAlert(duration: Long, alertType: String) {
        lock.withLock {
            stopAlertInternal()
            
            try {
                mediaPlayer?.let { player ->
                    if (!player.isPlaying) {
                        player.seekTo(0)
                        player.start()
                        isPlaying = true
                        Log.d(TAG, "$alertType alert playing for ${duration / 1000} seconds")
                        
                        // Schedule stop
                        stopJob = serviceScope.launch {
                            delay(duration)
                            stopAlert()
                            Log.d(TAG, "$alertType alert stopped after ${duration / 1000} seconds")
                        }
                    }
                } ?: run {
                    Log.w(TAG, "MediaPlayer not initialized, attempting to reinitialize")
                    // Note: initialize() handles its own lock, so we call the internal version
                    releaseInternal()
                    
                    val resourceId = context.resources.getIdentifier(
                        "beep_alarm_sound_effect",
                        "raw",
                        context.packageName
                    )
                    
                    if (resourceId != 0) {
                        mediaPlayer = MediaPlayer.create(context, resourceId)?.apply {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                    .build()
                            )
                            isLooping = true
                            setVolume(1.0f, 1.0f)
                        }
                    }
                    
                    // Retry after initialization
                    mediaPlayer?.let { player ->
                        player.seekTo(0)
                        player.start()
                        isPlaying = true
                        
                        stopJob = serviceScope.launch {
                            delay(duration)
                            stopAlert()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing $alertType alert", e)
                isPlaying = false
            }
        }
    }
    
    /**
     * Stop alert internal (no lock)
     */
    private fun stopAlertInternal() {
        stopJob?.cancel()
        stopJob = null
        
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    player.seekTo(0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping alert", e)
        }
        
        isPlaying = false
    }
    
    /**
     * Stop the currently playing alert
     */
    fun stopAlert() {
        lock.withLock {
            stopAlertInternal()
        }
    }
    
    /**
     * Check if sound is currently playing
     */
    fun isPlaying(): Boolean = lock.withLock { isPlaying }
    
    /**
     * Release resources (internal, no lock)
     */
    private fun releaseInternal() {
        stopJob?.cancel()
        stopJob = null
        
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
                player.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing media player", e)
        }
        
        isPlaying = false
    }
    
    /**
     * Release resources
     */
    fun release() {
        lock.withLock {
            releaseInternal()
        }
    }
    
    /**
     * Cleanup and cancel scope
     */
    fun cleanup() {
        release()
        serviceScope.cancel()
    }
}

/**
 * Singleton holder for the sound notification service
 */
object SoundNotificationServiceHolder {
    private var instance: SoundNotificationService? = null
    
    fun getInstance(context: Context): SoundNotificationService {
        return instance ?: SoundNotificationService(context.applicationContext).also {
            it.initialize()
            instance = it
        }
    }
    
    fun release() {
        instance?.cleanup()
        instance = null
    }
}
