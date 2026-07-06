/**
 * 거래 내역 및 임시 저장소(Staging Area) 상태를 관리하는 ViewModel.
 * 확정된 매매 이력과 중복 의심 데이터를 판별하여 UI에 제공합니다.
 */
package com.choi.assetportfolio.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.choi.assetportfolio.core.util.AppLogger
import com.choi.assetportfolio.data.repository.PortfolioRepository
import com.choi.assetportfolio.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ConfirmedTransactionsUiState {
    object Loading : ConfirmedTransactionsUiState
    data class Success(val transactions: List<Transaction>) : ConfirmedTransactionsUiState
    data class Error(val message: String) : ConfirmedTransactionsUiState
}

data class StagingTransaction(
    val transaction: Transaction,
    val ticker: String,
    val amount: Double,
    val isDuplicateSuspected: Boolean
)

class TransactionsViewModel(
    private val portfolioRepository: PortfolioRepository
) : ViewModel() {

    private val _confirmedTransactionsUiState = MutableStateFlow<ConfirmedTransactionsUiState>(ConfirmedTransactionsUiState.Loading)
    val confirmedTransactionsUiState: StateFlow<ConfirmedTransactionsUiState> = _confirmedTransactionsUiState.asStateFlow()

    private val _stagingTransactions = MutableStateFlow<List<StagingTransaction>>(emptyList())
    val stagingTransactions: StateFlow<List<StagingTransaction>> = _stagingTransactions.asStateFlow()

    private var allTransactions = emptyList<Transaction>()

    private val _accounts = MutableStateFlow<List<com.choi.assetportfolio.domain.model.Account>>(emptyList())
    val accounts: StateFlow<List<com.choi.assetportfolio.domain.model.Account>> = _accounts.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<String?>(null)
    val selectedAccountId: StateFlow<String?> = _selectedAccountId.asStateFlow()

    private val _selectedPeriod = MutableStateFlow<String>("전체")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            _confirmedTransactionsUiState.value = ConfirmedTransactionsUiState.Loading
            try {
                _accounts.value = portfolioRepository.getUserAccounts()
                allTransactions = portfolioRepository.getTransactions()
                applyFilters()
                AppLogger.d("TransactionsViewModel - 데이터 로드 성공", data = "Tx Count: ${allTransactions.size}")
            } catch (e: Exception) {
                AppLogger.e("TransactionsViewModel - 데이터 로드 실패", e)
                _confirmedTransactionsUiState.value = ConfirmedTransactionsUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun selectAccount(accountId: String?) {
        _selectedAccountId.value = accountId
        applyFilters()
    }

    fun selectPeriod(period: String) {
        _selectedPeriod.value = period
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = allTransactions
        val accId = _selectedAccountId.value
        if (accId != null) {
            filtered = filtered.filter { it.accountId == accId }
        }

        val periodDays = when (_selectedPeriod.value) {
            "1주일" -> 7L
            "1개월" -> 30L
            "3개월" -> 90L
            "6개월" -> 180L
            "1년" -> 365L
            else -> 0L
        }

        if (periodDays > 0) {
            val cutoffDate = java.time.ZonedDateTime.now().minusDays(periodDays)
            filtered = filtered.filter { !it.transactionDate.isBefore(cutoffDate) }
        }

        _confirmedTransactionsUiState.value = ConfirmedTransactionsUiState.Success(filtered)
    }

    fun addTransactionToStaging(transaction: Transaction, ticker: String, amount: Double) {
        val newHashKey = transaction.generateHashKey(ticker, amount)
        
        val currentState = _confirmedTransactionsUiState.value
        val isDuplicateSuspected = if (currentState is ConfirmedTransactionsUiState.Success) {
            currentState.transactions.any { confirmed ->
                // 실제 환경에서는 DB에 저장된 hash_key를 대조하지만, 여기서는 핵심 필드로 중복을 검증합니다.
                confirmed.transactionDate.toEpochSecond() == transaction.transactionDate.toEpochSecond() &&
                confirmed.assetId == transaction.assetId &&
                confirmed.tradeType == transaction.tradeType &&
                confirmed.quantity == transaction.quantity
            }
        } else {
            false
        }

        AppLogger.d("Staging Load", data = "Ticker: $ticker, Amount: $amount, isDuplicateSuspected: $isDuplicateSuspected, Hash: $newHashKey")

        val newStaging = StagingTransaction(transaction, ticker, amount, isDuplicateSuspected)
        _stagingTransactions.value = _stagingTransactions.value + newStaging
    }

    fun approveStaging() {
        val stagingCount = _stagingTransactions.value.size
        AppLogger.d("Staging Approve", data = "최종 승인된 거래 건수: $stagingCount")
        // 추후 실제 DB Insert 로직이 여기에 위치합니다.
        _stagingTransactions.value = emptyList()
    }
}
