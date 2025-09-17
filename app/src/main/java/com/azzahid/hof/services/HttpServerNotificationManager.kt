package com.azzahid.hof.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.azzahid.hof.MainActivity
import com.azzahid.hof.R

class HttpServerNotificationManager(
    private val service: Service,
    private val channelId: String = CHANNEL_ID,
    private val notificationId: Int = NOTIFICATION_ID
) {
    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "HttpServerChannel"
    }

    private val notificationManager by lazy {
        service.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "HTTP Server",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(message: String, isServerRunning: Boolean) {
        val intent = Intent(service, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            service, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(service, channelId)
            .setContentTitle("HTTP Server")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(isServerRunning)
            .build()

        try {
            if (isServerRunning) {
                service.startForeground(notificationId, notification)
            } else {
                notificationManager.notify(notificationId, notification)
            }
        } catch (_: Exception) {
            try {
                notificationManager.notify(notificationId, notification)
            } catch (_: Exception) {
            }
        }
    }
}