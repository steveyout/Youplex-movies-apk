package com.example.cinestream.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val isUpdateRequired: Boolean = false,
    val isUpdateAvailable: Boolean = false,
    val latestVersionName: String = "1.0",
    val latestVersionCode: Int = 1,
    val minRequiredVersionCode: Int = 1,
    val downloadUrl: String = "https://github.com/steveyout/youplex-apk/releases",
    val releaseNotes: String = "",
    val forceUpdate: Boolean = false
)

object UpdateChecker {
    private const val TAG = "UpdateChecker"
    private const val GITHUB_RELEASE_URL = "https://api.github.com/repos/steveyout/Youplex-movies-apk/releases/latest"
    private const val RAW_VERSION_URL = "https://raw.githubusercontent.com/steveyout/Youplex-movies-apk/main/version.json"
    private const val FALLBACK_REPO_URL = "https://github.com/steveyout/Youplex-movies-apk/releases"
    private const val SECONDARY_RELEASE_URL = "https://api.github.com/repos/steveyout/youplex-apk/releases/latest"

    suspend fun checkUpdate(currentVersionCode: Int, currentVersionName: String): UpdateInfo = withContext(Dispatchers.IO) {
        // 1. Try checking GitHub Release API
        try {
            val url = URL(GITHUB_RELEASE_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "CineStream-App")
            connection.connectTimeout = 6000
            connection.readTimeout = 6000

            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)

                val tagName = json.optString("tag_name", "v1.0").removePrefix("v").trim()
                val body = json.optString("body", "Bug fixes and streaming performance improvements.")
                val htmlUrl = json.optString("html_url", FALLBACK_REPO_URL)

                var downloadUrl = htmlUrl
                val assets = json.optJSONArray("assets")
                if (assets != null && assets.length() > 0) {
                    val firstAsset = assets.getJSONObject(0)
                    downloadUrl = firstAsset.optString("browser_download_url", htmlUrl)
                }

                // Parse version numbers (e.g., "1.1" -> versionCode ~ 2, or compare semantic versions)
                val latestCode = parseVersionCodeFromTag(tagName, currentVersionCode)
                val isNewer = isVersionNewer(currentVersionName, tagName) || latestCode > currentVersionCode

                // Check if release body includes mandatory indicator "[MANDATORY]" or "force_update: true"
                val isForceUpdate = isNewer && (body.contains("[MANDATORY]", ignoreCase = true) || body.contains("force_update", ignoreCase = true) || latestCode > currentVersionCode)

                if (isNewer) {
                    return@withContext UpdateInfo(
                        isUpdateRequired = isForceUpdate,
                        isUpdateAvailable = true,
                        latestVersionName = tagName,
                        latestVersionCode = latestCode,
                        minRequiredVersionCode = latestCode,
                        downloadUrl = downloadUrl,
                        releaseNotes = body,
                        forceUpdate = isForceUpdate
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "GitHub release check failed: ${e.message}")
        }

        // 2. Try raw version.json as fallback
        try {
            val url = URL(RAW_VERSION_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)

                val latestVersionName = json.optString("versionName", currentVersionName)
                val latestVersionCode = json.optInt("versionCode", currentVersionCode)
                val minRequiredCode = json.optInt("minRequiredVersionCode", 1)
                val forceUpdate = json.optBoolean("forceUpdate", false)
                val downloadUrl = json.optString("downloadUrl", FALLBACK_REPO_URL)
                val notes = json.optString("releaseNotes", "A mandatory system update is available.")

                val isNewer = latestVersionCode > currentVersionCode || isVersionNewer(currentVersionName, latestVersionName)
                val isRequired = isNewer && (forceUpdate || currentVersionCode < minRequiredCode)

                if (isNewer) {
                    return@withContext UpdateInfo(
                        isUpdateRequired = isRequired,
                        isUpdateAvailable = true,
                        latestVersionName = latestVersionName,
                        latestVersionCode = latestVersionCode,
                        minRequiredVersionCode = minRequiredCode,
                        downloadUrl = downloadUrl,
                        releaseNotes = notes,
                        forceUpdate = isRequired
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Raw version check failed: ${e.message}")
        }

        // No update found or up to date
        UpdateInfo(
            isUpdateRequired = false,
            isUpdateAvailable = false,
            latestVersionName = currentVersionName,
            latestVersionCode = currentVersionCode,
            minRequiredVersionCode = currentVersionCode,
            downloadUrl = FALLBACK_REPO_URL,
            releaseNotes = "App is up to date."
        )
    }

    private fun parseVersionCodeFromTag(tag: String, defaultCode: Int): Int {
        return try {
            val parts = tag.split(".")
            if (parts.size >= 2) {
                val major = parts[0].toIntOrNull() ?: 1
                val minor = parts[1].toIntOrNull() ?: 0
                val patch = if (parts.size > 2) parts[2].toIntOrNull() ?: 0 else 0
                major * 100 + minor * 10 + patch
            } else {
                tag.toIntOrNull() ?: (defaultCode + 1)
            }
        } catch (e: Exception) {
            defaultCode + 1
        }
    }

    private fun isVersionNewer(current: String, latest: String): Boolean {
        try {
            val currParts = current.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
            val lateParts = latest.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(currParts.size, lateParts.size)
            for (i in 0 until maxLen) {
                val c = currParts.getOrElse(i) { 0 }
                val l = lateParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
        } catch (e: Exception) {
            return latest != current
        }
        return false
    }

    fun openDownloadUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening update URL: ${e.message}")
        }
    }
}
