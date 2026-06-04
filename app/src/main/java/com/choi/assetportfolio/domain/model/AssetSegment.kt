package com.choi.assetportfolio.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Look-through 분석을 위한 자산의 구성 성분 정보 모델.
 * asset_segments 테이블을 기반으로 합니다.
 */
@Serializable
data class AssetSegment(
    @SerialName("asset_id")
    val assetId: Long,
    @SerialName("segment_asset_class")
    val segmentAssetClass: String,
    val weight: Double // 0.0 ~ 1.0 (비중)
)
