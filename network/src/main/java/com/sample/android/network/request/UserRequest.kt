package com.sample.android.network.request

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UserRequest(
    @SerializedName("seed")
    val seed: String,
    @SerializedName("page")
    val page: Int,
    @SerializedName("results")
    val results: Int = 10,
)