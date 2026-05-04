package com.cinetrack.presentation.screens.trakt

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cinetrack.data.remote.trakt.TraktAuthManager
import com.cinetrack.data.sync.SyncResult
import com.cinetrack.data.sync.TraktSyncService
import com.cinetrack.worker.TraktSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TraktUiState(
    val isConnected: Boolean = false,
    val username: String? = null,
    val avatarUrl: String? = null,
    val lastSyncTime: Long? = null,
    val isSyncing: Boolean = false,
    val isLoading: Boolean = false,
    val syncResult: SyncResult? = null,
    val error: String? = null
)

@HiltViewModel
class TraktViewModel @Inject constructor(
    application: Application,
    private val authManager: TraktAuthManager,
    private val syncService: TraktSyncService
) : AndroidViewModel(application) {

    private val appContext: Context get() = getApplication<Application>().applicationContext

    private val _syncResult = MutableStateFlow<SyncResult?>(null)
    private val _isSyncing  = MutableStateFlow(false)
    private val _isLoading  = MutableStateFlow(false)
    private val _error      = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TraktUiState> = combine(
        authManager.isConnected,
        authManager.username,
        authManager.avatar,
        authManager.lastSync,
        _isSyncing,
        _isLoading,
        _syncResult,
        _error
    ) { values ->
        TraktUiState(
            isConnected  = values[0] as Boolean,
            username     = values[1] as? String,
            avatarUrl    = values[2] as? String,
            lastSyncTime = (values[3] as Long).takeIf { it > 0 },
            isSyncing    = values[4] as Boolean,
            isLoading    = values[5] as Boolean,
            syncResult   = values[6] as? SyncResult,
            error        = values[7] as? String
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TraktUiState())

    fun startAuth(context: Context) {
        authManager.openAuthPage(context)
    }

    fun handleOAuthCallback(code: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authManager.handleCallback(code)
            if (result.isSuccess) {
                TraktSyncWorker.schedule(appContext)
                syncNow()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Authentication failed"
            }
            _isLoading.value = false
        }
    }

    fun syncNow(context: Context = appContext) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncResult.value = null
            try {
                val result = syncService.fullSync()
                _syncResult.value = result
                TraktSyncWorker.schedule(context)
            } catch (e: Exception) {
                _error.value = e.message
            }
            _isSyncing.value = false
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            TraktSyncWorker.cancel(appContext)
            authManager.disconnect()
            _syncResult.value = null
        }
    }

    fun clearError() { _error.value = null }
}
