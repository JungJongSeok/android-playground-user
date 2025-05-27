package com.sample.android.network

import androidx.annotation.Keep

@Keep
class NetworkCommonException(
    val code: Int,
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message) {
    companion object {
        const val CODE_FAILED_NETWORK = 9900
        const val CODE_FAILED_JSON_PARSING = 9901
        const val CODE_NULL_POINTER_ERROR = 9902
    }
}