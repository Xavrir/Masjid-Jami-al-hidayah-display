package com.masjiddisplay

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.masjiddisplay.data.LocalCache
import com.masjiddisplay.data.MediaCacheProvider
import com.masjiddisplay.data.SupabaseRepository

class MainApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        LocalCache.initialize(this)
        MediaCacheProvider.initialize(this)
        SupabaseRepository.initialize(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(256L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
    }
}
