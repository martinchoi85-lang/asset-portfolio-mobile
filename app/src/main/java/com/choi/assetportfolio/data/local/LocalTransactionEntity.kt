package com.choi.assetportfolio.data.local

data class LocalTransactionEntity(
    val id: Long = 0,
    val transactionDate: String,
    val assetId: Long,
    val accountId: String,
    val tradeType: String,
    val quantity: Double,
    val price: Double
)
