package com.choi.assetportfolio.domain.repository

import com.choi.assetportfolio.domain.model.Account
import com.choi.assetportfolio.domain.model.Asset
import com.choi.assetportfolio.domain.model.AssetSegment
import com.choi.assetportfolio.domain.model.DashboardAsset

interface AssetRepository {
    /**
     * 현재 로그인된 사용자의 모든 계좌를 조회합니다.
     */
    suspend fun fetchUserAccounts(): List<Account>

    /**
     * 글로벌 자산 목록 또는 사용자와 연관된 자산 목록을 페이지네이션으로 안전하게 가져옵니다.
     * 1,000행 초과 조회 금지 원칙(GUIDE_RAIL)을 준수합니다.
     */
    suspend fun fetchUserAssets(): List<Asset>

    /**
     * 대시보드에 필요한 자산 요약 목록(현재 평가액 포함)을 가져옵니다.
     */
    suspend fun fetchDashboardAssets(): List<DashboardAsset>

    /**
     * 특정 자산의 Look-through 구성을 위한 세그먼트 정보를 가져옵니다.
     */
    suspend fun getAssetSegments(assetId: Long): List<AssetSegment>
    suspend fun getAssetSegments(assetIds: List<Long>): List<AssetSegment>
    /**
     * 로컬 메모리에 캐시된 데이터를 무효화합니다. 
     * 거래 데이터가 추가되거나 동기화된 후 호출해야 합니다.
     */
    fun invalidateCache()
}
