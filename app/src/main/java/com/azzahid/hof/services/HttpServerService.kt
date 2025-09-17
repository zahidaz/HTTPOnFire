package com.azzahid.hof.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.azzahid.hof.data.database.AppDatabase
import com.azzahid.hof.data.repository.EndpointRepository
import com.azzahid.hof.data.repository.HttpRequestLogRepository
import com.azzahid.hof.data.repository.SettingsRepository
import com.azzahid.hof.features.http.CIOEmbeddedServer
import com.azzahid.hof.features.http.ServerConfigurationService
import com.azzahid.hof.features.http.getLocalIpAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class HttpServerService : Service() {

    companion object {
        private const val TAG = "HttpServerService"
        const val ACTION_START_SERVER = "START_SERVER"
        const val ACTION_STOP_SERVER = "STOP_SERVER"
    }

    private val binder = LocalBinder()
    private var server: CIOEmbeddedServer? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var serverConfigurationService: ServerConfigurationService
    private lateinit var notificationManager: HttpServerNotificationManager

    private val _isRunning = MutableSharedFlow<Boolean>(replay = 1)
    val isRunning: SharedFlow<Boolean> = _isRunning.asSharedFlow()

    private var currentRunningState = false
        set(value) {
            field = value
            serviceScope.launch {
                _isRunning.emit(value)
            }
        }

    inner class LocalBinder : Binder() {
        fun getService(): HttpServerService = this@HttpServerService
    }

    fun restartServerWithConfiguration() {
        Log.d(TAG, "Restarting server with new configuration")
        serviceScope.launch {
            if (currentRunningState) {
                try {
                    stopServerInternal()

                    val configuration =
                        serverConfigurationService.getServerConfiguration().firstOrNull()
                    val port = configuration?.port ?: 8080
                    Log.d(TAG, "Using port $port from configuration for restart")

                    server = serverConfigurationService.buildConfiguredServer()
                    server?.start(wait = false)
                    currentRunningState = true
                    val message = "Server restarted on http://${getLocalIpAddress()}:$port"
                    Log.i(TAG, message)
                    notificationManager.showNotification(message, true)
                } catch (e: Exception) {
                    val errorMessage = "Failed to restart server: ${e.message}"
                    Log.e(TAG, errorMessage, e)
                    currentRunningState = false
                    notificationManager.showNotification(errorMessage, false)
                }
            } else {
                Log.w(TAG, "Restart requested but server is not currently running")
            }
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "HttpServerService created")

        try {
            notificationManager = HttpServerNotificationManager(this)

            val database = AppDatabase.getDatabase(this)
            val settingsRepository = SettingsRepository(this)
            val endpointRepository = EndpointRepository(database.endpointDao())
            val httpRequestLogRepository = HttpRequestLogRepository(database.httpRequestLogDao())

            serverConfigurationService = ServerConfigurationService(
                settingsRepository,
                endpointRepository,
                httpRequestLogRepository
            )

            currentRunningState = false
            Log.d(TAG, "HttpServerService initialization completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize HttpServerService", e)
            throw e
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand called with action: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_SERVER -> {
                Log.i(TAG, "Starting server")
                startServer()
            }

            ACTION_STOP_SERVER -> {
                Log.i(TAG, "Stopping server")
                stopServer()
                stopSelf()
            }

            null -> {
                Log.w(TAG, "onStartCommand called with null intent")
            }

            else -> {
                Log.w(TAG, "Unknown action: ${intent.action}")
            }
        }
        return START_STICKY
    }

    private fun startServer() {
        Log.d(TAG, "Starting server")
        serviceScope.launch {
            try {
                stopServerInternal()

                val configuration =
                    serverConfigurationService.getServerConfiguration().firstOrNull()
                val port = configuration?.port ?: 8080
                Log.d(TAG, "Using port $port from configuration")

                server = serverConfigurationService.buildConfiguredServer()
                server?.start(wait = false)
                currentRunningState = true

                val message = "Server running on http://${getLocalIpAddress()}:$port"
                Log.i(TAG, message)
                notificationManager.showNotification(message, true)
            } catch (e: Exception) {
                val errorMessage = "Failed to start server: ${e.message}"
                Log.e(TAG, errorMessage, e)
                currentRunningState = false
                notificationManager.showNotification(errorMessage, false)
            }
        }
    }

    private fun stopServer() {
        Log.d(TAG, "Stopping server")
        stopServerInternal()
        val message = "Server stopped"
        Log.i(TAG, message)
        notificationManager.showNotification(message, false)
    }

    private fun stopServerInternal() {
        try {
            server?.stop(1000, 2000)
            Log.d(TAG, "Server stopped successfully")
        } catch (e: Exception) {
            Log.w(TAG, "Exception while stopping server", e)
        } finally {
            server = null
            currentRunningState = false
        }
    }


    override fun onDestroy() {
        Log.d(TAG, "HttpServerService destroyed")
        super.onDestroy()
        stopServerInternal()
        Log.d(TAG, "HttpServerService cleanup completed")
    }
}