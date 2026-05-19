package com.choi.assetportfolio.ui.dashboard

import com.choi.assetportfolio.domain.model.Asset

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(val assets: List<Asset>) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
