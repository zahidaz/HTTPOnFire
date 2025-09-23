package com.azzahid.hof.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class ServerServiceManager(private val context: Context) {
    private var httpServerService: HttpServerService? = null
    private var isBound = false

    private val _isConnected = MutableStateFlow(false)
    val isConnected: Flow<Boolean> = _isConnected.asStateFlow()

    val isServerRunning: Flow<Boolean> = _isConnected.flatMapLatest { isConnected ->
        if (isConnected) {
            httpServerService?.isRunning ?: flowOf(false)
        } else {
            flowOf(false)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? HttpServerService.LocalBinder
            httpServerService = binder?.getService()
            isBound = true
            _isConnected.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            httpServerService = null
            isBound = false
            _isConnected.value = false
        }
    }

    fun bindToService() {
        if (!isBound) {
            val intent = Intent(context, HttpServerService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbindFromService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
            _isConnected.value = false
            httpServerService = null
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

    fun restartServerWithNewConfiguration() {
        httpServerService?.restartServerWithConfiguration()
    }
}