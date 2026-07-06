package com.choi.assetportfolio.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.time.LocalDate
import com.choi.assetportfolio.core.serialization.LocalDateSerializer

@Serializable
data class DailySnapshot(
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    @SerialName("asset_id")
    val assetId: Long,
    @SerialName("account_id")
    val accountId: String, // UUID
    val quantity: Double,
    @SerialName("valuation_price")
    val valuationPrice: Double,
    @SerialName("purchase_price")
    val purchasePrice: Double,
    @SerialName("valuation_amount")
    val valuationAmount: Double,
    @SerialName("purchase_amount")
    val purchaseAmount: Double,
    val currency: String,
    @SerialName("snapshot_price_source")
    val snapshotPriceSource: String? = null
)
