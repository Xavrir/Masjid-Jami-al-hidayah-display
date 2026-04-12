package com.masjiddisplay.data

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@OptIn(UnstableApi::class)
object MediaCacheProvider {

    private const val VIDEO_CACHE_SIZE_BYTES = 500L * 1024 * 1024

    @Volatile
    private var videoCache: SimpleCache? = null

    fun initialize(context: Context) {
        getVideoCache(context)
    }

    fun buildVideoDataSourceFactory(context: Context): DataSource.Factory {
        val cache = getVideoCache(context)
        val upstreamFactory = DefaultHttpDataSource.Factory()
            .setConnectTimeoutMs(30_000)
            .setReadTimeoutMs(30_000)
            .setAllowCrossProtocolRedirects(true)

        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    private fun getVideoCache(context: Context): SimpleCache {
        videoCache?.let { return it }

        return synchronized(this) {
            videoCache?.let { return it }

            val cacheDir = File(context.cacheDir, "video-cache").apply {
                mkdirs()
            }

            SimpleCache(
                cacheDir,
                LeastRecentlyUsedCacheEvictor(VIDEO_CACHE_SIZE_BYTES),
                StandaloneDatabaseProvider(context.applicationContext)
            ).also {
                videoCache = it
            }
        }
    }
}
