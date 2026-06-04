package com.choi.assetportfolio.domain.usecase

import com.choi.assetportfolio.domain.model.AssetSegment
import com.choi.assetportfolio.domain.repository.AssetRepository

/**
 * Look-through 원칙에 따라 복합 자산(ETF, 펀드 등)을 기초 자산군으로 분해하여 계산하는 UseCase.
 */
import com.choi.assetportfolio.core.util.AppLogger

class CalculateLookthroughUseCase(
    private val repository: AssetRepository
) {
    /**
     * 특정 자산의 총 평가액을 기초 자산군별 비중(weight)에 따라 분해합니다.
     * @param assetId 분석할 자산의 ID
     * @param totalValuation 해당 자산의 총 평가액
     * @return 기초 자산군 명칭(key)과 해당 비중이 적용된 평가액(value)의 맵
     */
    suspend fun execute(assetId: Long, totalValuation: Double): Map<String, Double> {
        AppLogger.d("execute - Look-through 계산 시작", data = "assetId=$assetId, totalValuation=$totalValuation")
        val segments = repository.getAssetSegments(assetId)
        
        // 가중치(weight)를 적용하여 분해 후, 동일한 기초 자산군끼리 그룹화하여 합산
        val result = segments.groupBy { it.segmentAssetClass }
            .mapValues { (_, segmentList) ->
                segmentList.sumOf { it.weight * totalValuation }
            }
            
        AppLogger.d("execute - Look-through 계산 결과", data = "result=$result")
        return result
    }
}
