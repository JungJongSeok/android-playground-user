package com.sample.android.mapper

import com.sample.android.domain.entity.User
import com.sample.android.domain.entity.UserSearchResult
import com.sample.android.network.response.UserResponse
import com.sample.android.network.response.UserResult

/**
 * Extension functions to convert network models to domain entities
 */

/**
 * Converts UserResponse to UserSearchResult domain entity
 */
fun UserResponse.toUserSearchResult(): UserSearchResult {
    val users = results?.map { it.toUser() } ?: emptyList()
    return UserSearchResult(
        users = users,
        totalCount = results?.size ?: 0,
        incompleteResults = false
    )
}

/**
 * Converts UserResult to User domain entity
 */
fun UserResult.toUser(): User {
    return User(
        id = id?.value?.hashCode()?.toLong() ?: 0L,
        login = login?.username ?: "",
        avatarUrl = picture?.thumbnail ?: "",
        htmlUrl = email ?: "",
        type = "user",
        score = 0.0
    )
}