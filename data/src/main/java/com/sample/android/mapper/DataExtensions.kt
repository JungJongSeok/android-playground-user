package com.sample.android.mapper

import com.sample.android.data.UserMetaData
import com.sample.android.domain.entity.User

/**
 * Extension functions to convert between domain entities and data layer models
 */

/**
 * Converts User domain entity to UserMetaData data model
 */
fun User.toUserMetaData(): UserMetaData {
    return UserMetaData(
        title = login,
        thumbnail = avatarUrl,
        url = htmlUrl,
        datetime = null
    )
}

/**
 * Converts UserMetaData data model to User domain entity
 */
fun UserMetaData.toUser(): User {
    return User(
        id = title?.hashCode()?.toLong() ?: 0L,
        login = title ?: "",
        avatarUrl = thumbnail ?: "",
        htmlUrl = url ?: "",
        type = "user",
        score = 0.0
    )
}