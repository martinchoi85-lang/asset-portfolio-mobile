package com.choi.assetportfolio.data.repository

import com.choi.assetportfolio.core.session.SessionManager
import com.choi.assetportfolio.core.util.AppLogger
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.serialization.Serializable

@Serializable
data class ManualAssetCostBasisCurrent(
    val account_id: String,
    val asset_id: Long,
    val currency: String,
    val cost_basis_amount: Double,
    val as_of_date: String
)

@Serializable
data class ManualAssetCostBasisEvent(
    val account_id: String,
    val asset_id: Long,
    val event_date: String,
    val delta_amount: Double,
    val currency: String,
    val reason: String
)

class ManualAssetRepository(private val postgrest: Postgrest) {

    suspend fun getCurrentCostBasis(): List<ManualAssetCostBasisCurrent> {
        val userId = SessionManager.requireUserId()
        if (userId.isBlank()) {
            AppLogger.e("ManualAssetRepository - userId is blank")
            return emptyList()
        }
        
        // RLS 정책이 적용되어 사용자 소유의 데이터만 반환됨을 가정합니다.
        return try {
            postgrest.from("manual_asset_cost_basis_current")
                .select()
                .decodeList<ManualAssetCostBasisCurrent>()
        } catch (e: Exception) {
            AppLogger.e("getCurrentCostBasis Error", e)
            emptyList()
        }
    }

    suspend fun getCostBasisEvents(): List<ManualAssetCostBasisEvent> {
        val userId = SessionManager.requireUserId()
        if (userId.isBlank()) return emptyList()

        return try {
            postgrest.from("manual_asset_cost_basis_events")
                .select()
                .decodeList<ManualAssetCostBasisEvent>()
        } catch (e: Exception) {
            AppLogger.e("getCostBasisEvents Error", e)
            emptyList()
        }
    }

    suspend fun updateManualAsset(
        event: ManualAssetCostBasisEvent,
        current: ManualAssetCostBasisCurrent
    ) {
        val userId = SessionManager.requireUserId()
        if (userId.isBlank()) return

        try {
            // 이벤트 인서트
            postgrest.from("manual_asset_cost_basis_events").insert(event)
            // 잔액 업서트
            postgrest.from("manual_asset_cost_basis_current").upsert(current)
            AppLogger.d("updateManualAsset - DB Transaction 완료")
        } catch (e: Exception) {
            AppLogger.e("updateManualAsset Error", e)
        }
    }
}
