package com.sample.android.ui.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.sample.android.data.UserMetaData
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class UserUiData(
    val isFavorite: Boolean,
    val data: UserMetaData
) : Parcelable

fun List<UserUiData>.removeUiData(userUiData: UserUiData): List<UserUiData> {
    return this.mapNotNull { data ->
        if (data == userUiData) {
            null
        } else {
            data
        }
    }
}

fun List<UserUiData>.addUiData(userUiData: UserUiData): List<UserUiData> {
    return this + listOf(userUiData)
}

fun List<UserUiData>.like(userUiData: UserUiData): List<UserUiData> {
    return changeFavoriteStatus(userUiData, true)
}

fun List<UserUiData>.unlike(userUiData: UserUiData): List<UserUiData> {
    return changeFavoriteStatus(userUiData, false)
}

private fun List<UserUiData>.changeFavoriteStatus(userUiData: UserUiData, isFavorite: Boolean): List<UserUiData> {
    return this.map { data ->
        if (data == userUiData) {
            data.copy(isFavorite = isFavorite)
        } else {
            data
        }
    }
}