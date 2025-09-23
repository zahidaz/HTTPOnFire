package com.azzahid.hof.ui.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azzahid.hof.data.database.AppDatabase
import com.azzahid.hof.data.repository.AndroidNetworkRepository
import com.azzahid.hof.data.repository.HttpRequestLogRepository
import com.azzahid.hof.data.repository.NetworkRepository
import com.azzahid.hof.data.repository.PermissionRepository
import com.azzahid.hof.data.repository.RouteRepository
import com.azzahid.hof.data.repository.SettingsRepository
import com.azzahid.hof.features.http.ServerConfigurationService
import com.azzahid.hof.services.AndroidClipboardService
import com.azzahid.hof.services.AndroidFilePermissionService
import com.azzahid.hof.services.AndroidPermissionService
import com.azzahid.hof.services.AndroidQRCodeService
import com.azzahid.hof.services.ClipboardService
import com.azzahid.hof.services.FilePermissionService
import com.azzahid.hof.services.PermissionService
import com.azzahid.hof.services.QRCodeService
import com.azzahid.hof.services.ServerServiceManager
import com.azzahid.hof.ui.viewmodel.HomeViewModel
import com.azzahid.hof.ui.viewmodel.LogsViewModel
import com.azzahid.hof.ui.viewmodel.MainViewModel
import com.azzahid.hof.ui.viewmodel.PermissionViewModel
import com.azzahid.hof.ui.viewmodel.SettingsViewModel
import com.azzahid.hof.ui.viewmodel.route.ApiRouteBuilderViewModel
import com.azzahid.hof.ui.viewmodel.route.FileSystemRouteBuilderViewModel

class ViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    private val routeRepository by lazy {
        RouteRepository(AppDatabase.getDatabase(application).RouteDao())
    }

    private val settingsRepository by lazy {
        SettingsRepository(application)
    }

    private val serverServiceManager by lazy {
        ServerServiceManager(application)
    }

    private val permissionRepository by lazy {
        PermissionRepository(application)
    }

    private val logRepository by lazy {
        HttpRequestLogRepository(AppDatabase.getDatabase(application).httpRequestLogDao())
    }

    private val clipboardService: ClipboardService by lazy {
        AndroidClipboardService(application)
    }

    private val qrCodeService: QRCodeService by lazy {
        AndroidQRCodeService()
    }

    private val networkRepository: NetworkRepository by lazy {
        AndroidNetworkRepository()
    }

    private val filePermissionService: FilePermissionService by lazy {
        AndroidFilePermissionService(application.contentResolver)
    }

    private val permissionService: PermissionService by lazy {
        AndroidPermissionService(permissionRepository)
    }

    private val serverConfigurationService by lazy {
        ServerConfigurationService(settingsRepository, routeRepository, logRepository)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel() as T
            }

            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    routeRepository = routeRepository,
                    settingsRepository = settingsRepository,
                    serverServiceManager = serverServiceManager,
                    clipboardService = clipboardService,
                    qrCodeService = qrCodeService,
                    networkRepository = networkRepository,
                    serverConfigurationService = serverConfigurationService,
                    application = application
                ) as T
            }

            modelClass.isAssignableFrom(LogsViewModel::class.java) -> {
                LogsViewModel(
                    logRepository = logRepository,
                    settingsRepository = settingsRepository,
                    clipboardService = clipboardService,
                    application = application
                ) as T
            }

            modelClass.isAssignableFrom(PermissionViewModel::class.java) -> {
                PermissionViewModel(
                    permissionRepository = permissionRepository,
                    settingsRepository = settingsRepository,
                    application = application
                ) as T
            }

            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    settingsRepository = settingsRepository,
                    permissionService = permissionService,
                    application = application
                ) as T
            }

            modelClass.isAssignableFrom(ApiRouteBuilderViewModel::class.java) -> {
                ApiRouteBuilderViewModel(routeRepository) as T
            }

            modelClass.isAssignableFrom(FileSystemRouteBuilderViewModel::class.java) -> {
                FileSystemRouteBuilderViewModel(filePermissionService, routeRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}