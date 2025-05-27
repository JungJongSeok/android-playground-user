package com.sample.android.network

import android.content.Context
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import okhttp3.OkHttpClient

internal class InterceptorModule {
    companion object {
        fun initializeWithDefaults(context: Context) {
            Stetho.initializeWithDefaults(context)
        }
    }
}

internal fun OkHttpClient.Builder.addStethoInterceptor(): OkHttpClient.Builder {
    return this.addNetworkInterceptor(StethoInterceptor())
        .addInterceptor(OkHttpProfilerInterceptor())
}