package com.choi.assetportfolio.domain.usecase

import com.choi.assetportfolio.domain.model.Transaction

import com.choi.assetportfolio.core.util.AppLogger

data class PortfolioYieldResult(
    val netInvestedAmount: Double,
    val timeWeightedReturn: Double
)

class CalculatePortfolioYieldUseCase {
    /**
     * is_external_flow == true 인 외부 자금 유출입만 투자 원금에 반영하여
     * 예수금 미러링으로 인한 TWR(시간가중수익률) 왜곡을 방지합니다.
     */
    operator fun invoke(
        transactions: List<Transaction>, 
        currentTotalValuation: Double,
        currencyConverter: CurrencyConverter = DefaultCurrencyConverter(),
        assetCurrencyMap: Map<Long, String> = emptyMap() // assetId -> currency
    ): PortfolioYieldResult {
        AppLogger.d("invoke - 포트폴리오 수익률 계산 시작", data = "transactionsCount=${transactions.size}, currentTotalValuation=$currentTotalValuation")
        var netInvestedAmount = 0.0
        
        for (tx in transactions) {
            if (tx.isExternalFlow) {
                val currency = assetCurrencyMap[tx.assetId] ?: "KRW"
                val rawFlowAmount = tx.price * tx.quantity
                
                // 외화 변환 방어 코드
                val flowAmount = try {
                    currencyConverter.convertToKrw(rawFlowAmount, currency)
                } catch (e: Exception) {
                    AppLogger.e("CalculatePortfolioYieldUseCase - 통화 변환 예외 발생", e)
                    rawFlowAmount
                }

                val type = tx.tradeType.uppercase()
                // 외부 유입(+)과 유출(-)을 분리 계산하여 최종 투자 원금을 산출합니다.
                if (type == "BUY" || type == "DEPOSIT") {
                    netInvestedAmount += flowAmount
                } else if (type == "SELL" || type == "WITHDRAW" || type == "WITHDRAWAL") {
                    netInvestedAmount -= flowAmount
                }
            }
        }

        val twr = if (netInvestedAmount > 0) {
            ((currentTotalValuation - netInvestedAmount) / netInvestedAmount) * 100.0
        } else {
            0.0
        }

        val result = PortfolioYieldResult(
            netInvestedAmount = netInvestedAmount,
            timeWeightedReturn = twr
        )
        
        AppLogger.d("invoke - 포트폴리오 수익률 계산 완료", data = "result=$result")
        return result
    }
}
