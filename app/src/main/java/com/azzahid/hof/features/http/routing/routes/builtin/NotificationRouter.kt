package com.azzahid.hof.features.http.routing.routes.builtin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.NotificationRequest
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import com.azzahid.hof.features.http.androidContext
import com.azzahid.hof.services.DeviceNotificationService
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

internal fun io.ktor.server.routing.Route.addNotificationRoute(route: Route) {
    post(route.path, {
        description = "Trigger a device notification with custom title and message"
        request {
            body<NotificationRequest> {
                description = "Notification details including title, body, and optional settings"
                example("Basic Notification") {
                    description = "Simple notification with title and body"
                    value = NotificationRequest(
                        title = "Task Completed",
                        body = "Your backup finished successfully"
                    )
                }
                example("High Priority Alert") {
                    description = "High priority notification that persists until user dismisses"
                    value = NotificationRequest(
                        title = "Security Alert",
                        body = "Suspicious login attempt detected",
                        priority = "HIGH",
                        autoCancel = false
                    )
                }
                example("Ongoing Notification") {
                    description = "Persistent notification that stays in notification bar"
                    value = NotificationRequest(
                        title = "Download in Progress",
                        body = "Downloading important files...",
                        ongoing = true,
                        autoCancel = false
                    )
                }
            }
        }
        response {
            HttpStatusCode.OK to {
                description = "Notification sent successfully"
                body<Success<Map<String, Any>>> {
                    example("Success Response") {
                        description = "Successful notification delivery"
                        value = Success(
                            data = mapOf(
                                "message" to "Notification sent successfully",
                                "notificationId" to 1001
                            )
                        )
                    }
                }
            }
            HttpStatusCode.Forbidden to {
                description = "Notification permission not granted"
                body<Failure> {
                    example("Permission Denied") {
                        description = "User has not granted notification permission"
                        value =
                            Failure(error = "Notification permission required. Enable notifications in device settings.")
                    }
                }
            }
            HttpStatusCode.BadRequest to {
                description = "Invalid request data"
                body<Failure> {
                    example("Validation Error") {
                        description = "Request validation failed"
                        value = Failure(error = "Title and body are required and cannot be blank")
                    }
                    example("Invalid Priority") {
                        description = "Invalid priority value provided"
                        value =
                            Failure(error = "Priority must be one of: MIN, LOW, DEFAULT, HIGH, MAX")
                    }
                }
            }
            HttpStatusCode.InternalServerError to {
                description = "Internal server error while sending notification"
                body<Failure> {
                    example("System Error") {
                        description = "System error occurred"
                        value = Failure(error = "Failed to send notification due to system error")
                    }
                }
            }
        }
    }) {

        val context = call.application.androidContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    Failure(error = "Notification permission required. Enable notifications in device settings.")
                )
                return@post
            }
        }

        try {
            val request = call.receive<NotificationRequest>()

            if (!request.isValid()) {
                val errorMessage = when {
                    request.title.isBlank() -> "Title is required and cannot be blank"
                    request.body.isBlank() -> "Body is required and cannot be blank"
                    request.priority !in NotificationRequest.validPriorities ->
                        "Priority must be one of: ${
                            NotificationRequest.validPriorities.joinToString(
                                ", "
                            )
                        }"

                    else -> "Invalid request data"
                }
                call.respond(HttpStatusCode.BadRequest, Failure(error = errorMessage))
                return@post
            }

            val notificationService = DeviceNotificationService(context)
            val result = notificationService.sendNotification(request)

            if (result.success) {
                call.respond(
                    Success(
                        data = mapOf(
                            "message" to result.message,
                            "notificationId" to result.notificationId!!
                        )
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    Failure(error = result.error ?: "Unknown error occurred")
                )
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = "Invalid request format: ${e.message}")
            )
        }
    }
}