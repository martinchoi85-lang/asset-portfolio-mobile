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
import com.choi.assetportfolio.ui.dashboard.DashboardUiState
import com.choi.assetportfolio.ui.dashboard.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.checkSessionAndFetch()
        
        setContent {
            val navController = rememberNavController()
            val uiState by viewModel.uiState.collectAsState()

            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") {
                    when (val state = uiState) {
                        is DashboardUiState.Loading -> CircularProgressIndicator()
                        is DashboardUiState.Unauthorized -> {
                            // Redirect to login logic
                            Text("로그인이 필요합니다.")
                        }
                        is DashboardUiState.Error -> Text("Error: ${state.message}")
                        is DashboardUiState.Success -> {
                            Column {
                                Button(onClick = { viewModel.togglePrivacyMode() }) {
                                    Text("Toggle Privacy Mode")
                                }
                                LazyColumn {
                                    items(state.data) { asset ->
                                        val displayValue = if (state.isMasked) "****" else asset.amount.toString()
                                        Text(text = "${asset.name}: $displayValue")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
