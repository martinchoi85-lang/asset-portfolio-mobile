package com.choi.assetportfolio.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.choi.assetportfolio.domain.model.DashboardAsset
import java.text.NumberFormat
import java.util.Locale

import com.choi.assetportfolio.core.util.AppLogger

@Composable
fun DashboardScreen(
    viewModel: FinancialDashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPrivacyModeEnabled by viewModel.isPrivacyModeEnabled.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()

    AppLogger.d("DashboardScreen recomposed", data = "uiState=$uiState, isPrivacyModeEnabled=$isPrivacyModeEnabled")

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            edgePadding = 16.dp
        ) {
            viewModel.tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { 
                        AppLogger.d("Top Tab Clicked", data = "Tab: $title, Index: $index")
                        viewModel.selectTab(index) 
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "상태: $state", style = MaterialTheme.typography.headlineSmall)
                }
            }
            is DashboardUiState.Empty -> {
                Text(text = "상태: $state", style = MaterialTheme.typography.headlineMedium)
            }
            is DashboardUiState.Error -> {
                Text(
                    text = "상태: $state",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            is DashboardUiState.Success -> {
                DashboardContent(
                    assets = state.data,
                    totalAssetAmount = state.totalAssetAmount,
                    overallReturnRate = state.overallReturnRate,
                    isMasked = isPrivacyModeEnabled,
                    onTogglePrivacyMode = { viewModel.togglePrivacyMode() }
                )
            }
        }
        }
    }
}

@Composable
fun DashboardContent(
    assets: List<DashboardAsset>,
    totalAssetAmount: Double,
    overallReturnRate: Double,
    isMasked: Boolean,
    onTogglePrivacyMode: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.KOREA)
    val totalAssetStr = if (isMasked) "₩ ••••••••" else currencyFormat.format(totalAssetAmount)
    val returnRateStr = String.format(Locale.getDefault(), "%.2f%%", overallReturnRate)
    val returnColor = if (overallReturnRate >= 0) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 요약 카드 (총 자산 및 수익률)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "총 자산 금액",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    IconButton(onClick = onTogglePrivacyMode) {
                        Text(text = if (isMasked) "👁️" else "🙈")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = totalAssetStr,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "수익률: ", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = returnRateStr,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = returnColor
                    )
                }
            }
        }

        Text(
            text = "보유 자산 목록",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(assets) { asset ->
                val assetValueStr = if (isMasked) "₩ ••••••••" else currencyFormat.format(asset.totalValuationAmount)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = asset.nameKr,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${asset.ticker} · ${asset.totalQuantity}주",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = assetValueStr,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
