package com.azzahid.hof.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(
    val title: String,
    val body: String,
    val priority: String = "DEFAULT",
    val autoCancel: Boolean = true,
    val ongoing: Boolean = false
) {
    fun isValid(): Boolean {
        return title.isNotBlank() && body.isNotBlank() && priority in validPriorities
    }

    fun getPriorityLevel(): Int {
        return when (priority.uppercase()) {
            "MIN" -> -2
            "LOW" -> -1
            "DEFAULT" -> 0
            "HIGH" -> 1
            "MAX" -> 2
            else -> 0
        }
    }

    companion object {
        val validPriorities = setOf("MIN", "LOW", "DEFAULT", "HIGH", "MAX")
    }
}