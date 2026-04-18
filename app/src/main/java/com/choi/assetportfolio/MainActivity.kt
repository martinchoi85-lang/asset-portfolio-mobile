package com.choi.assetportfolio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

// --- 1. 테마 색상 (index.css 기준) ---
val PrimaryRed = Color(0xFFAF101A)
val PrimaryBlue = Color(0xFF005FAF)
val TextDark = Color(0xFF171717)
val TextGray = Color(0xFF737373)
val BgLight = Color(0xFFFAFAFA)
val SurfaceWhite = Color(0xFFFFFFFF)

// --- 2. 데이터 모델 (Supabase 스키마 기반) ---
// DB의 `accounts`와 `asset_summary_live` 테이블이 조인된 상태를 가정
data class DashboardState(
    val totalValuation: Double = 142850000.0,
    val totalReturnRate: Double = 2.4,
    val accounts: List<String> = listOf("Total", "Kookmin", "Mirae Asset", "Shinhan"),
    val selectedAccount: String = "Total",
    val allocation: List<AllocationItem> = listOf(
        AllocationItem("Domestic Stocks", 64282500.0, PrimaryRed),
        AllocationItem("Overseas Stocks", 42855000.0, PrimaryBlue),
        AllocationItem("Cash & Deposit", 35712500.0, Color(0xFF455B65))
    )
)

data class AllocationItem(val name: String, val value: Double, val color: Color)

// --- 3. 메인 액티비티 ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DashboardScreen()
            }
        }
    }
}

// --- 4. 메인 화면 컴포저블 ---
@Composable
fun DashboardScreen() {
    // 실제 환경에서는 ViewModel에서 state를 구독 (collectAsState)
    var state by remember { mutableStateOf(DashboardState()) }
    val scrollState = rememberScrollState()

    Scaffold(
        bottomBar = { BottomNavigationBar() },
        containerColor = BgLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            Header()
            AccountTabs(
                tabs = state.accounts,
                selectedTab = state.selectedAccount,
                onTabSelected = { state = state.copy(selectedAccount = it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            BalanceSection(state.totalValuation, state.totalReturnRate)
            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                AssetTrendCard()
                Spacer(modifier = Modifier.height(16.dp))
                PerformanceInsightCard()
                Spacer(modifier = Modifier.height(16.dp))
                RiskProfileCard()
                Spacer(modifier = Modifier.height(16.dp))
                AllocationCard(state.allocation)
                Spacer(modifier = Modifier.height(16.dp))
                AddWidgetCard()
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Portfolio", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
        IconButton(onClick = { /* TODO */ }) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextGray)
        }
    }
}

@Composable
fun AccountTabs(tabs: List<String>, selectedTab: String, onTabSelected: (String) -> Unit) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            Column(
                modifier = Modifier
                    .clickable { onTabSelected(tab) }
                    .padding(end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tab,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) PrimaryRed else TextGray
                )
                if (isSelected) {
                    Box(modifier = Modifier.padding(top = 4.dp).height(2.dp).width(24.dp).background(PrimaryRed))
                }
            }
        }
        IconButton(onClick = { /* 계좌 추가 */ }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Add, contentDescription = "Add Account", tint = TextGray)
        }
    }
}

@Composable
fun BalanceSection(totalValuation: Double, returnRate: Double) {
    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("TOTAL BALANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.Visibility, contentDescription = "Hide", modifier = Modifier.size(14.dp), tint = TextGray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("₩ ${formatter.format(totalValuation)}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = Color(0xFFFEF2F2),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    "+$returnRate%",
                    color = PrimaryRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Text("Updated 2 minutes ago", fontSize = 11.sp, color = TextGray, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun AssetTrendCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Border handled by modifier in real app
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Asset Trend", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                // 1W, 1M, 3M 탭 생략 (UI 구조화 집중)
                Text("1W | 1M | 3M", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Recharts LineChart 대체 (Compose Canvas 활용)
            Box(modifier = Modifier.height(150.dp).fillMaxWidth().background(Color(0xFFFFF6F6), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Text("Line Chart Area (Use Vico or MPAndroidChart)", color = PrimaryRed, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PerformanceInsightCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = PrimaryRed),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("PERFORMANCE INSIGHT", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                    Text("NVIDIA", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Top Performing Asset", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("+8.5%", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("TODAY", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RiskProfileCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = BgLight),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Text("RISK PROFILE", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(progress = 0.6f, color = PrimaryRed, strokeWidth = 4.dp, modifier = Modifier.fillMaxSize())
                    Text("Med", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Balanced Growth", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("Conservative approach recommended for this week.", fontSize = 10.sp, color = TextGray)
                }
            }
        }
    }
}

@Composable
fun AllocationCard(allocation: List<AllocationItem>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Portfolio Allocation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Details >", color = PrimaryRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pie Chart 커스텀 캔버스
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        val total = allocation.sumOf { it.value }
                        allocation.forEach { item ->
                            val sweepAngle = (item.value / total * 360).toFloat()
                            drawArc(
                                color = item.color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle - 2f, // Padding
                                useCenter = false,
                                style = Stroke(width = 40f, cap = StrokeCap.Butt),
                                size = Size(size.width, size.height)
                            )
                            startAngle += sweepAngle
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("STOCKS", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold)
                        Text("45%", fontSize = 18.sp, color = TextDark, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(32.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
                    allocation.forEach { item ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(item.color))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            }
                            Text("₩ ${formatter.format(item.value)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddWidgetCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)) // Dashed border 느낌 대체
            .clickable { /* TODO */ },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE5E5E5)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, contentDescription = null, tint = TextGray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("ADD WIDGET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
        }
    }
}

@Composable
fun BottomNavigationBar() {
    Surface(
        shadowElevation = 8.dp,
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BottomNavItem("Dashboard", Icons.Default.Home, true)
            BottomNavItem("Trade History", Icons.Default.List, false)
            BottomNavItem("Manage", Icons.Default.Build, false)
        }
    }
}

@Composable
fun BottomNavItem(label: String, icon: ImageVector, isSelected: Boolean) {
    val color = if (isSelected) PrimaryRed else TextGray
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { }) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
    }
}