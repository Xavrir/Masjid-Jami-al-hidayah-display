package com.masjiddisplay.services

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.*

/**
 * Service for playing sound notifications during adhan and iqamah times
 */
object SoundNotificationService {
    private var mediaPlayer: MediaPlayer? = null
    private var stopJob: Job? = null
    private var isPlayingState = false
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private const val ADHAN_ALERT_DURATION_MS = 10000L
    private const val IQAMAH_ALERT_DURATION_MS = 15000L
    
    /**
     * Initialize the sound service with context
     */
    fun initialize(context: Context) {
        release()
        try {
            val player = MediaPlayer.create(context, com.masjiddisplay.R.raw.beep_alarm_sound_effect)
            if (player != null) {
                player.isLooping = true
                player.setVolume(1.0f, 1.0f)
                mediaPlayer = player
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Play adhan alert sound for 10 seconds
     */
    fun playAdhanAlert() {
        playAlert(ADHAN_ALERT_DURATION_MS)
    }
    
    /**
     * Play iqamah alert sound for 15 seconds
     */
    fun playIqamahAlert() {
        playAlert(IQAMAH_ALERT_DURATION_MS)
    }
    
    private fun playAlert(durationMs: Long) {
        stopAlert()
        
        try {
            mediaPlayer?.let { player ->
                // Set state before starting to avoid race condition
                isPlayingState = true
                player.seekTo(0)
                player.start()
                
                stopJob = serviceScope.launch {
                    delay(durationMs)
                    stopAlert()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isPlayingState = false
        }
    }
    
    /**
     * Stop any currently playing alert
     */
    fun stopAlert() {
        stopJob?.cancel()
        stopJob = null
        
        try {
            mediaPlayer?.let { player ->
                // Use tracked state instead of isPlaying to avoid IllegalStateException
                if (isPlayingState) {
                    try {
                        player.pause()
                        player.seekTo(0)
                    } catch (e: Exception) {
                        // Player might be in invalid state, just ignore
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        isPlayingState = false
    }
    
    /**
     * Release the media player resources
     */
    fun release() {
        stopJob?.cancel()
        stopJob = null
        
        try {
            mediaPlayer?.let { player ->
                // Use tracked state instead of isPlaying to avoid IllegalStateException
                if (isPlayingState) {
                    try {
                        player.stop()
                    } catch (e: Exception) {
                        // Player might be in invalid state, just ignore
                    }
                }
                player.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        mediaPlayer = null
        isPlayingState = false
    }
    
    /**
     * Check if sound is currently playing
     */
    fun isPlaying(): Boolean = isPlayingState
}
