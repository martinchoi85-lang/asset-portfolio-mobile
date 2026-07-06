package com.example.portfolio

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- 테마 컬러 정의 ---
val PrimaryRed = Color(0xFFAF101A)
val LightPrimaryRed = Color(0xFFFFEBEC)
val BackgroundColor = Color(0xFFF9F9F9)
val OnSurfaceColor = Color(0xFF1A1C1C)
val CardBackground = Color(0xFFFFFFFF)
val CustomBlue = Color(0xFF005FAF)
val CustomGray = Color(0xFF455B65)
val LightGray = Color(0xFFF3F3F3)

data class TrendData(val day: String, val value: Float)
data class AllocationData(val name: String, val value: Long, val color: Color, val percentage: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioDashboardScreen() {
    var selectedTab by remember { mutableStateOf("Total") }
    var isBalanceVisible by remember { mutableStateOf(true) }
    val tabs = listOf("Total", "Kookmin", "Mirae Asset", "Shinhan")

    val trendList = listOf(
        TrendData("MON", 132f),
        TrendData("TUE", 134f),
        TrendData("WED", 133f),
        TrendData("THU", 138f),
        TrendData("FRI", 142f),
        TrendData("SAT", 145f),
        TrendData("SUN", 148f)
    )

    val allocations = listOf(
        AllocationData("Domestic Stocks", 64282500L, PrimaryRed, 0.45f),
        AllocationData("Overseas Stocks", 42855000L, CustomBlue, 0.30f),
        AllocationData("Cash & Deposit", 35712500L, CustomGray, 0.25f)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        ) {
                            Text(
                                "M", 
                                modifier = Modifier.align(Alignment.Center),
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceColor
                            )
                        }
                        Text(
                            text = "Portfolio",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = OnSurfaceColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBackground)
            )
        },
        bottomBar = { BottomNavigationBar() },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // --- 가로 백화점/뱅킹 탭 영역 ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedTab = tab }
                    ) {
                        Text(
                            text = tab,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PrimaryRed else Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(2.dp)
                                    .background(PrimaryRed)
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Tab",
                    tint = Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            // --- 총 계좌 잔액 ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
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
                            .clickable { isBalanceVisible = !isBalanceVisible }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isBalanceVisible) "₩ 142,850,000" else "₩ ••••••••",
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
                            text = "+2.4%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Updated 2 minutes ago", fontSize = 11.sp, color = Color.Gray)
            }

            // --- 카드 목록 배치 ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AssetTrendCardView(trendList = trendList)
                PerformanceInsightCardView()
                RiskProfileCardView()
                PortfolioAllocationCardView(allocations = allocations)
                AddWidgetCardView()
            }
        }
    }
}

// 1. 자산 추이 그래프 컴포넌트
@Composable
fun AssetTrendCardView(trendList: List<TrendData>) {
    var selectedRange by remember { mutableStateOf("1W") }
    
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
                    listOf("1W", "1M", "3M").forEach { range ->
                        val isSelected = selectedRange == range
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CardBackground else Color.Transparent)
                                .clickable { selectedRange = range }
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
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / (trendList.size - 1)

                    val maxVal = 150f
                    val minVal = 130f
                    val points = trendList.mapIndexed { idx, point ->
                        val x = idx * spacing
                        val ratio = (point.value - minVal) / (maxVal - minVal)
                        val y = height - (ratio * height)
                        Offset(x, y)
                    }

                    // 하단 그라데이션 영역 채우기
                    val fillPath = Path().apply {
                        moveTo(0f, height)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(width, height)
                        close()
                    }
                    clipPath(fillPath) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(PrimaryRed.copy(alpha = 0.15f), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )
                    }

                    // 부드러운 Bezier 곡선 렌더링
                    val strokePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val pPrev = points[i - 1]
                            val pCurr = points[i]
                            val cp1 = Offset(pPrev.x + spacing / 2, pPrev.y)
                            val cp2 = Offset(pCurr.x - spacing / 2, pCurr.y)
                            cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, pCurr.x, pCurr.y)
                        }
                    }
                    drawPath(
                        path = strokePath,
                        color = PrimaryRed,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("150M", fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(40.dp))
                    Text("140M", fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(40.dp))
                    Text("130M", fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trendList.forEach { label ->
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

// 2. 실시간 인사이트 카드 컴포넌트
@Composable
fun PerformanceInsightCardView() {
    Card(
        colors = CardDefaults.cardColors(containerColor = PrimaryRed),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
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
                Column {
                    Text("NVIDIA", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Top Performing Asset", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("+8.5%", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("TODAY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

// 3. 투자 성향 분석 카드 컴포넌트
@Composable
fun RiskProfileCardView() {
    Card(
        colors = CardDefaults.cardColors(containerColor = LightGray),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "RISK PROFILE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.size(48.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        drawArc(
                            color = PrimaryRed,
                            startAngle = -90f,
                            sweepAngle = 260f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "Med",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Column {
                    Text("Balanced Growth", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurfaceColor)
                    Text("Conservative approach recommended for this week.", fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                }
            }
        }
    }
}

// 4. 포트폴리오 비중 차트 컴포넌트
@Composable
fun PortfolioAllocationCardView(allocations: List<AllocationData>) {
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("STOCKS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("45%", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = OnSurfaceColor)
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    allocations.forEach { model ->
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
                                Text(model.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Text("₩ ${formatAmount(model.value)}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = OnSurfaceColor)
                        }
                    }
                }
            }
        }
    }
}

// 5. 대시보드 위젯 추가 빈 카드
@Composable
fun AddWidgetCardView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 2.dp, color = Color.LightGray.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { }
            .background(LightGray.copy(alpha = 0.3f))
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LightGray)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Widget",
                    tint = Color.Gray,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text("ADD WIDGET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Real estate, Crypto, or Loan data", fontSize = 10.sp, color = Color.LightGray)
        }
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(
        containerColor = CardBackground,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Dashboard") },
            label = { Text("Dashboard", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryRed,
                selectedTextColor = PrimaryRed,
                indicatorColor = LightPrimaryRed
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(imageVector = Icons.Default.Info, contentDescription = "Trade History") },
            label = { Text("Trade History", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(imageVector = Icons.Default.Menu, contentDescription = "Manage") },
            label = { Text("Manage", fontSize = 10.sp) }
        )
    }
}

fun formatAmount(value: Long): String {
    return java.text.DecimalFormat("#,###").format(value)
}

@Preview(showBackground = true)
@Composable
fun PortfolioDashboardPreview() {
    MaterialTheme {
        PortfolioDashboardScreen()
    }
}