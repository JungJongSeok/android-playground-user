package com.sample.android.mapper

import com.sample.android.data.UserMetaData
import com.sample.android.domain.entity.User

fun User.toUserMetaData(): UserMetaData {
    return UserMetaData(
        title = login,
        thumbnail = avatarUrl,
        url = htmlUrl,
        datetime = null
    )
}

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