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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Additional/Unified Themes ---
val SecondaryBlue = Color(0xFF005FAF)
val MutedBlue = Color(0xFF5E737D)
val PositiveGreen = Color(0xFF2E7D32)
val LightRedBg = Color(0xFFFFDAD6)

// --- Domain Models for Detail Screen ---
data class DetailHolding(
    val symbol: String,
    val name: String,
    val category: String,
    val value: Double,
    val changePercent: Double,
    val isPositive: Boolean,
    val quantity: String,
    val isManual: Boolean = false,
    val maturityDate: String? = null,
    val rateText: String? = null,
    val iconType: String = "" // "savings", "bank", "stock"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen() {
    var selectedTab by remember { mutableStateOf("Mirae Asset") }
    val tabs = listOf("Total Assets", "Mirae Asset", "Samsung Securities", "Crypto Wallet", "Real Estate")

    val activeHoldings = listOf(
        DetailHolding(
            symbol = "NVDA",
            name = "NVIDIA Corp",
            category = "Technology • NASDAQ",
            value = 141.24,
            changePercent = 4.12,
            isPositive = true,
            quantity = "480.00",
            iconType = "stock"
        ),
        DetailHolding(
            symbol = "AAPL",
            name = "Apple Inc.",
            category = "Electronics • NASDAQ",
            value = 230.15,
            changePercent = -1.04,
            isPositive = false,
            quantity = "215.00",
            iconType = "stock"
        ),
        DetailHolding(
            symbol = "Savings",
            name = "Emergency Reserve",
            category = "Savings Account",
            value = 85000.00,
            changePercent = 4.20, // Used as APY
            isPositive = true,
            quantity = "Manual",
            isManual = true,
            maturityDate = "2026-12-01",
            rateText = "4.2% APY",
            iconType = "savings"
        ),
        DetailHolding(
            symbol = "Bank",
            name = "Retirement TDF 2055",
            category = "Investment Plan",
            value = 124500.00,
            changePercent = 8.40,
            isPositive = true,
            quantity = "Manual",
            isManual = true,
            maturityDate = "2055-06-30",
            rateText = "+8.4%",
            iconType = "bank"
        ),
        DetailHolding(
            symbol = "TSLA",
            name = "Tesla, Inc.",
            category = "Automotive • NASDAQ",
            value = 262.50,
            changePercent = 0.88,
            isPositive = true,
            quantity = "110.00",
            iconType = "stock"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Financial Advisor Profile picture placeholder
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        ) {
                            Text(
                                "FA", 
                                modifier = Modifier.align(Alignment.Center),
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceColor,
                                fontSize = 12.sp
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
                    IconButton(onClick = { /* Handle settings click */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = PrimaryRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardBackground
                )
            )
        },
        bottomBar = {
            BottomNavigationBar()
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Open quick trade or addition menu */ },
                containerColor = PrimaryRed,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick Action"
                )
            }
        },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // --- Account Tabs (Scrollable) ---
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
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(2.dp)
                                    .background(PrimaryRed)
                            )
                        }
                    }
                }
            }

            // --- Real-time Account Total Valuation ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "TOTAL VALUATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$492,840.52",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurfaceColor,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(LightPrimaryRed)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+$14,204.00 (2.9%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed
                        )
                    }
                    Text(
                        text = "Past 24h",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // --- Two Columns/Grid Widgets ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Profit Performance Card
                ProfitPerformanceCardView()

                // 2. Asset Allocation Bar card
                AssetAllocationBarCardView()

                // 3. Active Holdings Title Section
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Filter List",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Sort List",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // 4. Compact Active Holdings list (alternating background colors)
                activeHoldings.forEachIndexed { index, holding ->
                    val isAlternating = index % 2 != 0
                    ActiveHoldingItemView(holding = holding, isAlternating = isAlternating)
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
                    imageVector = Icons.Default.Menu, // Placeholder for drag layout indicator
                    contentDescription = "Drag widget",
                    tint = Color.LightGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Spec-driven fake sparkline using stacked custom progress bars representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // 0 level horizontal red line
                    val yBaseline = height * 0.6f
                    drawLine(
                        color = PrimaryRed,
                        start = Offset(0f, yBaseline),
                        end = Offset(width, yBaseline),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Draw pink translucent bar groups
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
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("OCT 01", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text("OCT 15", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text("OCT 30", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AssetAllocationBarCardView() {
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
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Drag widget",
                    tint = Color.LightGray,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sector Linear Bars Stack
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SectorMetricView(label = "Technology", valueText = "42%", progressRatio = 0.42f, trackColor = PrimaryRed)
                SectorMetricView(label = "Cash & Savings", valueText = "28%", progressRatio = 0.28f, trackColor = PrimaryRed)
                SectorMetricView(label = "Index Funds", valueText = "30%", progressRatio = 0.30f, trackColor = CustomBlue)
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
fun ActiveHoldingItemView(holding: DetailHolding, isAlternating: Boolean) {
    val backgroundColor = if (isAlternating) LightGray.copy(alpha = 0.4f) else CardBackground

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
            // Left Group - Logo + details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customized Circle/Square Visual Icon representing category
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (holding.iconType == "stock") LightGray else LightPrimaryRed),
                    contentAlignment = Alignment.Center
                ) {
                    if (holding.iconType == "stock") {
                        Text(
                            text = holding.symbol,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceColor
                        )
                    } else {
                        // Mimicking material vectors with simpler labels or dots
                        val iconCharacter = if (holding.iconType == "savings") "🐖" else "🏦"
                        Text(
                            text = iconCharacter,
                            fontSize = 16.sp
                        )
                    }
                }

                Column {
                    Text(
                        text = holding.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                    if (holding.maturityDate != null) {
                        Text(
                            text = "Maturity: ${holding.maturityDate}",
                            fontSize = 11.sp,
                            color = PrimaryRed,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = holding.category,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Right Group - Info & P/L Rates split
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity (Hidden on extremely small layouts, visible conceptually here)
                Column(horizontalAlignment = Alignment.End) {
                    val primaryHeader = if (holding.isManual) {
                        holding.rateText ?: ""
                    } else {
                        val sign = if (holding.isPositive) "+" else ""
                        "$sign${holding.changePercent}%"
                    }

                    val rateColor = if (holding.isPositive) {
                        PrimaryRed
                    } else {
                        CustomBlue
                    }

                    Text(
                        text = if (holding.isManual) {
                            "$${formatAmountDouble(holding.value)}"
                        } else {
                            "$${holding.value}"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceColor
                    )
                    Text(
                        text = primaryHeader,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (holding.isManual) Color.Gray else rateColor
                    )
                }
            }
        }
    }
}

// Utility double amount mapper
fun formatAmountDouble(value: Double): String {
    return java.text.DecimalFormat("#,###.00").format(value)
}

@Preview(showBackground = true)
@Composable
fun AccountDetailPreview() {
    MaterialTheme {
        AccountDetailScreen()
    }
}