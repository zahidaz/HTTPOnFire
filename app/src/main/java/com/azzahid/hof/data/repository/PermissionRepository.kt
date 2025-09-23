package com.azzahid.hof.data.repository

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.azzahid.hof.domain.model.NotificationPermissionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PermissionRepository(private val context: Context) {

    companion object {
        private val FIRST_LAUNCH_KEY = booleanPreferencesKey("first_launch")
        private val NOTIFICATION_REQUESTED_KEY = booleanPreferencesKey("notification_requested")
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[FIRST_LAUNCH_KEY] ?: true
        }

    val notificationRequested: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[NOTIFICATION_REQUESTED_KEY] ?: false
        }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH_KEY] = false
        }
    }

    suspend fun setNotificationRequested() {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_REQUESTED_KEY] = true
        }
    }

    fun checkNotificationPermission(): NotificationPermissionState {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return NotificationPermissionState(
                isGranted = true,
                canRequest = false,
                isFirstRequest = false
            )
        }

        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        return NotificationPermissionState(
            isGranted = isGranted,
            canRequest = !isGranted,
            isFirstRequest = true
        )
    }

    fun shouldShowNotificationRationale(activity: android.app.Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else false
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    suspend fun getNotificationPermissionState(): NotificationPermissionState {
        val baseState = checkNotificationPermission()
        val isFirstRequest = !notificationRequested.first()

        return baseState.copy(
            isFirstRequest = isFirstRequest,
            canRequest = !baseState.isGranted && isFirstRequest
        )
    }
}