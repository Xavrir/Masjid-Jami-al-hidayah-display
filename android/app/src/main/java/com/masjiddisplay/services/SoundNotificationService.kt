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
    private var isPlaying = false
    
    private const val ADHAN_ALERT_DURATION_MS = 10000L
    private const val IQAMAH_ALERT_DURATION_MS = 15000L
    
    /**
     * Initialize the sound service with context
     */
    fun initialize(context: Context) {
        release()
        try {
            mediaPlayer = MediaPlayer.create(context, com.masjiddisplay.R.raw.beep_alarm_sound_effect)
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(1.0f, 1.0f)
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
                player.seekTo(0)
                player.start()
                isPlaying = true
                
                stopJob = CoroutineScope(Dispatchers.Main).launch {
                    delay(durationMs)
                    stopAlert()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isPlaying = false
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
                if (player.isPlaying) {
                    player.pause()
                    player.seekTo(0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        isPlaying = false
    }
    
    /**
     * Release the media player resources
     */
    fun release() {
        stopJob?.cancel()
        stopJob = null
        
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        mediaPlayer = null
        isPlaying = false
    }
    
    /**
     * Check if sound is currently playing
     */
    fun isPlaying(): Boolean = isPlaying
}
