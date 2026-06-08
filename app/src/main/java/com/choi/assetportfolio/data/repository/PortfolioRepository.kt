package com.choi.assetportfolio.data.repository

import com.choi.assetportfolio.core.session.SessionManager
import com.choi.assetportfolio.domain.model.Transaction
import com.choi.assetportfolio.domain.model.DailySnapshot
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.Columns
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
            if (e.javaClass.simpleName == "HttpRequestException" || e.message?.contains("Unable to resolve host", true) == true) {
                AppLogger.d("네트워크 단절. 계좌 목록 조회 로컬 폴백")
            } else {
                AppLogger.e("계좌 목록 조회 실패", e)
            }
            emptyList()
        }
        
        if (userAccounts.isEmpty()) {
            AppLogger.d("getTransactions - no accounts found for user")
            return emptyList()
        }
        
        val accountIds = userAccounts.map { it.id }

        // 2. 해당 계좌들에 속한 거래 내역 조회
        val result = postgrest.from("transactions")
            .select(columns = Columns.raw("*, assets(name_kr, currency, asset_type, market, underlying_asset_class, economic_exposure_region, vehicle_type, lookthrough_available, price_source)")) {
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

    suspend fun getUserAccounts(): List<com.choi.assetportfolio.domain.model.Account> {
        val userId = SessionManager.requireUserId()
        if (userId.isBlank()) return emptyList()
        return try {
            postgrest.from("accounts")
                .select { filter { eq("user_id", userId) } }
                .decodeList<com.choi.assetportfolio.domain.model.Account>()
        } catch (e: Exception) {
            if (e.javaClass.simpleName == "HttpRequestException" || e.message?.contains("Unable to resolve host", true) == true) {
                AppLogger.d("네트워크 단절. getUserAccounts 로컬 폴백")
            } else {
                AppLogger.e("getUserAccounts 오류", e)
            }
            emptyList()
        }
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
            if (e.javaClass.simpleName == "HttpRequestException" || e.message?.contains("Unable to resolve host", true) == true) {
                AppLogger.d("네트워크 단절. getTargetWeights 로컬 폴백")
            } else {
                AppLogger.e("getTargetWeights 오류", e)
            }
            emptyList()
        }
    }

    suspend fun getDailySnapshots(startDate: LocalDate, endDate: LocalDate): List<DailySnapshot> {
        val userId = SessionManager.requireUserId()
        if (userId.isBlank()) {
            AppLogger.e("getDailySnapshots - userId is blank")
            return emptyList()
        }

        // 먼저 계좌 ID 목록을 가져와야 격리 보장이 됨 (getTransactions 방식 참조)
        val userAccounts = try {
            postgrest.from("accounts")
                .select { filter { eq("user_id", userId) } }
                .decodeList<com.choi.assetportfolio.domain.model.Account>()
        } catch (e: Exception) {
            if (e.javaClass.simpleName == "HttpRequestException" || e.message?.contains("Unable to resolve host", true) == true) {
                AppLogger.d("네트워크 단절. getDailySnapshots 계좌 조회 로컬 폴백")
            } else {
                AppLogger.e("계좌 목록 조회 실패", e)
            }
            emptyList()
        }
        
        if (userAccounts.isEmpty()) return emptyList()
        val accountIds = userAccounts.map { it.id }

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        return try {
            postgrest.from("daily_snapshots")
                .select {
                    filter {
                        isIn("account_id", accountIds)
                        gte("date", startDate.format(formatter))
                        lte("date", endDate.format(formatter))
                    }
                    order("date", Order.ASCENDING)
                }
                .decodeList<DailySnapshot>()
        } catch (e: Exception) {
            if (e.javaClass.simpleName == "HttpRequestException" || e.message?.contains("Unable to resolve host", true) == true) {
                AppLogger.d("네트워크 단절. getDailySnapshots 로컬 폴백")
            } else {
                AppLogger.e("getDailySnapshots 오류", e)
            }
            emptyList()
        }
    }
}
