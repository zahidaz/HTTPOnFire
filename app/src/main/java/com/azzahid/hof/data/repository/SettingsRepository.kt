package com.azzahid.hof.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.azzahid.hof.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val AUTO_START_KEY = booleanPreferencesKey("auto_start")
        private val BACKGROUND_SERVICE_ENABLED_KEY =
            booleanPreferencesKey("background_service_enabled")
        private val ENABLE_LOGS_KEY = booleanPreferencesKey("enable_logs")
        private val DEFAULT_PORT_KEY = stringPreferencesKey("default_port")
        private val LOG_RETENTION_DAYS_KEY = stringPreferencesKey("log_retention_days")
        private val MAX_LOG_ENTRIES_KEY = stringPreferencesKey("max_log_entries")
        private val AUTO_CLEANUP_ENABLED_KEY = booleanPreferencesKey("auto_cleanup_enabled")
        private val CORS_ALLOW_ANY_HOST_KEY = booleanPreferencesKey("cors_allow_any_host")
        private val CORS_ALLOWED_HOSTS_KEY = stringPreferencesKey("cors_allowed_hosts")
        private val CORS_ALLOW_CREDENTIALS_KEY = booleanPreferencesKey("cors_allow_credentials")
        private val ENABLE_SWAGGER_KEY = booleanPreferencesKey("enable_swagger")
        private val ENABLE_OPENAPI_KEY = booleanPreferencesKey("enable_openapi")
        private val ENABLE_STATUS_KEY = booleanPreferencesKey("enable_status")
        private val ENABLE_NOTIFICATION_KEY = booleanPreferencesKey("enable_notification")
    }

    val autoStart: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[AUTO_START_KEY] ?: false
        }

    val backgroundServiceEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[BACKGROUND_SERVICE_ENABLED_KEY] ?: false
        }

    val enableLogs: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ENABLE_LOGS_KEY] ?: true
        }

    val defaultPort: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[DEFAULT_PORT_KEY] ?: Constants.DEFAULT_PORT_STRING
        }

    val logRetentionDays: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LOG_RETENTION_DAYS_KEY] ?: "7"
        }

    val maxLogEntries: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[MAX_LOG_ENTRIES_KEY] ?: "1000"
        }

    val autoCleanupEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[AUTO_CLEANUP_ENABLED_KEY] ?: true
        }

    val corsAllowAnyHost: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[CORS_ALLOW_ANY_HOST_KEY] ?: false
        }

    val corsAllowedHosts: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[CORS_ALLOWED_HOSTS_KEY] ?: ""
        }

    val corsAllowCredentials: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[CORS_ALLOW_CREDENTIALS_KEY] ?: false
        }

    val enableSwagger: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ENABLE_SWAGGER_KEY] ?: true
        }

    val enableOpenApi: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ENABLE_OPENAPI_KEY] ?: true
        }

    val enableStatus: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ENABLE_STATUS_KEY] ?: true
        }

    val enableNotification: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ENABLE_NOTIFICATION_KEY] ?: true
        }

    suspend fun updateAutoStart(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_START_KEY] = enabled
        }
    }

    suspend fun updateBackgroundServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BACKGROUND_SERVICE_ENABLED_KEY] = enabled
        }
    }

    suspend fun updateEnableLogs(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_LOGS_KEY] = enabled
        }
    }

    suspend fun updateDefaultPort(port: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_PORT_KEY] = port
        }
    }

    suspend fun updateLogRetentionDays(days: String) {
        context.dataStore.edit { preferences ->
            preferences[LOG_RETENTION_DAYS_KEY] = days
        }
    }

    suspend fun updateMaxLogEntries(maxEntries: String) {
        context.dataStore.edit { preferences ->
            preferences[MAX_LOG_ENTRIES_KEY] = maxEntries
        }
    }

    suspend fun updateAutoCleanupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_CLEANUP_ENABLED_KEY] = enabled
        }
    }

    suspend fun updateCorsAllowAnyHost(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CORS_ALLOW_ANY_HOST_KEY] = enabled
        }
    }

    suspend fun updateCorsAllowedHosts(hosts: String) {
        context.dataStore.edit { preferences ->
            preferences[CORS_ALLOWED_HOSTS_KEY] = hosts
        }
    }

    suspend fun updateCorsAllowCredentials(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CORS_ALLOW_CREDENTIALS_KEY] = enabled
        }
    }

    suspend fun updateEnableSwagger(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_SWAGGER_KEY] = enabled
        }
    }

    suspend fun updateEnableOpenApi(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_OPENAPI_KEY] = enabled
        }
    }

    suspend fun updateEnableStatus(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_STATUS_KEY] = enabled
        }
    }

    suspend fun updateEnableNotification(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ENABLE_NOTIFICATION_KEY] = enabled
        }
    }
}