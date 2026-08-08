package com.example.cinestream.data.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsManager {
    private const val TAG = "AnalyticsManager"
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
            Log.d(TAG, "Firebase Analytics initialized successfully.")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Analytics failed to initialize: ${e.message}")
        }
    }

    fun logScreenView(screenName: String) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            }
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
            Log.d(TAG, "Logged Screen View: $screenName")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging screen view: ${e.message}")
        }
    }

    fun logMediaSelected(mediaId: String, title: String, mediaType: String) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_ID, mediaId)
                putString(FirebaseAnalytics.Param.ITEM_NAME, title)
                putString(FirebaseAnalytics.Param.CONTENT_TYPE, mediaType)
            }
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle)
            Log.d(TAG, "Logged Select Item: $title ($mediaType)")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging select item: ${e.message}")
        }
    }

    fun logSearch(query: String) {
        if (query.isBlank()) return
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
            }
            firebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
            Log.d(TAG, "Logged Search Query: $query")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging search: ${e.message}")
        }
    }

    fun logServerProviderChanged(providerId: String) {
        try {
            val bundle = Bundle().apply {
                putString("provider_id", providerId)
            }
            firebaseAnalytics?.logEvent("change_server_provider", bundle)
            Log.d(TAG, "Logged Provider Change: $providerId")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging provider change: ${e.message}")
        }
    }

    fun logAppUpdateCheck(latestVersionName: String, isUpdateRequired: Boolean) {
        try {
            val bundle = Bundle().apply {
                putString("latest_version", latestVersionName)
                putBoolean("is_update_required", isUpdateRequired)
            }
            firebaseAnalytics?.logEvent("check_app_update", bundle)
            Log.d(TAG, "Logged App Update Check: v$latestVersionName (Required: $isUpdateRequired)")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging app update check: ${e.message}")
        }
    }
}
