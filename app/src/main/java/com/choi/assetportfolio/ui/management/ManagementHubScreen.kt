/**
 * 관리 센터(Management Hub) - 수동 자산 원금 수정 UI.
 * 수량 개념이 없는 예적금, TDF 등을 이벤트 기반(입금, 만기, 배당 재투자 등)으로 관리합니다.
 */
package com.choi.assetportfolio.ui.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.choi.assetportfolio.data.repository.ManualAssetCostBasisEvent
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementHubScreen(viewModel: ManagementHubViewModel, modifier: Modifier = Modifier) {
    val currentBalances by viewModel.currentBalances.collectAsState()
    val events by viewModel.events.collectAsState()

    var deltaAmount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    // 시연용 임시 계좌/자산 ID (실제로는 드롭다운에서 선택)
    val tempAccountId = "00000000-0000-0000-0000-000000000000"
    val tempAssetId = 999L
    val tempCurrency = "KRW"

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "수동 자산 원금 수정",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = deltaAmount,
                    onValueChange = { deltaAmount = it },
                    label = { Text("변동 금액 (Delta Amount)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // 사유 선택 드롭다운 모사 (단순 텍스트 필드로 구현)
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("변경 사유 (예: 추가납입, 배당재투자)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        val amount = deltaAmount.toDoubleOrNull() ?: 0.0
                        if (amount != 0.0 && reason.isNotBlank()) {
                            viewModel.saveManualAssetUpdate(tempAccountId, tempAssetId, amount, tempCurrency, reason)
                            deltaAmount = ""
                            reason = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("저장")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "원금 변동 이벤트 히스토리",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (events.isEmpty()) {
            Text(
                text = "기록된 변경 이력이 없습니다.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events.sortedByDescending { it.event_date }) { event ->
                    EventHistoryItem(event)
                }
            }
        }
    }
}

@Composable
fun EventHistoryItem(event: ManualAssetCostBasisEvent) {
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
                    text = event.reason,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = event.event_date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${if(event.delta_amount > 0) "+" else ""}${event.delta_amount} ${event.currency}",
                fontWeight = FontWeight.SemiBold,
                color = if (event.delta_amount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}
