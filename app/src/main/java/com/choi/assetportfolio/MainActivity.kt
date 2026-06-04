package com.choi.assetportfolio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.jan.supabase.postgrest.postgrest
import com.choi.assetportfolio.data.repository.AssetRepositoryImpl
import com.choi.assetportfolio.data.repository.PortfolioRepository
import com.choi.assetportfolio.domain.usecase.CalculatePortfolioYieldUseCase
import com.choi.assetportfolio.domain.usecase.GetLookthroughAllocationUseCase
import com.choi.assetportfolio.ui.dashboard.DashboardScreen
import com.choi.assetportfolio.ui.dashboard.FinancialDashboardViewModel

import com.choi.assetportfolio.core.util.AppLogger

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: FinancialDashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.d("MainActivity onCreate - 시작")
        
        val postgrest = (application as AssetApplication).supabase.postgrest
        val syncRepository = com.choi.assetportfolio.data.repository.SyncRepository(postgrest)
        val assetRepository = AssetRepositoryImpl(postgrest, syncRepository)
        val portfolioRepository = PortfolioRepository(postgrest)
        val calculatePortfolioYieldUseCase = CalculatePortfolioYieldUseCase()
        val getLookthroughAllocationUseCase = GetLookthroughAllocationUseCase()
        val manualAssetRepository = com.choi.assetportfolio.data.repository.ManualAssetRepository(postgrest)

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(FinancialDashboardViewModel::class.java) -> {
                        AppLogger.d("ViewModelProvider.Factory - FinancialDashboardViewModel 생성")
                        FinancialDashboardViewModel(
                            assetRepository = assetRepository,
                            portfolioRepository = portfolioRepository,
                            calculatePortfolioYieldUseCase = calculatePortfolioYieldUseCase,
                            getLookthroughAllocationUseCase = getLookthroughAllocationUseCase
                        ) as T
                    }
                    modelClass.isAssignableFrom(com.choi.assetportfolio.ui.transactions.TransactionsViewModel::class.java) -> {
                        AppLogger.d("ViewModelProvider.Factory - TransactionsViewModel 생성")
                        com.choi.assetportfolio.ui.transactions.TransactionsViewModel(
                            portfolioRepository = portfolioRepository
                        ) as T
                    }
                    modelClass.isAssignableFrom(com.choi.assetportfolio.ui.management.ManagementHubViewModel::class.java) -> {
                        AppLogger.d("ViewModelProvider.Factory - ManagementHubViewModel 생성")
                        com.choi.assetportfolio.ui.management.ManagementHubViewModel(
                            manualAssetRepository = manualAssetRepository
                        ) as T
                    }
                    modelClass.isAssignableFrom(com.choi.assetportfolio.ui.analysis.AnalysisViewModel::class.java) -> {
                        AppLogger.d("ViewModelProvider.Factory - AnalysisViewModel 생성")
                        com.choi.assetportfolio.ui.analysis.AnalysisViewModel(
                            assetRepository = assetRepository,
                            portfolioRepository = portfolioRepository,
                            getLookthroughAllocationUseCase = getLookthroughAllocationUseCase
                        ) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
        viewModel = ViewModelProvider(this, factory)[FinancialDashboardViewModel::class.java]
        val transactionsViewModel = ViewModelProvider(this, factory)[com.choi.assetportfolio.ui.transactions.TransactionsViewModel::class.java]
        val managementHubViewModel = ViewModelProvider(this, factory)[com.choi.assetportfolio.ui.management.ManagementHubViewModel::class.java]
        val analysisViewModel = ViewModelProvider(this, factory)[com.choi.assetportfolio.ui.analysis.AnalysisViewModel::class.java]
        AppLogger.d("MainActivity - ViewModel 초기화 완료")
        
        setContent {
            if (::viewModel.isInitialized) {
                com.choi.assetportfolio.ui.main.MainContainerScreen(
                    viewModel = viewModel,
                    transactionsViewModel = transactionsViewModel,
                    managementHubViewModel = managementHubViewModel,
                    analysisViewModel = analysisViewModel
                )
            } else {
                AppLogger.e("MainActivity - ViewModel이 초기화되지 않음")
            }
        }
    }
}
