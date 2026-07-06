/**
 * 앱의 주요 내비게이션 라우트를 정의하는 Sealed Class.
 * 각 화면의 경로, 하단 바 표시 이름(한국어), 머티리얼 3 아이콘을 포함합니다.
 */
package com.choi.assetportfolio.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val displayName: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "대시보드", Icons.Filled.Dashboard)
    object Analysis : Screen("analysis", "수익 분석", Icons.Filled.Analytics)
    object Transactions : Screen("transactions", "거래 내역", Icons.Filled.List)
    object Settings : Screen("settings", "설정", Icons.Filled.Settings)
}
