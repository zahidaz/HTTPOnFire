package com.azzahid.hof.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.azzahid.hof.domain.state.ServerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch


class ServerServiceManager(private val context: Context) {
    private val _service = MutableStateFlow<HttpServerService?>(null)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    val serverStatus: Flow<ServerStatus> = _service.flatMapLatest { service ->
        service?.serverStatus ?: flowOf(ServerStatus.STOPPED)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? HttpServerService.LocalBinder
            _service.value = binder?.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _service.value = null
            isBound = false
        }
    }

    private var isBound = false

    fun bindToService() {
        if (!isBound) {
            val intent = Intent(context, HttpServerService::class.java)
            val success = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (success) {
                isBound = true
            }
        }
    }

    fun unbindFromService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
    }

    fun startServer() {
        val intent = Intent(context, HttpServerService::class.java).apply {
            action = HttpServerService.ACTION_START_SERVER
        }
        context.startService(intent)
    }

    fun stopServer() {
        val intent = Intent(context, HttpServerService::class.java).apply {
            action = HttpServerService.ACTION_STOP_SERVER
        }
        context.startService(intent)
    }

    fun toggleServer() {
        if (_service.value?.serverStatus?.value == ServerStatus.STARTED) {
            stopServer()
        } else {
            startServer()
        }
    }

    fun restartServer() {
        scope.launch {
            val currentService = _service.value
            if (currentService?.serverStatus?.value == ServerStatus.STARTED) {
                stopServer()
                serverStatus.first { it == ServerStatus.STOPPED }

                startServer()
            }
        }
    }
}