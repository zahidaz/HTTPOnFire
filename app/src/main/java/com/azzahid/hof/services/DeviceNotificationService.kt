package com.azzahid.hof.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.azzahid.hof.MainActivity
import com.azzahid.hof.R
import com.azzahid.hof.domain.model.NotificationRequest

class DeviceNotificationService(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "UserNotifications"
        const val CHANNEL_NAME = "User Notifications"
        private var notificationId = 1000
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications triggered via API"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getLargeIconBitmap(): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)
            when (drawable) {
                is BitmapDrawable -> drawable.bitmap
                else -> {
                    drawable?.let {
                        val bitmap = createBitmap(
                            it.intrinsicWidth.takeIf { w -> w > 0 } ?: 64,
                            it.intrinsicHeight.takeIf { h -> h > 0 } ?: 64
                        )
                        val canvas = Canvas(bitmap)
                        it.setBounds(0, 0, canvas.width, canvas.height)
                        it.draw(canvas)
                        bitmap
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun sendNotification(request: NotificationRequest): NotificationResult {
        return try {
            val currentId = ++notificationId

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                currentId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(request.title)
                .setContentText(request.body)
                .setSmallIcon(R.drawable.ic_notification_server)
                .setLargeIcon(getLargeIconBitmap())
                .setContentIntent(pendingIntent)
                .setAutoCancel(request.autoCancel)
                .setOngoing(request.ongoing)
                .setPriority(request.getPriorityLevel())
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build()

            notificationManager.notify(currentId, notification)

            NotificationResult.success(
                message = "Notification sent successfully",
                notificationId = currentId
            )
        } catch (e: SecurityException) {
            NotificationResult.error("Notification permission denied: ${e.message}")
        } catch (e: Exception) {
            NotificationResult.error("Failed to send notification: ${e.message}")
        }
    }
}

data class NotificationResult(
    val success: Boolean,
    val message: String,
    val notificationId: Int? = null,
    val error: String? = null
) {
    companion object {
        fun success(message: String, notificationId: Int) = NotificationResult(
            success = true,
            message = message,
            notificationId = notificationId
        )

        fun error(error: String) = NotificationResult(
            success = false,
            message = "Failed to send notification",
            error = error
        )
    }
}