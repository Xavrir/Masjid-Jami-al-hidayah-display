package com.masjiddisplay.data

import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.lang.reflect.Type

object LocalCache {

    data class CachedValue<T>(
        val savedAt: Long,
        val value: T
    )

    private data class CacheEnvelope(
        val savedAt: Long,
        val payload: String
    )

    private val gson = Gson()
    private lateinit var cacheDir: File

    fun initialize(context: Context) {
        if (::cacheDir.isInitialized) return
        cacheDir = File(context.filesDir, "supabase-cache").apply {
            mkdirs()
        }
    }

    fun <T> write(key: String, value: T, type: Type) {
        if (!::cacheDir.isInitialized) return

        runCatching {
            val envelope = CacheEnvelope(
                savedAt = System.currentTimeMillis(),
                payload = gson.toJson(value, type)
            )
            fileFor(key).writeText(gson.toJson(envelope))
        }
    }

    fun <T> read(key: String, type: Type): CachedValue<T>? {
        if (!::cacheDir.isInitialized) return null

        val file = fileFor(key)
        if (!file.exists()) return null

        return runCatching<CachedValue<T>> {
            val envelope = gson.fromJson(file.readText(), CacheEnvelope::class.java)
            val value: T = gson.fromJson(envelope.payload, type)
            CachedValue(
                savedAt = envelope.savedAt,
                value = value
            )
        }.getOrNull()
    }

    fun remove(key: String) {
        if (!::cacheDir.isInitialized) return
        fileFor(key).delete()
    }

    private fun fileFor(key: String): File {
        val safeKey = key.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return File(cacheDir, "$safeKey.json")
    }
}
