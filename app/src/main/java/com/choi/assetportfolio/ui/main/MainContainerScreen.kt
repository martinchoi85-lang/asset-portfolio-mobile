/**
 * 전역 내비게이션 컨테이너 역할을 하는 메인 스크린.
 * 하단 네비게이션 바를 포함하며, 선택된 메뉴에 따라 NavHost를 통해 화면을 전환합니다.
 */
package com.choi.assetportfolio.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.choi.assetportfolio.core.util.AppLogger
import com.choi.assetportfolio.ui.dashboard.DashboardScreen
import com.choi.assetportfolio.ui.dashboard.FinancialDashboardViewModel
import com.choi.assetportfolio.ui.navigation.Screen

@Composable
fun MainContainerScreen(
    viewModel: FinancialDashboardViewModel,
    transactionsViewModel: com.choi.assetportfolio.ui.transactions.TransactionsViewModel,
    managementHubViewModel: com.choi.assetportfolio.ui.management.ManagementHubViewModel,
    analysisViewModel: com.choi.assetportfolio.ui.analysis.AnalysisViewModel
) {
    val navController = rememberNavController()
    val screens = listOf(
        Screen.Dashboard,
        Screen.Analysis,
        Screen.Transactions,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentRoute = currentDestination?.route ?: "unknown"

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.displayName) },
                        label = { Text(screen.displayName) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            if (currentRoute != screen.route) {
                                AppLogger.d("Navigation Clicked", data = "From [$currentRoute] To [${screen.route}]")
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = viewModel)
            }
            composable(Screen.Analysis.route) {
                com.choi.assetportfolio.ui.analysis.AnalysisScreen(viewModel = analysisViewModel)
            }
            composable(Screen.Transactions.route) {
                com.choi.assetportfolio.ui.transactions.TransactionsScreen(viewModel = transactionsViewModel)
            }
            composable(Screen.Settings.route) {
                com.choi.assetportfolio.ui.management.ManagementHubScreen(viewModel = managementHubViewModel)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$title 화면 (준비 중)", style = MaterialTheme.typography.headlineMedium)
    }
}
