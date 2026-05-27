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
import com.choi.assetportfolio.ui.dashboard.DashboardScreen
import com.choi.assetportfolio.ui.dashboard.FinancialDashboardViewModel

class MainActivity : ComponentActivity() {
    // TODO: Hilt 등 DI 의존성이 없으므로, ViewModel Factory를 통해 수동 주입이 필요합니다.
    private lateinit var viewModel: FinancialDashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // viewModel.fetchDashboardData() // 초기화 후 호출 필요
        
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") {
                    if (::viewModel.isInitialized) {
                        DashboardScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
