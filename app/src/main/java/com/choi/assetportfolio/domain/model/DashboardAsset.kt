package com.choi.assetportfolio.domain.model

import kotlinx.serialization.Serializable

/**
 * 대시보드에 표시될 자산 요약 정보 모델.
 * DB의 asset_summary_live 뷰를 기반으로 합니다.
 */
@Serializable
data class DashboardAsset(
    val assetId: Long,
    val accountId: String,
    val ticker: String,
    val nameKr: String,
    val totalQuantity: Double,
    val averagePurchasePrice: Double,
    val currentValuationPrice: Double,
    val totalValuationAmount: Double,
    val unrealizedPnl: Double,
    val unrealizedReturnRate: Double,
    val lookthroughAvailable: Boolean,
    val currency: String = "won"
)
