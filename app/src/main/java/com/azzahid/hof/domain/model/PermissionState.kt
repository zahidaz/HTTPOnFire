package com.azzahid.hof.domain.model

data class NotificationPermissionState(
    val isGranted: Boolean = false,
    val canRequest: Boolean = true,
    val isFirstRequest: Boolean = true,
    val showRationale: Boolean = false
)
