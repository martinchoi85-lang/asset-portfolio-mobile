package com.choi.assetportfolio.data.local.entity

import java.time.ZonedDateTime

/**
 * 로컬 캐시 및 오프라인 동기화를 위한 트랜잭션 엔티티.
 * isDirty 플래그를 통해 서버(Supabase)와의 동기화 여부를 관리합니다.
 */
data class LocalTransactionEntity(
    val localId: Long = 0,
    val transactionDate: ZonedDateTime,
    val assetId: Long,
    val accountId: String,
    val tradeType: String,
    val quantity: Double,
    val price: Double,
    val fee: Double = 0.0,
    val tax: Double = 0.0,
    val memo: String? = null,
    
    // 동기화 상태 관리를 위한 필드
    val isDirty: Boolean = false, // true일 경우 서버에 Upsert 필요
    val syncError: String? = null // 동기화 실패 시 에러 메시지 보관
)
