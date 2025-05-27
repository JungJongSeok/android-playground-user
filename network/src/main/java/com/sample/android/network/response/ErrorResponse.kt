package com.sample.android.network.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ErrorResponse(
    @SerializedName("errorType")
    val errorType: String?,
    @SerializedName("message")
    val message: String?
)