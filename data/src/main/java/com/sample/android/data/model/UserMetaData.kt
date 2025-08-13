package com.sample.android.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.sample.android.network.response.UserResult
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class UserMetaData(
    val title: String?,
    val thumbnail: String?,
    val url: String?,
    val datetime: String?
) : Parcelable {
}

@Keep
data class UserMetaDataList(
    val users: List<UserMetaData>,
)

internal fun UserResult.toData(): UserMetaData {
    return UserMetaData(
        thumbnail = this.picture?.thumbnail,
        title = (this.name?.first ?: "") + " " + (this.name?.last ?: ""),
        url = this.email,
        datetime = this.dob?.date
    )
}