package com.sample.android.ui.mapper

import com.sample.android.data.model.UserMetaData
import com.sample.android.ui.model.UserUiData

fun UserMetaData.toUiData(isFavorite: Boolean = false): UserUiData {
    return UserUiData(
        isFavorite = isFavorite,
        title = this.title,
        thumbnail = this.thumbnail,
        url = this.url,
        datetime = this.datetime
    )
}