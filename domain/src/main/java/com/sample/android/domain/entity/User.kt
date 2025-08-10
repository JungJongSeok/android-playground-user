package com.sample.android.domain.entity

/**
 * Domain entity representing a User
 * Pure business model without any framework dependencies
 */
data class User(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String,
    val type: String,
    val score: Double
)