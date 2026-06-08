package com.choi.assetportfolio.data.repository

import com.choi.assetportfolio.core.session.SessionManager
import com.choi.assetportfolio.core.util.AppLogger
import com.choi.assetportfolio.domain.model.*
import com.choi.assetportfolio.domain.repository.AssetRepository
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable

/**
 * 자산 관련 데이터를 Supabase에서 가져오는 저장소 구현체.
 * RLS 권한 에러(42501)에 대한 예외 처리가 포함되어 있습니다.
 */
class AssetRepositoryImpl(
    private val postgrest: Postgrest,
    private val syncRepository: SyncRepository
) : AssetRepository {

    private var dashboardCache: List<DashboardAsset>? = null

    override fun invalidateCache() {
        dashboardCache = null
        AppLogger.d("AssetRepository cache invalidated")
    }

    /**
     * Supabase 호출을 래핑하여 공통 예외(권한 부족 등)와 네트워크 단절을 처리합니다.
     * 네트워크 에러 발생 시 fallbackBlock을 통해 로컬 Room DB 캐시로 스위칭합니다.
     */
    private suspend fun <T> wrapSupabaseCall(fallbackBlock: (suspend () -> T)? = null, block: suspend () -> T): T {
        return try {
            block()
        } catch (e: RestException) {
            if (e.message?.contains("42501") == true) {
                AppLogger.e("Permission Denied (42501): RLS 정책 또는 GRANT 설정을 확인하세요.", e)
                throw IllegalStateException("권한 설정이 필요합니다. 관리자에게 문의하세요.")
            }
            if (e.message?.contains("timeout", ignoreCase = true) == true) {
                AppLogger.d("네트워크 연결 시간 초과 (RestException). 로컬 캐시 폴백 시도")
                fallbackBlock?.invoke() ?: throw e
            }
            throw e
        } catch (e: java.net.SocketTimeoutException) {
            AppLogger.d("네트워크 단절: SocketTimeoutException. 로컬 캐시 폴백 시도")
            fallbackBlock?.invoke() ?: throw e
        } catch (e: java.net.UnknownHostException) {
            AppLogger.d("네트워크 단절: UnknownHostException. 로컬 캐시 폴백 시도")
            fallbackBlock?.invoke() ?: throw e
        } catch (e: java.net.ConnectException) {
            AppLogger.d("네트워크 단절: ConnectException. 로컬 캐시 폴백 시도")
            fallbackBlock?.invoke() ?: throw e
        } catch (e: Exception) {
            // 다른 종류의 네트워크 예외일 가능성 확인
            val msg = e.message ?: ""
            if (msg.contains("Failed to connect", ignoreCase = true) || 
                msg.contains("timeout", ignoreCase = true) ||
                msg.contains("Unable to resolve host", ignoreCase = true) ||
                e.javaClass.simpleName == "HttpRequestException"
            ) {
                AppLogger.d("네트워크 단절: ${e.javaClass.simpleName}. 로컬 캐시 폴백 시도 (${e.message})")
                return fallbackBlock?.invoke() ?: throw e
            }
            throw e
        }
    }

    override suspend fun fetchUserAccounts(): List<Account> = wrapSupabaseCall(
        fallbackBlock = {
            AppLogger.d("fetchUserAccounts Fallback - 로컬 캐시(또는 빈 리스트) 반환")
            emptyList()
        }
    ) {
        val userId = SessionManager.requireUserId()
        if (userId.isBlank()) {
            AppLogger.d("fetchUserAccounts - userId is blank, returning empty list")
            return@wrapSupabaseCall emptyList()
        }
        
        val allAccounts = mutableListOf<Account>()
        var offset = 0
        val limit = 500
        
        while (true) {
            val from = offset
            val to = offset + limit - 1
            
            val batch = postgrest.from("accounts")
                .select {
                    filter { eq("user_id", userId) }
                    range(from.toLong()..to.toLong())
                }
                .decodeList<Account>()
                
            allAccounts.addAll(batch)
            
            if (batch.size < limit) {
                break
            }
            offset += limit
        }
        
        AppLogger.d("fetchUserAccounts 결과 반환", data = "${allAccounts.size} accounts found")
        allAccounts
    }

    override suspend fun fetchUserAssets(): List<Asset> = wrapSupabaseCall(
        fallbackBlock = {
            AppLogger.d("fetchUserAssets Fallback - 로컬 캐시 반환")
            emptyList()
        }
    ) {
        val userId = SessionManager.requireUserId()
        if (userId.isBlank()) {
            AppLogger.d("fetchUserAssets - userId is blank, returning empty list")
            return@wrapSupabaseCall emptyList()
        }
        
        val allAssets = mutableListOf<Asset>()
        var offset = 0
        val limit = 500
        
        while (true) {
            val from = offset
            val to = offset + limit - 1
            
            val batch = postgrest.from("assets")
                .select {
                    range(from.toLong()..to.toLong())
                }
                .decodeList<Asset>()
                
            allAssets.addAll(batch)
            
            if (batch.size < limit) {
                break
            }
            offset += limit
        }
        
        AppLogger.d("fetchUserAssets 결과 반환", data = "${allAssets.size} assets found")
        allAssets
    }

    override suspend fun fetchDashboardAssets(): List<DashboardAsset> {
        // 동기화 파이프라인: 캐시 건전성 확인
        if (!syncRepository.isLocalCacheDirty.value && !dashboardCache.isNullOrEmpty()) {
            AppLogger.d("AssetRepositoryImpl - 캐시가 건전하여 로컬 스트림을 반환합니다.")
            return dashboardCache!!
        }

        AppLogger.d("SyncEngine: Local Cache Dirty Detected - Initiating Remote Fetch")

        return wrapSupabaseCall(
            fallbackBlock = {
                AppLogger.d("fetchDashboardAssets Fallback - 로컬 캐시 반환")
                dashboardCache ?: emptyList()
            }
        ) {
            val accountIds = fetchUserAccounts().map { it.id }
        if (accountIds.isEmpty()) {
            AppLogger.d("fetchDashboardAssets - accountIds is empty, returning empty list")
            return@wrapSupabaseCall emptyList()
        }

        // 1. 모든 자산 메타데이터 조회
        val allAssets = fetchUserAssets()
        val assetMap = allAssets.associateBy { it.id }

        // 2. 계좌들에 속한 모든 거래내역(transactions) 조회
        val allTransactions = mutableListOf<Transaction>()
        var offset = 0
        val limit = 500
        while (true) {
            val from = offset
            val to = offset + limit - 1
            val batch = postgrest.from("transactions")
                .select {
                    filter { isIn("account_id", accountIds) }
                    range(from.toLong()..to.toLong())
                }
                .decodeList<Transaction>()
            allTransactions.addAll(batch)
            if (batch.size < limit) break
            offset += limit
        }

        AppLogger.d("fetchDashboardAssets - fetched transactions", data = "${allTransactions.size} transactions found")

        // 3. 거래내역을 (accountId, assetId) 단위로 그룹핑하여 WAC(가중평균) 알고리즘으로 집계
        val groupedTransactions = allTransactions.groupBy { Pair(it.accountId, it.assetId) }
        
        val dashboardAssets = groupedTransactions.mapNotNull { (key, transactions) ->
            val (accountId, assetId) = key
            val asset = assetMap[assetId] ?: return@mapNotNull null

            var currentQty = 0.0
            var averagePrice = 0.0
            var realizedPnl = 0.0

            // transaction_date 기준 오름차순 정렬하여 순차적으로 누적 계산 (Fold)
            val sortedTx = transactions.sortedBy { it.transactionDate }

            for (tx in sortedTx) {
                val type = tx.tradeType.uppercase()
                if (type == "BUY" || type == "DEPOSIT") {
                    val newTotalQty = currentQty + tx.quantity
                    if (newTotalQty > 0) {
                        averagePrice = ((currentQty * averagePrice) + (tx.quantity * tx.price)) / newTotalQty
                    }
                    currentQty = newTotalQty
                } else if (type == "SELL" || type == "WITHDRAWAL") {
                    currentQty -= tx.quantity
                    realizedPnl += tx.quantity * (tx.price - averagePrice)
                    // averagePrice는 유지됨
                }
            }

            // 보유 수량이 0 이하인 경우 대시보드에서 제외
            if (currentQty <= 0.000001) {
                return@mapNotNull null
            }

            val currentValuationPrice = asset.currentPrice
            val totalValuationAmount = currentQty * currentValuationPrice
            val totalPurchaseAmount = currentQty * averagePrice
            val unrealizedPnl = totalValuationAmount - totalPurchaseAmount
            val unrealizedReturnRate = if (totalPurchaseAmount > 0) (unrealizedPnl / totalPurchaseAmount) * 100 else 0.0

            DashboardAsset(
                assetId = assetId,
                accountId = accountId,
                ticker = asset.ticker,
                nameKr = asset.nameKr,
                totalQuantity = currentQty,
                averagePurchasePrice = averagePrice,
                currentValuationPrice = currentValuationPrice,
                totalValuationAmount = totalValuationAmount,
                unrealizedPnl = unrealizedPnl,
                unrealizedReturnRate = unrealizedReturnRate,
                lookthroughAvailable = asset.lookthroughAvailable,
                currency = asset.currency
            )
        }

        AppLogger.d("fetchDashboardAssets 결과 집계 완료", data = "${dashboardAssets.size} assets grouped and calculated")
        dashboardCache = dashboardAssets
        
        // 데이터 패치 성공 시 캐시 오염 상태 해제
        syncRepository.setCacheDirty(false)
        
        dashboardAssets
    }
}

    override suspend fun getAssetSegments(assetId: Long): List<AssetSegment> = wrapSupabaseCall(
        fallbackBlock = {
            AppLogger.d("getAssetSegments Fallback - 로컬 캐시 반환")
            emptyList()
        }
    ) {
        val userId = SessionManager.requireUserId()
        if (userId.isBlank()) {
            AppLogger.d("getAssetSegments - userId is blank, returning empty list")
            return@wrapSupabaseCall emptyList()
        }
        
        val result = postgrest.from("asset_segments")
            .select {
                filter {
                    eq("asset_id", assetId)
                }
            }
            .decodeList<AssetSegment>()

        AppLogger.d("getAssetSegments 결과 반환", data = "assetId=$assetId, segmentsCount=${result.size}")
        result
    }
}


