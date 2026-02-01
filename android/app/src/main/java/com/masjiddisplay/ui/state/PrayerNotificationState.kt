package com.masjiddisplay.ui.state

import androidx.compose.runtime.*
import com.masjiddisplay.data.Prayer
import com.masjiddisplay.services.SoundNotificationService
import com.masjiddisplay.utils.jakartaDateFormat
import kotlinx.coroutines.delay
import java.util.*

/**
 * Type of prayer alert
 */
enum class AlertType {
    ADHAN,
    IQAMAH
}

/**
 * Data class representing a prayer alert
 */
data class PrayerAlert(
    val type: AlertType? = null,
    val prayer: Prayer? = null,
    val timestamp: Date? = null
)

/**
 * State holder for prayer notifications
 */
class PrayerNotificationState(
    private val soundService: SoundNotificationService?
) {
    companion object {
        const val ADHAN_ALERT_DURATION_MS = 60_000L
        const val IQAMAH_ALERT_DURATION_MS = 60_000L
    }
    
    var currentAlert by mutableStateOf(PrayerAlert())
        private set
    
    private var lastAdhanAlert: String = ""
    private var lastIqamahAlert: String = ""
    
    val isAdhanAlert: Boolean
        get() = currentAlert.type == AlertType.ADHAN
    
    val isIqamahAlert: Boolean
        get() = currentAlert.type == AlertType.IQAMAH
    
    /**
     * Check for prayer alerts based on current time
     */
    suspend fun checkForAlerts(prayers: List<Prayer>, currentTime: Date) {
        val timeFormat = jakartaDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = jakartaDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val currentTimeStr = timeFormat.format(currentTime)
        val currentDateStr = dateFormat.format(currentTime)
        
        for (prayer in prayers) {
            val prayerName = prayer.name.lowercase()
            
            // Skip sunrise/shuruq
            if (prayerName in listOf("shuruq", "syuruq", "sunrise")) {
                continue
            }
            
            // Check for adhan time
            if (currentTimeStr == prayer.adhanTime) {
                val alertKey = "$currentDateStr-${prayer.name}-$currentTimeStr"
                
                if (lastAdhanAlert != alertKey) {
                    lastAdhanAlert = alertKey
                    
                    soundService?.playAdhanAlert()
                    
                    currentAlert = PrayerAlert(
                        type = AlertType.ADHAN,
                        prayer = prayer,
                        timestamp = Date()
                    )
                    
                    // Auto-dismiss after duration
                    delay(ADHAN_ALERT_DURATION_MS)
                    clearAlert()
                    break
                }
            }
            
            // Check for iqamah time
            if (currentTimeStr == prayer.iqamahTime) {
                val alertKey = "$currentDateStr-${prayer.name}-$currentTimeStr"
                
                if (lastIqamahAlert != alertKey) {
                    lastIqamahAlert = alertKey
                    
                    soundService?.playIqamahAlert()
                    
                    currentAlert = PrayerAlert(
                        type = AlertType.IQAMAH,
                        prayer = prayer,
                        timestamp = Date()
                    )
                    
                    // Auto-dismiss after duration
                    delay(IQAMAH_ALERT_DURATION_MS)
                    clearAlert()
                    break
                }
            }
        }
    }
    
    /**
     * Clear the current alert
     */
    fun clearAlert() {
        currentAlert = PrayerAlert()
    }
    
    /**
     * Manually trigger an alert (for debug purposes)
     */
    fun triggerDebugAlert(type: AlertType, prayer: Prayer) {
        when (type) {
            AlertType.ADHAN -> soundService?.playAdhanAlert()
            AlertType.IQAMAH -> soundService?.playIqamahAlert()
        }
        
        currentAlert = PrayerAlert(
            type = type,
            prayer = prayer,
            timestamp = Date()
        )
    }
}

/**
 * Composable to remember prayer notification state
 */
@Composable
fun rememberPrayerNotificationState(
    soundService: SoundNotificationService?
): PrayerNotificationState {
    return remember(soundService) {
        PrayerNotificationState(soundService)
    }
}
