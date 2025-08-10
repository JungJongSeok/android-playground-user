package com.sample.android.domain.entity

data class User(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val htmlUrl: String,
    val type: String,
    val score: Double
)