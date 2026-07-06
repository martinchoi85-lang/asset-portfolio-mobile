package com.choi.assetportfolio.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.style.TextAlign
import com.choi.assetportfolio.domain.model.DashboardAsset
import com.choi.assetportfolio.domain.usecase.AllocationResult
import java.text.NumberFormat
import java.util.Locale
import com.choi.assetportfolio.core.util.AppLogger

@Composable
fun DashboardScreen(
    viewModel: FinancialDashboardViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPrivacyModeEnabled by viewModel.isPrivacyModeEnabled.collectAsStateWithLifecycle()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsStateWithLifecycle()
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()

    AppLogger.d("DashboardScreen recomposed", data = "uiState=$uiState, isPrivacyModeEnabled=$isPrivacyModeEnabled")

    val context = androidx.compose.ui.platform.LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { 
                        if (title == "+계좌추가") {
                            android.widget.Toast.makeText(context, "Account creation UI pending specification sheet", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            AppLogger.d("Top Tab Clicked", data = "Tab: $title, Index: $index")
                            viewModel.selectTab(index) 
                        }
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
                AppLogger.d("DashboardScreen Rendering", data = "State: Loading")
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "상태: $state", style = MaterialTheme.typography.headlineSmall)
                }
            }
            is DashboardUiState.Empty -> {
                AppLogger.d("DashboardScreen Rendering", data = "State: Empty")
                Text(text = "상태: $state", style = MaterialTheme.typography.headlineMedium)
            }
            is DashboardUiState.Error -> {
                AppLogger.d("DashboardScreen Rendering", data = "State: Error ($state)")
                Text(
                    text = "상태: $state",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            is DashboardUiState.Success -> {
                AppLogger.d("DashboardScreen Rendering", data = "State: Success (Assets count: ${state.data.size})")
                val selectedRange by viewModel.selectedRange.collectAsStateWithLifecycle()
                DashboardContent(
                    assets = state.data,
                    totalAssetAmount = state.totalAssetAmount,
                    overallReturnRate = state.overallReturnRate,
                    allocations = state.allocations,
                    trendData = state.trendData,
                    bestPerformingAsset = state.bestPerformingAsset,
                    selectedRange = selectedRange,
                    onRangeSelected = { viewModel.selectRange(it) },
                    isMasked = isPrivacyModeEnabled,
                    onTogglePrivacyMode = { viewModel.togglePrivacyMode() },
                    isTotalAccount = selectedTabIndex == 0,
                    hasUsdAccount = state.hasUsdAccount,
                    isUsdDisplayPreferred = state.isUsdDisplayPreferred,
                    onToggleCurrencyDisplay = { viewModel.toggleCurrencyDisplay() }
                )
            }
        }
        }
    }
}



// --- 테마 컬러 (DashboardSample 참조) ---
val PrimaryRed = Color(0xFFAF101A)
val LightPrimaryRed = Color(0xFFFFEBEC)
val BackgroundColor = Color(0xFFF9F9F9)
val OnSurfaceColor = Color(0xFF1A1C1C)
val CardBackground = Color(0xFFFFFFFF)
val CustomBlue = Color(0xFF005FAF)
val CustomGray = Color(0xFF455B65)
val LightGray = Color(0xFFF3F3F3)

data class AllocationData(val name: String, val value: Double, val color: Color, val percentage: Float)

fun mapAllocationResultToData(allocations: List<AllocationResult>): List<AllocationData> {
    val colors = listOf(PrimaryRed, CustomBlue, CustomGray, Color(0xFFF57C00), Color(0xFF388E3C))
    return allocations.mapIndexed { index, result ->
        AllocationData(
            name = result.assetClass.uppercase(),
            value = result.totalAmount,
            color = colors[index % colors.size],
            percentage = result.weight.toFloat()
        )
    }
}

@Composable
fun DashboardContent(
    assets: List<DashboardAsset>,
    totalAssetAmount: Double,
    overallReturnRate: Double,
    allocations: List<AllocationResult>,
    trendData: List<TrendData>,
    bestPerformingAsset: BestAssetData?,
    selectedRange: String,
    onRangeSelected: (String) -> Unit,
    isMasked: Boolean,
    onTogglePrivacyMode: () -> Unit,
    isTotalAccount: Boolean,
    hasUsdAccount: Boolean,
    isUsdDisplayPreferred: Boolean,
    onToggleCurrencyDisplay: () -> Unit
) {
    val totalAssetStr = if (isMasked) {
        if (isUsdDisplayPreferred) "$ ••••••••" else "₩ ••••••••"
    } else {
        if (isUsdDisplayPreferred) {
            val usdFormat = NumberFormat.getCurrencyInstance(Locale.US)
            usdFormat.format(totalAssetAmount)
        } else {
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.KOREA)
            currencyFormat.format(totalAssetAmount)
        }
    }
    val returnRateStr = String.format(Locale.getDefault(), "%+.2f%%", overallReturnRate)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(16.dp)
    ) {
        // --- 총 자산 금액 (Sample 스타일 참고) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "TOTAL BALANCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle Visibility",
                    tint = Color.LightGray,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onTogglePrivacyMode() }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = totalAssetStr,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurfaceColor
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(LightPrimaryRed)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = returnRateStr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed
                        )
                    }
                }
                
                // Toggle Button (Only for USD accounts)
                if (hasUsdAccount) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray.copy(alpha = 0.5f))
                            .clickable { onToggleCurrencyDisplay() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isUsdDisplayPreferred) "USD" else "KRW",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isTotalAccount) {
                // --- 대시보드 컴포넌트 구성 ---
                item {
                    AssetTrendCardView(
                        trendList = trendData,
                        selectedRange = selectedRange,
                        onRangeSelected = onRangeSelected
                    )
                }

                item {
                    PerformanceInsightCardView(bestAsset = bestPerformingAsset)
                }

                if (allocations.isNotEmpty()) {
                    item {
                        PortfolioAllocationCardView(allocations = mapAllocationResultToData(allocations), isMasked = isMasked)
                    }
                }
            } else {
                // --- 개별 계좌 상세 뷰 ---
                item {
                    ProfitPerformanceCardView(
                        trendList = trendData,
                        selectedRange = selectedRange,
                        onRangeSelected = onRangeSelected
                    )
                }

                if (allocations.isNotEmpty()) {
                    item {
                        AssetAllocationBarCardView(allocations = mapAllocationResultToData(allocations), isMasked = isMasked)
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Holdings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OnSurfaceColor
                        )
                    }
                }

                items(assets.size) { index ->
                    val asset = assets[index]
                    val isAlternating = index % 2 != 0
                    ActiveHoldingItemView(asset = asset, isAlternating = isAlternating, isMasked = isMasked)
                }
            }
        }
    }
}

@Composable
fun PortfolioAllocationCardView(allocations: List<AllocationData>, isMasked: Boolean) {
    AppLogger.d("PortfolioAllocationCardView Rendering", data = "allocations count: ${allocations.size}")
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.KOREA)
    
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Portfolio Allocation", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurfaceColor)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { }
                ) {
                    Text("Details", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryRed)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Details",
                        tint = PrimaryRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        allocations.forEach { model ->
                            val sweepAngle = model.percentage * 360f
                            drawArc(
                                color = model.color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle - 4f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                            startAngle += sweepAngle
                        }
                    }
                    val largest = allocations.maxByOrNull { it.percentage }
                    if (largest != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (largest.name.length > 8) largest.name.take(6) + ".." else largest.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "${(largest.percentage * 100).toInt()}%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnSurfaceColor
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    allocations.take(4).forEach { model ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(model.color)
                                )
                                Text(
                                    text = if (model.name.length > 10) model.name.take(8) + ".." else model.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            }
                            val valStr = if (isMasked) "₩ •••••" else currencyFormat.format(model.value)
                            Text(
                                text = valStr,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnSurfaceColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfitPerformanceCardView(
    trendList: List<TrendData>,
    selectedRange: String,
    onRangeSelected: (String) -> Unit
) {

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Profit Performance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = OnSurfaceColor
                    )
                    Text(
                        text = "Accumulated P/L"
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("1W", "1M", "3M", "6M", "1Y", "ALL").forEach { period ->
                        val isSelected = selectedRange == period
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) PrimaryRed else Color.Transparent)
                                .clickable { onRangeSelected(period) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = period,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (trendList.isEmpty()) {
                    Text(text = "No Data", color = Color.Gray, fontSize = 12.sp)
                } else {
                    val textMeasurer = rememberTextMeasurer()
                    Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp)) {
                        val width = size.width
                        val height = size.height
                        
                        val minVal = trendList.minOfOrNull { it.value } ?: 0f
                        val maxVal = trendList.maxOfOrNull { it.value } ?: 100f
                        val valRange = if (maxVal > minVal) (maxVal - minVal) else 1f

                        val yBaseline = height * 0.6f
                        drawLine(
                            color = PrimaryRed,
                            start = androidx.compose.ui.geometry.Offset(0f, yBaseline),
                            end = androidx.compose.ui.geometry.Offset(width, yBaseline),
                            strokeWidth = 1.5.dp.toPx()
                        )

                        val barCount = trendList.size
                        val gap = 4.dp.toPx()
                        val totalGaps = (barCount - 1) * gap
                        val barWidth = (width - totalGaps) / barCount

                        trendList.forEachIndexed { i, point ->
                            val ratio = (point.value - minVal) / valRange
                            val barHeight = height * ratio
                            val x = i * (barWidth + gap)
                            val y = height - barHeight
                            
                            drawRect(
                                color = PrimaryRed.copy(alpha = 0.4f),
                                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                            )
                        }
                        
                        val maxValText = String.format("%.1fM", maxVal / 1000000)
                        val minValText = String.format("%.1fM", minVal / 1000000)
                        
                        drawText(
                            textMeasurer = textMeasurer,
                            text = maxValText,
                            topLeft = androidx.compose.ui.geometry.Offset(width - 50.dp.toPx(), 0f),
                            style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = Color.Gray)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = minValText,
                            topLeft = androidx.compose.ui.geometry.Offset(width - 50.dp.toPx(), yBaseline + 4.dp.toPx()),
                            style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = Color.Gray)
                        )

                        if (trendList.size >= 3) {
                            val startText = trendList.first().day
                            val midText = trendList[trendList.size / 2].day
                            val endText = trendList.last().day
                            
                            drawText(
                                textMeasurer = textMeasurer,
                                text = startText,
                                topLeft = androidx.compose.ui.geometry.Offset(0f, height + 5.dp.toPx()),
                                style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = Color.Gray)
                            )
                            drawText(
                                textMeasurer = textMeasurer,
                                text = midText,
                                topLeft = androidx.compose.ui.geometry.Offset(width / 2f - 20.dp.toPx(), height + 5.dp.toPx()),
                                style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = Color.Gray)
                            )
                            drawText(
                                textMeasurer = textMeasurer,
                                text = endText,
                                topLeft = androidx.compose.ui.geometry.Offset(width - 40.dp.toPx(), height + 5.dp.toPx()),
                                style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, color = Color.Gray)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssetAllocationBarCardView(allocations: List<AllocationData>, isMasked: Boolean) {
    AppLogger.d("AssetAllocationBarCardView Rendering", data = "allocations count: ${allocations.size}")
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.KOREA)
    val totalAmount = allocations.sumOf { it.value }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Asset Allocation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = OnSurfaceColor
                    )
                    Text(
                        text = "Distribution by sector",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                allocations.forEach { data ->
                    val ratio = if (totalAmount > 0) (data.value / totalAmount).toFloat() else 0f
                    val percentageStr = "${(ratio * 100).toInt()}%"
                    SectorMetricView(
                        label = data.name,
                        valueText = percentageStr,
                        progressRatio = ratio,
                        trackColor = data.color
                    )
                }
            }
        }
    }
}

@Composable
fun SectorMetricView(
    label: String,
    valueText: String,
    progressRatio: Float,
    trackColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
            Text(text = valueText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progressRatio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(100.dp)),
            color = trackColor,
            trackColor = LightGray,
        )
    }
}

@Composable
fun ActiveHoldingItemView(asset: DashboardAsset, isAlternating: Boolean, isMasked: Boolean) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.KOREA)
    val backgroundColor = if (isAlternating) LightGray.copy(alpha = 0.4f) else CardBackground
    val assetValueStr = if (isMasked) {
        if (asset.currency.equals("usd", ignoreCase = true)) "$ ••••••••" else "₩ ••••••••"
    } else {
        if (asset.currency.equals("usd", ignoreCase = true)) {
            val formatUsd = java.text.DecimalFormat("$#,###.##")
            formatUsd.format(asset.totalValuationAmount)
        } else {
            currencyFormat.format(asset.totalValuationAmount)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { /* Detail navigation */ }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (asset.ticker.length > 4) asset.ticker.take(4) else asset.ticker,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                }

                Column {
                    Text(
                        text = if (asset.nameKr.length > 10) asset.nameKr.take(10) + ".." else asset.nameKr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                    Text(
                        text = "Qty: ${asset.totalQuantity}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = assetValueStr,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceColor
                )
            }
        }
    }
}

// 1. 자산 추이 그래프 컴포넌트
@Composable
fun AssetTrendCardView(
    trendList: List<TrendData>,
    selectedRange: String,
    onRangeSelected: (String) -> Unit
) {
    AppLogger.d("AssetTrendCardView Rendering", data = "trendList count: ${trendList.size}")
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Asset Trend", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurfaceColor)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightGray)
                        .padding(2.dp)
                ) {
                    listOf("1W", "1M", "3M", "6M", "1Y", "ALL").forEach { range ->
                        val isSelected = selectedRange == range
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CardBackground else Color.Transparent)
                                .clickable { onRangeSelected(range) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = range,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) OnSurfaceColor else Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp)) {
                    val width = size.width
                    val height = size.height
                    
                    val minVal = trendList.minOfOrNull { it.value } ?: 0f
                    val maxVal = trendList.maxOfOrNull { it.value } ?: 100f
                    val valRange = if (maxVal > minVal) (maxVal - minVal) else 1f

                    val yBaseline = height * 0.9f
                    drawLine(
                        color = PrimaryRed,
                        start = androidx.compose.ui.geometry.Offset(0f, yBaseline),
                        end = androidx.compose.ui.geometry.Offset(width, yBaseline),
                        strokeWidth = 1.dp.toPx()
                    )

                    if (trendList.isNotEmpty()) {
                        val barCount = trendList.size
                        val gap = 4.dp.toPx()
                        val totalGaps = (barCount - 1) * gap
                        val barWidth = (width - totalGaps) / barCount

                        trendList.forEachIndexed { i, point ->
                            val ratio = (point.value - minVal) / valRange
                            val barHeight = (height * 0.8f) * ratio
                            val x = i * (barWidth + gap)
                            val y = yBaseline - barHeight
                            
                            drawRect(
                                color = PrimaryRed.copy(alpha = 0.5f),
                                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalAlignment = Alignment.End
                ) {
                    val minVal = trendList.minOfOrNull { it.value } ?: 0f
                    val maxVal = trendList.maxOfOrNull { it.value } ?: 100f
                    val midVal = (maxVal + minVal) / 2
                    Text(String.format("%.1fM", maxVal/1000000), fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(String.format("%.1fM", midVal/1000000), fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(String.format("%.1fM", minVal/1000000), fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (trendList.isNotEmpty()) {
                    val step = maxOf(1, trendList.size / 5)
                    trendList.filterIndexed { index, _ -> index % step == 0 }.forEach { label ->
                        Text(
                            text = label.day,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// 2. 실시간 인사이트 카드 컴포넌트
@Composable
fun PerformanceInsightCardView(bestAsset: BestAssetData?) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PrimaryRed),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Insight Icon",
                    tint = Color.White.copy(alpha = 0.82f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "PERFORMANCE INSIGHT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = bestAsset?.assetName ?: "-", 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Top Performing Asset", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    val rateText = bestAsset?.let {
                        if (it.returnRate > 0) String.format("+%.1f%%", it.returnRate)
                        else String.format("%.1f%%", it.returnRate)
                    } ?: "-%"
                    Text(
                        text = rateText, 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(bestAsset?.periodLabel?.uppercase() ?: "-", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}
