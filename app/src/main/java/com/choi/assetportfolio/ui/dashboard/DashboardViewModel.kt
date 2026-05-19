package com.choi.assetportfolio.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.choi.assetportfolio.domain.model.DashboardAsset
import com.choi.assetportfolio.domain.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    object Unauthorized : DashboardUiState
    data class Success(val data: List<DashboardAsset>, val isMasked: Boolean) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AssetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    private var isMasked = true

    fun togglePrivacyMode() {
        isMasked = !isMasked
        val currentState = _uiState.value
        if (currentState is DashboardUiState.Success) {
            _uiState.value = currentState.copy(isMasked = isMasked)
        }
    }

    fun checkSessionAndFetch() {
        viewModelScope.launch {
            // TODO: Replace with real session check logic
            val hasSession = false 
            if (!hasSession) {
                _uiState.value = DashboardUiState.Unauthorized
            } else {
                fetchDashboardData()
            }
        }
    }

    private suspend fun fetchDashboardData() {
        _uiState.value = try {
            DashboardUiState.Success(repository.fetchDashboardAssets(), isMasked)
        } catch (e: Exception) {
            DashboardUiState.Error(e.message ?: "Unknown Error")
        }
    }
}
