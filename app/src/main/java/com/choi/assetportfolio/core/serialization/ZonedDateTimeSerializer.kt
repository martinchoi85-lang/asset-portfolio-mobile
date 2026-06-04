package com.choi.assetportfolio.core.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.choi.assetportfolio.core.util.AppLogger

object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ZonedDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        val str = decoder.decodeString()
        return try {
            ZonedDateTime.parse(str)
        } catch (e: Exception) {
            AppLogger.d("ZonedDateTime 1차 파싱 실패, 정규화 진행", data = "Raw: $str, Error: ${e.message}")
            val normalized = str.replace(" ", "T")
            try {
                ZonedDateTime.parse(normalized)
            } catch (e2: Exception) {
                AppLogger.d("ZonedDateTime 2차 파싱 실패, OffsetDateTime 시도", data = "Normalized: $normalized, Error: ${e2.message}")
                try {
                    java.time.OffsetDateTime.parse(normalized).toZonedDateTime()
                } catch (e3: Exception) {
                    AppLogger.e("ZonedDateTime 최종 파싱 실패", error = e3)
                    throw e3
                }
            }
        }
    }
}
