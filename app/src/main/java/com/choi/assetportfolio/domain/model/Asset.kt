package com.choi.assetportfolio.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.time.ZonedDateTime
import com.choi.assetportfolio.core.serialization.ZonedDateTimeSerializer

@Serializable
data class Asset(
    val id: Long,
    val ticker: String,
    @SerialName("name_kr")
    val nameKr: String,
    @SerialName("asset_type")
    val assetType: String,
    val currency: String,
    val market: String?,
    @SerialName("current_price")
    val currentPrice: Double,
    @SerialName("underlying_asset_class")
    val underlyingAssetClass: String,
    @SerialName("economic_exposure_region")
    val economicExposureRegion: String,
    @SerialName("asset_nature")
    val assetNature: String?,
    @SerialName("vehicle_type")
    val vehicleType: String,
    @SerialName("fx_exposure_type")
    val fxExposureType: String?,
    @SerialName("return_driver")
    val returnDriver: String?,
    @SerialName("strategy_type")
    val strategyType: String?,
    @SerialName("lookthrough_available")
    val lookthroughAvailable: Boolean,
    @SerialName("price_updated_at")
    @Serializable(with = ZonedDateTimeSerializer::class)
    val priceUpdatedAt: ZonedDateTime?,
    @SerialName("price_update_status")
    val priceUpdateStatus: String?,
    @SerialName("price_update_error")
    val priceUpdateError: String?,
    @SerialName("price_source")
    val priceSource: String?
)
