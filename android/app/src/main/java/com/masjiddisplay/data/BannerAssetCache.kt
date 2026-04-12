package com.masjiddisplay.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

object BannerAssetCache {

    private val downloadMutex = Mutex()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun resolve(context: Context, banners: List<BannerRemote>): List<BannerRemote> = withContext(Dispatchers.IO) {
        if (banners.isEmpty()) {
            return@withContext emptyList()
        }

        val mediaDir = File(context.filesDir, "banner-assets").apply { mkdirs() }
        val expectedNames = mutableSetOf<String>()

        val resolved = banners.map { banner ->
            val targetFile = fileFor(mediaDir, banner)
            expectedNames += targetFile.name

            runCatching {
                downloadMutex.withLock {
                    if (!targetFile.exists() || targetFile.length() == 0L) {
                        downloadToFile(banner.image_url, targetFile)
                    }
                }

                banner.copy(local_path = targetFile.toURI().toString())
            }.getOrElse {
                banner
            }
        }

        mediaDir.listFiles()?.forEach { file ->
            if (file.name !in expectedNames) {
                file.delete()
            }
        }

        resolved
    }

    private fun fileFor(directory: File, banner: BannerRemote): File {
        val extension = extensionFor(banner.image_url)
        val hashedName = sha256(banner.image_url).take(16)
        return File(directory, "banner_${banner.id}_$hashedName$extension")
    }

    private fun extensionFor(url: String): String {
        val cleanUrl = url.substringBefore('?').substringBefore('#')
        val lastSegment = cleanUrl.substringAfterLast('/', missingDelimiterValue = cleanUrl)
        val extension = lastSegment.substringAfterLast('.', missingDelimiterValue = "")
        return if (extension.isBlank()) "" else ".${extension.lowercase()}"
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun downloadToFile(url: String, targetFile: File) {
        val request = Request.Builder()
            .url(url)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to download banner asset: ${response.code}")
            }

            val body = response.body ?: throw IOException("Missing response body for banner asset")
            val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")

            body.byteStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }
        }
    }
}
