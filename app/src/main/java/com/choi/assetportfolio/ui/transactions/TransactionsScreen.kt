/**
 * 거래 내역(임시 저장소 및 확정된 매매 이력)을 표시하는 화면입니다.
 * 상단은 클립보드나 외부에서 파싱된 임시 데이터(Staging), 하단은 확정 내역을 나타냅니다.
 */
package com.choi.assetportfolio.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.choi.assetportfolio.domain.model.Transaction
import java.time.format.DateTimeFormatter

@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel, modifier: Modifier = Modifier) {
    val stagingTransactions by viewModel.stagingTransactions.collectAsState()
    val confirmedUiState by viewModel.confirmedTransactionsUiState.collectAsState()

    val accounts by viewModel.accounts.collectAsState()
    val selectedAccountId by viewModel.selectedAccountId.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // 상단: 계좌 선택 및 기간 선택 드롭다운
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            var accountExpanded by remember { mutableStateOf(false) }
            val selectedAccountName = accounts.find { it.id == selectedAccountId }?.name ?: "전체 계좌"

            @OptIn(ExperimentalMaterial3Api::class)
            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedAccountName,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("전체 계좌") },
                        onClick = {
                            viewModel.selectAccount(null)
                            accountExpanded = false
                        }
                    )
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.name) },
                            onClick = {
                                viewModel.selectAccount(account.id)
                                accountExpanded = false
                            }
                        )
                    }
                }
            }

            var periodExpanded by remember { mutableStateOf(false) }

            @OptIn(ExperimentalMaterial3Api::class)
            ExposedDropdownMenuBox(
                expanded = periodExpanded,
                onExpandedChange = { periodExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedPeriod,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = periodExpanded,
                    onDismissRequest = { periodExpanded = false }
                ) {
                    listOf("전체", "1주일", "1개월", "3개월", "6개월", "1년").forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period) },
                            onClick = {
                                viewModel.selectPeriod(period)
                                periodExpanded = false
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 상단: Staging Area Component
        Text(
            text = "임시 저장소 (Staging Area)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (stagingTransactions.isEmpty()) {
            Text(
                text = "현재 임시로 추가된 거래가 없습니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(stagingTransactions) { stagingItem ->
                    StagingTransactionItem(item = stagingItem)
                }
                item {
                    Button(
                        onClick = { viewModel.approveStaging() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(text = "최종 승인")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // 하단: Confirmed History Component
        Text(
            text = "확정된 과거 매매 이력",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        when (val state = confirmedUiState) {
            is ConfirmedTransactionsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ConfirmedTransactionsUiState.Error -> {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = "오류 발생: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is ConfirmedTransactionsUiState.Success -> {
                if (state.transactions.isEmpty()) {
                    Text(
                        text = "확정된 거래 내역이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(2f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.transactions) { transaction ->
                            ConfirmedTransactionItem(transaction = transaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StagingTransactionItem(item: StagingTransaction) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val dateStr = item.transaction.transactionDate.format(formatter)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isDuplicateSuspected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isDuplicateSuspected) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "중복 의심",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = item.ticker,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$dateStr | ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TradeTypeBadge(tradeType = item.transaction.tradeType)
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.widthIn(min = 80.dp)
            ) {
                if (item.transaction.assets?.priceSource != "manual") {
                    Text(
                        text = "${item.transaction.quantity}주",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "금액: ${item.amount}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ConfirmedTransactionItem(transaction: Transaction) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val dateStr = transaction.transactionDate.format(formatter)

    val displayName = transaction.assets?.nameKr?.takeIf { it.isNotBlank() } ?: "Asset ID: ${transaction.assetId}"
    val currency = transaction.assets?.currency ?: "won"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = displayName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$dateStr | ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TradeTypeBadge(tradeType = transaction.tradeType)
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.widthIn(min = 80.dp)
            ) {
                if (transaction.assets?.priceSource != "manual") {
                    Text(
                        text = "${transaction.quantity}주",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                val displayPrice = if (transaction.assets?.priceSource == "manual") transaction.quantity else transaction.price
                Text(
                    text = formatPrice(displayPrice, currency),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun TradeTypeBadge(tradeType: String) {
    val (text, color) = when (tradeType.uppercase()) {
        "BUY" -> "매수" to Color.Red
        "SELL" -> "매도" to Color.Blue
        "REVALUATION" -> "정정" to Color(0xFF388E3C)
        "DEPOSIT" -> "입금" to Color.Black
        "WITHDRAW" -> "출금" to Color.Black
        else -> tradeType to Color.Gray
    }
    Text(
        text = text,
        color = color,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.bodySmall
    )
}

fun formatPrice(price: Double, currency: String): String {
    val formatWon = java.text.DecimalFormat("#,###원")
    val formatUsd = java.text.DecimalFormat("$#,###.##")
    return if (currency.equals("won", ignoreCase = true) || currency.equals("krw", ignoreCase = true)) {
        formatWon.format(price)
    } else if (currency.equals("usd", ignoreCase = true)) {
        formatUsd.format(price)
    } else {
        price.toString()
    }
}
