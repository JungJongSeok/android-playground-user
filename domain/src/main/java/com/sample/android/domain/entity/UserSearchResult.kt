package com.sample.android.domain.entity

data class UserSearchResult(
    val users: List<User>,
    val totalCount: Int,
    val incompleteResults: Boolean
)