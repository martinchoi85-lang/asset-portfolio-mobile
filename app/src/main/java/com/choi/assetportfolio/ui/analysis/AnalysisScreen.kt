/**
 * 자산 분석(Analysis) UI 컴포넌트.
 * 상단에는 룩스루가 적용된 실제 노출도를 차트(진행 바)로 표시하고,
 * 하단에는 목표 비중과의 괴리(Gap)를 통한 리밸런싱 가이드를 제공합니다.
 */
package com.choi.assetportfolio.ui.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.choi.assetportfolio.domain.usecase.AllocationResult
import java.util.Locale

@Composable
fun AnalysisScreen(viewModel: AnalysisViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "포트폴리오 다차원 비중 분석",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is AnalysisUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is AnalysisUiState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "분석할 보유 자산이 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            is AnalysisUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "분석 실패: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is AnalysisUiState.Success -> {
                LookthroughAllocationView(allocations = state.allocations)
                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(24.dp))
                RebalancingGuideView(rebalancingItems = state.rebalancingGuide)
            }
        }
    }
}

@Composable
fun LookthroughAllocationView(allocations: List<AllocationResult>) {
    Text(
        text = "실제 노출도 (Look-through 적용)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(16.dp))

    if (allocations.isEmpty()) {
        Text("분석할 자산 데이터가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    // 간단한 막대 그래프 형태의 커스텀 뷰
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val colors = listOf(Color(0xFF5C6BC0), Color(0xFF66BB6A), Color(0xFFFFA726), Color(0xFFEF5350), Color(0xFFAB47BC))
        allocations.forEachIndexed { index, allocation ->
            val weightFraction = allocation.weight.toFloat()
            if (weightFraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(weightFraction)
                        .background(colors[index % colors.size])
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    
    // 범례 표시
    allocations.forEachIndexed { index, allocation ->
        val colors = listOf(Color(0xFF5C6BC0), Color(0xFF66BB6A), Color(0xFFFFA726), Color(0xFFEF5350), Color(0xFFAB47BC))
        val percent = String.format(Locale.getDefault(), "%.1f%%", allocation.weight * 100)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors[index % colors.size])
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${mapAssetClassToKorean(allocation.assetClass)}: $percent",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun RebalancingGuideView(rebalancingItems: List<RebalancingItem>) {
    Text(
        text = "리밸런싱 가이드 (목표 비중 오차)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Gap이 양수(+)이면 매수 권장, 음수(-)이면 매도 권장입니다.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))

    if (rebalancingItems.isEmpty()) {
        Text("설정된 목표 비중 데이터가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rebalancingItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = mapAssetClassToKorean(item.category),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "목표: ${String.format(Locale.getDefault(), "%.1f", item.targetWeight)}% / 실제: ${String.format(Locale.getDefault(), "%.1f", item.actualWeight)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        val gapStr = String.format(Locale.getDefault(), "%.1f%%", item.gap)
                        val (gapColor, gapSign) = if (item.gap > 0) {
                            Pair(MaterialTheme.colorScheme.primary, "+")
                        } else if (item.gap < 0) {
                            Pair(MaterialTheme.colorScheme.error, "")
                        } else {
                            Pair(MaterialTheme.colorScheme.onSurface, "")
                        }
                        
                        Text(
                            text = "Gap: $gapSign$gapStr",
                            fontWeight = FontWeight.Bold,
                            color = gapColor
                        )
                    }
                }
            }
        }
    }
}

fun mapAssetClassToKorean(assetClass: String): String {
    return when (assetClass.uppercase(Locale.getDefault())) {
        "STOCK_KR" -> "국내 주식"
        "STOCK_US_UH" -> "미국 주식 (환노출)"
        "STOCK_US_H" -> "미국 주식 (환헤지)"
        "BOND_KR" -> "국내 채권"
        "BOND_US_UH" -> "미국 채권 (환노출)"
        "BOND_US_H" -> "미국 채권 (환헤지)"
        "CASH_KRW" -> "원화 현금"
        "CASH_USD" -> "달러 현금"
        "GOLD" -> "금"
        else -> assetClass
    }
}
