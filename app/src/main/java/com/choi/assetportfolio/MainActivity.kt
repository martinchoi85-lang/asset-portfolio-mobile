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
        val assetRepository = AssetRepositoryImpl(postgrest)
        val portfolioRepository = PortfolioRepository(postgrest)
        val calculatePortfolioYieldUseCase = CalculatePortfolioYieldUseCase()
        val getLookthroughAllocationUseCase = GetLookthroughAllocationUseCase()

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                AppLogger.d("ViewModelProvider.Factory - FinancialDashboardViewModel 생성")
                return FinancialDashboardViewModel(
                    assetRepository = assetRepository,
                    portfolioRepository = portfolioRepository,
                    calculatePortfolioYieldUseCase = calculatePortfolioYieldUseCase,
                    getLookthroughAllocationUseCase = getLookthroughAllocationUseCase
                ) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[FinancialDashboardViewModel::class.java]
        AppLogger.d("MainActivity - ViewModel 초기화 완료")
        
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") {
                    if (::viewModel.isInitialized) {
                        AppLogger.d("NavHost - DashboardScreen 진입")
                        DashboardScreen(viewModel = viewModel)
                    } else {
                        AppLogger.e("NavHost - ViewModel이 초기화되지 않음")
                    }
                }
            }
        }
    }
}
