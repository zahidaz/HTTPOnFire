package com.azzahid.hof.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.azzahid.hof.data.repository.PermissionRepository
import com.azzahid.hof.data.repository.SettingsRepository
import com.azzahid.hof.domain.model.NotificationPermissionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PermissionViewModel(
    private val permissionRepository: PermissionRepository,
    private val settingsRepository: SettingsRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _notificationPermissionState = MutableStateFlow(NotificationPermissionState())

    private val _showFirstLaunchDialog = MutableStateFlow(false)
    val showFirstLaunchDialog: StateFlow<Boolean> = _showFirstLaunchDialog.asStateFlow()

    private val _showPermissionRationale = MutableStateFlow(false)
    val showPermissionRationale: StateFlow<Boolean> = _showPermissionRationale.asStateFlow()

    init {
        checkFirstLaunch()
        refreshPermissionState()
    }

    private fun checkFirstLaunch() {
        viewModelScope.launch {
            permissionRepository.isFirstLaunch.collect { isFirst ->
                if (isFirst) {
                    _showFirstLaunchDialog.value = true
                }
            }
        }
    }

    private fun refreshPermissionState() {
        viewModelScope.launch {
            val state = permissionRepository.getNotificationPermissionState()
            _notificationPermissionState.value = state
        }
    }

    fun onFirstLaunchDialogDismissed() {
        _showFirstLaunchDialog.value = false
        viewModelScope.launch {
            permissionRepository.setFirstLaunchComplete()
        }
    }

    fun requestNotificationPermission(launcher: ActivityResultLauncher<String>) {
        viewModelScope.launch {
            permissionRepository.setNotificationRequested()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean, activity: Activity? = null) {
        viewModelScope.launch {
            if (isGranted) {
                settingsRepository.updateBackgroundServiceEnabled(true)
            } else {
                val shouldShowRationale = activity?.let {
                    permissionRepository.shouldShowNotificationRationale(it)
                } ?: false

                if (!shouldShowRationale) {
                    _notificationPermissionState.value = _notificationPermissionState.value.copy(
                        canRequest = false
                    )
                }
            }
            refreshPermissionState()
        }
    }


    fun openAppSettings() {
        permissionRepository.openAppSettings()
        _showPermissionRationale.value = false
    }

    fun dismissPermissionRationale() {
        _showPermissionRationale.value = false
    }
}