package com.azzahid.hof.services

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import com.azzahid.hof.data.repository.PermissionRepository
import com.azzahid.hof.domain.model.NotificationPermissionState
import kotlinx.coroutines.runBlocking

interface PermissionService {
    fun getNotificationPermissionState(): NotificationPermissionState
    fun shouldShowNotificationRationale(activity: Activity): Boolean
    fun setNotificationRequested()
    fun openAppSettings()
    fun handleNotificationPermissionRequest(
        activity: Activity?,
        launcher: ActivityResultLauncher<String>,
        onGranted: () -> Unit,
        onShowRationale: () -> Unit
    )
}

class AndroidPermissionService(
    private val permissionRepository: PermissionRepository
) : PermissionService {

    override fun getNotificationPermissionState(): NotificationPermissionState {
        return runBlocking { permissionRepository.getNotificationPermissionState() }
    }

    override fun shouldShowNotificationRationale(activity: Activity): Boolean {
        return permissionRepository.shouldShowNotificationRationale(activity)
    }

    override fun setNotificationRequested() {
        runBlocking { permissionRepository.setNotificationRequested() }
    }

    override fun openAppSettings() {
        permissionRepository.openAppSettings()
    }

    override fun handleNotificationPermissionRequest(
        activity: Activity?,
        launcher: ActivityResultLauncher<String>,
        onGranted: () -> Unit,
        onShowRationale: () -> Unit
    ) {
        val permissionState = getNotificationPermissionState()
        if (permissionState.isGranted) {
            onGranted()
        } else {
            val isPermanentlyDenied = activity?.let { act ->
                !shouldShowNotificationRationale(act) && !permissionState.isFirstRequest
            } ?: false

            if (isPermanentlyDenied) {
                onShowRationale()
            } else {
                setNotificationRequested()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}