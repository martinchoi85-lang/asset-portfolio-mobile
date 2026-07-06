package com.choi.assetportfolio.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PortfolioTargetWeight(
    val id: Long = 0,
    @SerialName("user_id") val userId: String,
    @SerialName("grouping_criteria") val groupingCriteria: String,
    @SerialName("target_category") val targetCategory: String,
    @SerialName("target_weight") val targetWeight: Double
)
