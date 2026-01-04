package com.masjiddisplay.ui.state

import com.masjiddisplay.data.Prayer
import java.util.*

class PrayerNotificationState {
    private var lastAlertedPrayer: String? = null
    private var lastAlertTime: Long = 0
    
    fun checkForAlerts(prayers: List<Prayer>, currentTime: Date) {
    }
}
