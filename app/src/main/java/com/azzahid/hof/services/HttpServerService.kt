package com.azzahid.hof.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.azzahid.hof.data.database.AppDatabase
import com.azzahid.hof.data.repository.AndroidNetworkRepository
import com.azzahid.hof.data.repository.HttpRequestLogRepository
import com.azzahid.hof.data.repository.RouteRepository
import com.azzahid.hof.data.repository.SettingsRepository
import com.azzahid.hof.domain.model.CIOEmbeddedServer
import com.azzahid.hof.domain.state.ServerStatus
import com.azzahid.hof.features.http.ServerConfigurationService
import io.ktor.server.application.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.reflect.KClass

class HttpServerService : Service() {

    companion object {
        private const val TAG = "HttpServerService"
        const val ACTION_START_SERVER = "START_SERVER"
        const val ACTION_STOP_SERVER = "STOP_SERVER"
        private const val STOP_GRACE_PERIOD_MS = 1000L
        private const val STOP_TIMEOUT_MS = 2000L
    }

    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var serverConfig: ServerConfigurationService
    private lateinit var notificationMgr: HttpServerNotificationManager
    private lateinit var networkRepo: AndroidNetworkRepository

    @Volatile
    private var server: CIOEmbeddedServer? = null

    private val _serverStatus = MutableStateFlow(ServerStatus.STOPPED)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()

    inner class LocalBinder : Binder() {
        fun getService() = this@HttpServerService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        notificationMgr = HttpServerNotificationManager(this)
        networkRepo = AndroidNetworkRepository()

        val db = AppDatabase.getDatabase(this)
        val settings = SettingsRepository(this)
        serverConfig = ServerConfigurationService(
            settings,
            RouteRepository(db.RouteDao(), settings),
            HttpRequestLogRepository(db.httpRequestLogDao())
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")
        when (intent?.action) {
            ACTION_START_SERVER -> startServer()
            ACTION_STOP_SERVER -> {
                stopServer()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startServer() {
        scope.launch {
            if (server == null) {
                startServerInternal()
            } else {
                Log.w(TAG, "Server already running")
            }
        }
    }


    private suspend fun startServerInternal() {
        if (_serverStatus.value == ServerStatus.STARTING || _serverStatus.value == ServerStatus.STARTED) {
            Log.w(TAG, "Server already starting or started")
            return
        }

        try {
            _serverStatus.value = ServerStatus.STARTING
            val newServer = serverConfig.buildConfiguredServer(this@HttpServerService).apply {
                setupServerMonitoring()
            }
            server = newServer
            newServer.startSuspend(wait = true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start server", e)
            _serverStatus.value = ServerStatus.ERROR
            notificationMgr.showNotification("Failed to start: ${e.message}", false)
            server = null
        }
    }

    private fun CIOEmbeddedServer.setupServerMonitoring() {
        monitor.subscribe(ApplicationStarted) {
            _serverStatus.value = ServerStatus.STARTED
            val ipAddress = networkRepo.getLocalIpAddress()
            val msg = "Server started on http://$ipAddress:port"
            notificationMgr.showNotification(msg, true)
            Log.i(TAG, msg)
        }

        monitor.subscribe(ApplicationStopPreparing) {
            _serverStatus.value = ServerStatus.STOPPING
            Log.d(TAG, "Server stopping...")
        }

        monitor.subscribe(ApplicationStopped) {
            _serverStatus.value = ServerStatus.STOPPED
            Log.i(TAG, "Server stopped")
            notificationMgr.clearNotification()
        }
    }

    private fun stopServer() {
        scope.launch { stopServerInternal() }
    }

    private suspend fun stopServerInternal() {
        server?.let { currentServer ->
            try {
                _serverStatus.value = ServerStatus.STOPPING
                currentServer.stopSuspend(STOP_GRACE_PERIOD_MS, STOP_TIMEOUT_MS)
                server = null
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping server", e)
                server = null
                _serverStatus.value = ServerStatus.STOPPED
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runBlocking {
            stopServerInternal()
        }
        scope.cancel()
        Log.d(TAG, "Service destroyed")
    }
}