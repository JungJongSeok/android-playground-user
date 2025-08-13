package com.sample.android.ui.model

import android.os.Build
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Locale

@Keep
@Parcelize
data class UserUiData(
    val isFavorite: Boolean,
    val title: String?,
    val thumbnail: String?,
    val url: String?,
    val datetime: String?
) : Parcelable {
    val timestamp: Long
        get() {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).parse(
                        datetime ?: return 0L
                    )?.time ?: 0L
                } else {
                    0L
                }
            } catch (e: Exception) {
                0L
            }
        }
}

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

private fun List<UserUiData>.changeFavoriteStatus(
    userUiData: UserUiData,
    isFavorite: Boolean
): List<UserUiData> {
    return this.map { data ->
        if (data == userUiData) {
            data.copy(isFavorite = isFavorite)
        } else {
            data
        }
    }
}