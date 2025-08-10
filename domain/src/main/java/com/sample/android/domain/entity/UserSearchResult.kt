package com.sample.android.domain.entity

/**
 * Domain entity representing search results
 */
data class UserSearchResult(
    val users: List<User>,
    val totalCount: Int,
    val incompleteResults: Boolean
)