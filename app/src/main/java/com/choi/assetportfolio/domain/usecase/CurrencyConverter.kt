package com.choi.assetportfolio.domain.usecase

import com.choi.assetportfolio.core.util.AppLogger

interface CurrencyConverter {
    fun convertToKrw(amount: Double, currency: String): Double
}

class DefaultCurrencyConverter : CurrencyConverter {
    override fun convertToKrw(amount: Double, currency: String): Double {
        if (currency.uppercase() == "KRW") return amount
        
        return try {
            // 기초적인 환율 변환 뼈대 (임시 하드코딩 비율, 추후 API 연동 필요)
            val rate = when (currency.uppercase()) {
                "USD" -> 1400.0
                "JPY" -> 9.0
                "EUR" -> 1500.0
                else -> 1.0 // 지원하지 않는 통화는 에러 방지용 1.0 처리 (안전장치)
            }
            amount * rate
        } catch (e: Exception) {
            AppLogger.e("통화 변환 중 예외 발생: $currency", e)
            amount // 예외 발생 시 원금 그대로 반환하여 앱 중단 방지
        }
    }
}
