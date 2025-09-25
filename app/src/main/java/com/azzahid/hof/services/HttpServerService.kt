package com.azzahid.hof.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.azzahid.hof.Constants
import com.azzahid.hof.data.database.AppDatabase
import com.azzahid.hof.data.repository.AndroidNetworkRepository
import com.azzahid.hof.data.repository.HttpRequestLogRepository
import com.azzahid.hof.data.repository.NetworkRepository
import com.azzahid.hof.data.repository.RouteRepository
import com.azzahid.hof.data.repository.SettingsRepository
import com.azzahid.hof.domain.model.CIOEmbeddedServer
import com.azzahid.hof.features.http.ServerConfigurationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class HttpServerService : Service() {

    companion object {
        private const val TAG = "HttpServerService"
        const val ACTION_START_SERVER = "START_SERVER"
        const val ACTION_STOP_SERVER = "STOP_SERVER"
    }

    private val binder = LocalBinder()
    private var server: CIOEmbeddedServer? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val serverMutex = Mutex()

    private lateinit var serverConfigurationService: ServerConfigurationService
    private lateinit var notificationManager: HttpServerNotificationManager
    private lateinit var networkRepository: NetworkRepository

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
        serviceScope.launch {
            serverMutex.withLock {
                if (currentRunningState) {
                    Log.d(TAG, "Restarting server with new configuration")
                    startServerInternal("restarted")
                } else {
                    Log.w(TAG, "Restart requested but server is not currently running")
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()

        notificationManager = HttpServerNotificationManager(this)
        networkRepository = AndroidNetworkRepository()

        val database = AppDatabase.getDatabase(this)
        val settingsRepository = SettingsRepository(this)
        serverConfigurationService = ServerConfigurationService(
            settingsRepository,
            RouteRepository(database.RouteDao(), settingsRepository),
            HttpRequestLogRepository(database.httpRequestLogDao())
        )

        currentRunningState = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVER -> startServer()
            ACTION_STOP_SERVER -> {
                stopServer()
                stopSelf()
            }

            null -> Log.w(TAG, "onStartCommand called with null intent")
            else -> Log.w(TAG, "Unknown action: ${intent.action}")
        }
        return START_STICKY
    }

    private fun startServer() {
        serviceScope.launch {
            serverMutex.withLock {
                startServerInternal("running")
            }
        }
    }

    private suspend fun startServerInternal(statusText: String) {
        try {
            stopServerInternal()

            val configuration = serverConfigurationService.getServerConfiguration().firstOrNull()
            val port = configuration?.port ?: Constants.DEFAULT_PORT

            server = serverConfigurationService.buildConfiguredServer(this@HttpServerService)
            server?.start(wait = false)
            currentRunningState = true

            val message =
                "Server $statusText on http://${networkRepository.getLocalIpAddress()}:$port"
            Log.i(TAG, message)
            notificationManager.showNotification(message, true)
        } catch (e: Exception) {
            val errorMessage = "Failed to start server: ${e.message}"
            Log.e(TAG, errorMessage, e)
            currentRunningState = false
            notificationManager.showNotification(errorMessage, false)
        }
    }

    private fun stopServer() {
        serviceScope.launch {
            serverMutex.withLock {
                stopServerInternal()
                Log.i(TAG, "Server stopped")
                notificationManager.clearNotification()
            }
        }
    }

    private suspend fun stopServerInternal() {
        server?.let { serverInstance ->
            try {
                serverInstance.stop(1000, 2000)
                delay(500)
            } catch (e: Exception) {
                Log.w(TAG, "Exception while stopping server", e)
            } finally {
                server = null
                currentRunningState = false
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.launch {
            stopServerInternal()
            notificationManager.clearNotification()
        }
    }
}