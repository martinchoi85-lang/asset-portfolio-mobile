/**
 * 수동 자산(예적금, TDF 등)의 원금 변경 및 이력 상태를 관리하는 ViewModel.
 * 현재 잔액(Current)과 이벤트(Events)를 로드하고, 새로운 이벤트를 기록합니다.
 */
package com.choi.assetportfolio.ui.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.choi.assetportfolio.core.session.SessionManager
import com.choi.assetportfolio.core.util.AppLogger
import com.choi.assetportfolio.data.repository.ManualAssetCostBasisCurrent
import com.choi.assetportfolio.data.repository.ManualAssetCostBasisEvent
import com.choi.assetportfolio.data.repository.ManualAssetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ManagementHubViewModel(
    private val manualAssetRepository: ManualAssetRepository
) : ViewModel() {

    private val _currentBalances = MutableStateFlow<List<ManualAssetCostBasisCurrent>>(emptyList())
    val currentBalances: StateFlow<List<ManualAssetCostBasisCurrent>> = _currentBalances.asStateFlow()

    private val _events = MutableStateFlow<List<ManualAssetCostBasisEvent>>(emptyList())
    val events: StateFlow<List<ManualAssetCostBasisEvent>> = _events.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // 로그인 세션 아이디가 없으면 쿼리를 실행하지 않음 (Guide Rail 보안 원칙)
                SessionManager.requireUserId()
                
                val current = manualAssetRepository.getCurrentCostBasis()
                _currentBalances.value = current

                val eventList = manualAssetRepository.getCostBasisEvents()
                _events.value = eventList
            } catch (e: Exception) {
                AppLogger.e("ManagementHub 로드 실패", e)
            }
        }
    }

    fun saveManualAssetUpdate(accountId: String, assetId: Long, deltaAmount: Double, currency: String, reason: String) {
        AppLogger.d("Asset Update Initiated", data = "Asset ID: $assetId, Delta: $deltaAmount, Reason: $reason - Update Initiated")
        
        viewModelScope.launch {
            try {
                SessionManager.requireUserId()
                
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                
                // 기존 잔액 탐색
                val existingCurrent = _currentBalances.value.find { it.account_id == accountId && it.asset_id == assetId }
                val previousAmount = existingCurrent?.cost_basis_amount ?: 0.0
                val newTotalAmount = previousAmount + deltaAmount

                val newEvent = ManualAssetCostBasisEvent(
                    account_id = accountId,
                    asset_id = assetId,
                    event_date = today,
                    delta_amount = deltaAmount,
                    currency = currency,
                    reason = reason
                )

                val updatedCurrent = ManualAssetCostBasisCurrent(
                    account_id = accountId,
                    asset_id = assetId,
                    currency = currency,
                    cost_basis_amount = newTotalAmount,
                    as_of_date = today
                )

                manualAssetRepository.updateManualAsset(newEvent, updatedCurrent)
                // 업서트 완료 후 최신화
                loadData()
            } catch (e: Exception) {
                AppLogger.e("수동 자산 업데이트 실패", e)
            }
        }
    }
}
