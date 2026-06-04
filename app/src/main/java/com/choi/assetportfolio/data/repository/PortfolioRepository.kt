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
        
        val result = postgrest.from("transactions")
            .select {
                filter {
                    eq("user_id", userId)
                }
                order("transaction_date", Order.DESCENDING)
                range(from.toLong()..to.toLong())
            }
            .decodeList<Transaction>()

        AppLogger.d("getTransactions 결과 반환", data = "page=$page, transactionsCount=${result.size}")
        return result
    }
}
