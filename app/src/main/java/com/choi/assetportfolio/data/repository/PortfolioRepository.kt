package com.choi.assetportfolio.data.repository

import com.choi.assetportfolio.core.session.SessionManager
import com.choi.assetportfolio.domain.model.Transaction
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order

import com.choi.assetportfolio.core.util.AppLogger

class PortfolioRepository(private val postgrest: Postgrest) {

    /**
     * @param page 0부터 시작하는 페이지 번호
     * @param pageSize 페이지당 항목 수 (최대 1000 미만 권장)
     * AGENTS.md의 1,000행 초과 조회 금지 규칙 준수를 위해 Pagination 패턴 적용
     */
    suspend fun getTransactions(page: Int = 0, pageSize: Int = 500): List<Transaction> {
        val from = page * pageSize
        val to = from + pageSize - 1
        
        // SessionManager를 통해 현재 사용자의 ID를 가져와 데이터 격리 보장
        val userId = SessionManager.requireUserId()
        if (userId.isBlank()) {
            AppLogger.d("getTransactions - userId is blank, returning empty list")
            return emptyList() // Prevent 22P02 Error
        }
        
        // 1. 사용자의 계좌 목록을 먼저 조회
        val userAccounts = try {
            postgrest.from("accounts")
                .select { filter { eq("user_id", userId) } }
                .decodeList<com.choi.assetportfolio.domain.model.Account>()
        } catch (e: Exception) {
            AppLogger.e("계좌 목록 조회 실패", e)
            emptyList()
        }
        
        if (userAccounts.isEmpty()) {
            AppLogger.d("getTransactions - no accounts found for user")
            return emptyList()
        }
        
        val accountIds = userAccounts.map { it.id }

        // 2. 해당 계좌들에 속한 거래 내역 조회
        val result = postgrest.from("transactions")
            .select {
                filter {
                    isIn("account_id", accountIds)
                }
                order("transaction_date", Order.DESCENDING)
                range(from.toLong()..to.toLong())
            }
            .decodeList<Transaction>()

        AppLogger.d("getTransactions 결과 반환", data = "page=$page, transactionsCount=${result.size}")
        return result
    }

    suspend fun getTargetWeights(groupingCriteria: String = "underlying_asset_class"): List<com.choi.assetportfolio.domain.model.PortfolioTargetWeight> {
        val userId = SessionManager.requireUserId()
        if (userId.isBlank()) {
            AppLogger.e("getTargetWeights - userId is blank")
            return emptyList()
        }

        return try {
            postgrest.from("portfolio_target_weights")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("grouping_criteria", groupingCriteria)
                    }
                }
                .decodeList<com.choi.assetportfolio.domain.model.PortfolioTargetWeight>()
        } catch (e: Exception) {
            AppLogger.e("getTargetWeights 오류", e)
            emptyList()
        }
    }
}
