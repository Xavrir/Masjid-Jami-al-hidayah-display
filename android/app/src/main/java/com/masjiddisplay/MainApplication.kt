package com.masjiddisplay

import android.app.Application
import android.content.ComponentCallbacks2
import com.masjiddisplay.services.SoundNotificationService

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the sound notification service
        SoundNotificationService.initialize(this)
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Release sound resources when app is going to background or under memory pressure
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            SoundNotificationService.stopAlert()
        }
        if (level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            SoundNotificationService.release()
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        SoundNotificationService.release()
    }
}
