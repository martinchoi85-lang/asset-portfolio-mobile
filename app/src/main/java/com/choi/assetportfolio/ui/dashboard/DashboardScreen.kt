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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val uiState by viewModel.uiState.collectAsState()
    val isPrivacyModeEnabled by viewModel.isPrivacyModeEnabled.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val tabs by viewModel.tabs.collectAsState()

    AppLogger.d("DashboardScreen recomposed", data = "uiState=$uiState, isPrivacyModeEnabled=$isPrivacyModeEnabled")

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
                DashboardContent(
                    assets = state.data,
                    totalAssetAmount = state.totalAssetAmount,
                    overallReturnRate = state.overallReturnRate,
                    allocations = state.allocations,
                    isMasked = isPrivacyModeEnabled,
                    onTogglePrivacyMode = { viewModel.togglePrivacyMode() },
                    isTotalAccount = selectedTabIndex == 0
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
    isMasked: Boolean,
    onTogglePrivacyMode: () -> Unit,
    isTotalAccount: Boolean
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.KOREA)
    val totalAssetStr = if (isMasked) "₩ ••••••••" else currencyFormat.format(totalAssetAmount)
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
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isTotalAccount) {
                // --- 포트폴리오 비중 카드 ---
                if (allocations.isNotEmpty()) {
                    item {
                        PortfolioAllocationCardView(allocations = mapAllocationResultToData(allocations), isMasked = isMasked)
                    }
                }

                item {
                    Text(
                        text = "보유 자산 목록",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }

                // --- 보유 자산 리스트 ---
                items(assets) { asset ->
                    val assetValueStr = if (isMasked) "₩ ••••••••" else currencyFormat.format(asset.totalValuationAmount)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = asset.nameKr,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceColor
                                )
                                Text(
                                    text = "${asset.ticker} · ${asset.totalQuantity}주",
                                    fontSize = 11.sp,
                                    color = CustomGray
                                )
                            }
                            Text(
                                text = assetValueStr,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = OnSurfaceColor
                            )
                        }
                    }
                }
            } else {
                // --- 개별 계좌 상세 뷰 ---
                item {
                    ProfitPerformanceCardView()
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
fun ProfitPerformanceCardView() {
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
                        text = "Accumulated P/L over 30 days",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Drag widget",
                    tint = Color.LightGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    val yBaseline = height * 0.6f
                    drawLine(
                        color = PrimaryRed,
                        start = androidx.compose.ui.geometry.Offset(0f, yBaseline),
                        end = androidx.compose.ui.geometry.Offset(width, yBaseline),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    val barCount = 6
                    val gap = 12.dp.toPx()
                    val totalGaps = (barCount - 1) * gap
                    val barWidth = (width - totalGaps) / barCount
                    
                    val heightRatios = listOf(0.7f, 0.45f, 0.65f, 0.75f, 0.45f, 0.95f)

                    for (i in 0 until barCount) {
                        val barHeight = height * heightRatios[i]
                        val x = i * (barWidth + gap)
                        val y = height - barHeight
                        
                        drawRect(
                            color = PrimaryRed.copy(alpha = 0.18f),
                            topLeft = androidx.compose.ui.geometry.Offset(x, y),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssetAllocationBarCardView(allocations: List<AllocationData>, isMasked: Boolean) {
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
    val assetValueStr = if (isMasked) "₩ ••••••••" else currencyFormat.format(asset.totalValuationAmount)

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
