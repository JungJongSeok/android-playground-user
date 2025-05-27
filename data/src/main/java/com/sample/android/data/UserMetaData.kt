package com.sample.android.data

import android.os.Parcelable
import androidx.annotation.Keep
import com.sample.android.network.response.UserResult
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Locale

@Keep
@Parcelize
data class UserMetaData(
    val title: String?,
    val thumbnail: String?,
    val url: String?,
    val datetime: String?
) : Parcelable {
    val timestamp: Long
        get() {
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).parse(
                datetime ?: return 0L
            )?.time ?: 0L
        }
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