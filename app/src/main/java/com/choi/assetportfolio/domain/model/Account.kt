package com.choi.assetportfolio.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Account(
    val id: String, // UUID
    val name: String,
    val brokerage: String,
    @SerialName("old_owner")
    val oldOwner: String,
    val type: String,
    @SerialName("user_id")
    val userId: String, // UUID
    val currency: String = "KRW"
)
