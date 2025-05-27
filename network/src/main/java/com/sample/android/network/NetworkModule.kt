package com.sample.android.network

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sample.android.network.NetworkCommonException.Companion.CODE_FAILED_JSON_PARSING
import com.sample.android.network.NetworkCommonException.Companion.CODE_FAILED_NETWORK
import com.sample.android.network.NetworkCommonException.Companion.CODE_NULL_POINTER_ERROR
import com.sample.android.network.response.ErrorResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


internal object NetworkModule {
    internal enum class Method {
        GET,
        POST,
        PUT,
        DELETE,
    }

    internal suspend inline fun <reified T : Any> call(
        method: Method,
        endpoint: String,
        requestBodyData: Any? = null,
        queries: Map<String, Any?>? = null,
        headers: Map<String, String> = emptyMap()
    ): T {
        return internalCall(
            endpoint, method, if (requestBodyData != null) {
                Gson().toJson(requestBodyData).toRequestBody()
            } else {
                null
            }, queries, headers
        )
    }

    private suspend inline fun <reified T : Any> internalCall(
        endpoint: String,
        method: Method,
        requestBody: RequestBody?,
        queries: Map<String, Any?>? = null,
        headers: Map<String, String> = emptyMap()
    ): T {
        val request = Request.Builder()
            .url(
                Uri.parse(endpoint)
                    .buildUpon()
                    .apply {
                        queries?.forEach { pair ->
                            if (pair.value != null) {
                                appendQueryParameter(pair.key, pair.value.toString())
                            }
                        }
                    }
                    .build().toString()
            )
            .apply {
                headers(headers.toHeaders())
            }
            .method(method.name, requestBody)
            .build()

        return request.internalResult()
    }

    private suspend inline fun <reified T : Any> Request.internalResult(): T {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { result ->
                val client = provideOkHttpClient(
                    Interceptor { chain ->
                        chain.proceed(
                            chain.request().newBuilder()
                                .addHeader("Content-Type", "application/json;charset=UTF-8")
                                .url(this@internalResult.url)
                                .build()
                        ).newBuilder()
                            .addHeader("Content-Type", "application/json;charset=UTF-8")
                            .build()
                    },
                ).newCall(this@internalResult)

                result.invokeOnCancellation {
                    client.cancel()
                }

                client.enqueue(
                    responseCallback = object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            result.resumeWithException(
                                NetworkCommonException(
                                    CODE_FAILED_NETWORK,
                                    message = "Failed network.",
                                    cause = e
                                )
                            )
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseBody = response.body
                            responseBody?.use { body ->
                                if (response.isSuccessful) {
                                    try {
                                        val data = GsonBuilder()
                                            .serializeNulls().create()
                                            .fromJson(body.string(), T::class.java)
                                        result.resume(data)
                                    } catch (e: Exception) {
                                        result.resumeWithException(
                                            NetworkCommonException(
                                                CODE_FAILED_JSON_PARSING,
                                                message = "Failed json parsing.",
                                                cause = e
                                            )
                                        )
                                    }
                                } else {
                                    try {
                                        val data = GsonBuilder()
                                            .serializeNulls().create()
                                            .fromJson(
                                                body.string(),
                                                ErrorResponse::class.java
                                            )
                                        result.resumeWithException(
                                            NetworkCommonException(
                                                response.code,
                                                data.message
                                            )
                                        )
                                    } catch (e: Exception) {
                                        result.resumeWithException(
                                            NetworkCommonException(
                                                code = response.code,
                                                message = response.message,
                                                cause = e
                                            )
                                        )
                                    }
                                }
                            } ?: result.resumeWithException(
                                NetworkCommonException(
                                    CODE_NULL_POINTER_ERROR,
                                    message = "Failed null pointer error.",
                                    cause = NullPointerException()
                                )
                            )
                        }
                    })
            }
        }
    }

    private fun provideOkHttpClient(
        apiClientInterceptor: Interceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(apiClientInterceptor)
            .addStethoInterceptor()
            .build()
}
