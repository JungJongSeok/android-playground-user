package com.sample.android.network

import android.content.Context
import okhttp3.OkHttpClient


internal class InterceptorModule {
    companion object {
        fun initializeWithDefaults(context: Context) {
            // Do nothing
        }
    }
}

internal fun OkHttpClient.Builder.addStethoInterceptor(): OkHttpClient.Builder {
    return this
}