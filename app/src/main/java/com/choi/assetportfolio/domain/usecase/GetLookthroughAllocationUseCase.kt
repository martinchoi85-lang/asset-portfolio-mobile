package com.choi.assetportfolio.domain.usecase

import com.choi.assetportfolio.domain.model.Asset
import com.choi.assetportfolio.domain.model.AssetSegment
import com.choi.assetportfolio.domain.model.DashboardAsset

import com.choi.assetportfolio.core.util.AppLogger

data class AllocationResult(
    val assetClass: String,
    val totalAmount: Double,
    val weight: Double
)

class GetLookthroughAllocationUseCase {
    /**
     * lookthrough_available == true 인 자산을 분해(Explode)하고,
     * underlying_asset_class 기준으로 최종 비중(Weight)을 Grouping 합니다.
     */
    operator fun invoke(
        dashboardAssets: List<DashboardAsset>,
        assets: List<Asset>,
        segments: List<AssetSegment>,
        currencyConverter: CurrencyConverter = DefaultCurrencyConverter()
    ): List<AllocationResult> {
        AppLogger.d("invoke - 룩스루 비중 계산 시작", data = "dashboardAssets=${dashboardAssets.size}, assets=${assets.size}, segments=${segments.size}")
        val totalPortfolioValue = dashboardAssets.sumOf { 
            val asset = assets.find { a -> a.id == it.assetId }
            val currency = asset?.currency ?: "KRW" // assuming currency property exists
            currencyConverter.convertToKrw(it.totalValuationAmount, currency)
        }
        
        if (totalPortfolioValue <= 0.0) {
            AppLogger.d("invoke - 포트폴리오 총 가치가 0이므로 빈 목록 반환")
            return emptyList()
        }

        val assetMap = assets.associateBy { it.id }
        val segmentMap = segments.groupBy { it.assetId }
        
        val classAmountMap = mutableMapOf<String, Double>()

        for (dAsset in dashboardAssets) {
            val asset = assetMap[dAsset.assetId] ?: continue
            // 외화 변환 방어 코드 적용
            val currency = asset.currency ?: "KRW"
            val amount = try {
                currencyConverter.convertToKrw(dAsset.totalValuationAmount, currency)
            } catch (e: Exception) {
                AppLogger.e("GetLookthroughAllocationUseCase - 통화 변환 예외 발생", e)
                dAsset.totalValuationAmount
            }

            if (dAsset.lookthroughAvailable) {
                val assetSegments = segmentMap[dAsset.assetId]
                if (!assetSegments.isNullOrEmpty()) {
                    for (segment in assetSegments) {
                        val segmentAmount = amount * segment.weight
                        classAmountMap[segment.segmentAssetClass] = 
                            (classAmountMap[segment.segmentAssetClass] ?: 0.0) + segmentAmount
                    }
                } else {
                    classAmountMap[asset.underlyingAssetClass] = 
                        (classAmountMap[asset.underlyingAssetClass] ?: 0.0) + amount
                }
            } else {
                classAmountMap[asset.underlyingAssetClass] = 
                    (classAmountMap[asset.underlyingAssetClass] ?: 0.0) + amount
            }
        }

        val result = classAmountMap.map { (assetClass, amount) ->
            AllocationResult(
                assetClass = assetClass,
                totalAmount = amount,
                weight = amount / totalPortfolioValue
            )
        }.sortedByDescending { it.weight }

        AppLogger.d("invoke - 룩스루 비중 계산 완료", data = "allocationsCount=${result.size}, allocations=$result")
        return result
    }
}
