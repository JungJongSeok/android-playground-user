package com.sample.android.data

import android.os.Build
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