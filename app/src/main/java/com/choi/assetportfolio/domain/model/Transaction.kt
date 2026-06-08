package com.choi.assetportfolio.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.security.MessageDigest
import java.time.ZonedDateTime
import com.choi.assetportfolio.core.serialization.ZonedDateTimeSerializer

@Serializable
data class AssetRef(
    @SerialName("name_kr")
    val nameKr: String = "",
    val currency: String = "won",
    @SerialName("asset_type")
    val assetType: String = "",
    val market: String? = null,
    @SerialName("underlying_asset_class")
    val underlyingAssetClass: String = "",
    @SerialName("economic_exposure_region")
    val economicExposureRegion: String = "",
    @SerialName("vehicle_type")
    val vehicleType: String = "",
    @SerialName("lookthrough_available")
    val lookthroughAvailable: Boolean = false,
    @SerialName("price_source")
    val priceSource: String? = null
)

@Serializable
data class Transaction(
    val id: Long = 0L,
    @SerialName("transaction_date")
    @Serializable(with = ZonedDateTimeSerializer::class)
    val transactionDate: ZonedDateTime,
    @SerialName("asset_id")
    val assetId: Long,
    @SerialName("account_id")
    val accountId: String, // UUID
    @SerialName("trade_type")
    val tradeType: String,
    val quantity: Double,
    val price: Double,
    val fee: Double = 0.0,
    val tax: Double = 0.0,
    val memo: String? = null,
    @SerialName("realized_pnl")
    val realizedPnl: Double = 0.0,
    @SerialName("is_external_flow")
    val isExternalFlow: Boolean = true,
    @SerialName("parent_transaction_id")
    val parentTransactionId: Long? = null,
    val assets: AssetRef? = null
) {
    /**
     * 비즈니스 로직: 데이터 중복 삽입 방지를 위한 고유 해시 키 생성.
     * GUIDE_RAIL.md 원칙에 따라 date + ticker + amount + type + account_id 조합을 사용합니다.
     */
    fun generateHashKey(ticker: String, amount: Double): String {
        val rawString = "${transactionDate.toString().substringBefore("T")}_${ticker}_${amount}_${tradeType}_${accountId}"
        val bytes = MessageDigest.getInstance("SHA-256").digest(rawString.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
