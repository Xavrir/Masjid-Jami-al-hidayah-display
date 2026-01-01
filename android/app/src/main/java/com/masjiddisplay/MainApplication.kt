package com.masjiddisplay

import android.app.Application
import com.masjiddisplay.services.SoundNotificationService

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the sound notification service
        SoundNotificationService.initialize(this)
    }
    
    override fun onTerminate() {
        super.onTerminate()
        SoundNotificationService.release()
    }
}
