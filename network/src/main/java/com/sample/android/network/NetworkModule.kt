package com.sample.android.network

import android.net.Uri
import androidx.annotation.VisibleForTesting
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
    private const val BASE_URL = "https://randomuser.me"

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
            endpoint, method, createRequestBody(requestBodyData), queries, headers
        )
    }

    @VisibleForTesting
    internal fun getBaseUrl(): String = BASE_URL

    @VisibleForTesting
    internal fun buildUrl(endpoint: String): String {
        return if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            endpoint
        } else {
            BASE_URL + endpoint
        }
    }

    @VisibleForTesting
    internal fun createRequestBody(requestBodyData: Any?): RequestBody? {
        return if (requestBodyData != null) {
            Gson().toJson(requestBodyData).toRequestBody()
        } else {
            null
        }
    }

    @VisibleForTesting
    internal fun buildUrlWithQueries(url: String, queries: Map<String, Any?>?): String {
        return Uri.parse(url)
            .buildUpon()
            .apply {
                queries?.forEach { pair ->
                    if (pair.value != null) {
                        appendQueryParameter(pair.key, pair.value.toString())
                    }
                }
            }
            .build().toString()
    }

    @VisibleForTesting
    internal suspend inline fun <reified T : Any> internalCall(
        endpoint: String,
        method: Method,
        requestBody: RequestBody?,
        queries: Map<String, Any?>? = null,
        headers: Map<String, String> = emptyMap()
    ): T {
        return internalCallImpl(endpoint, method, requestBody, queries, headers, T::class.java)
    }

    @VisibleForTesting
    internal suspend fun <T : Any> internalCallImpl(
        endpoint: String,
        method: Method,
        requestBody: RequestBody?,
        queries: Map<String, Any?>? = null,
        headers: Map<String, String> = emptyMap(),
        clazz: Class<T>
    ): T {
        val url = buildUrl(endpoint)
        val finalUrl = buildUrlWithQueries(url, queries)

        val request = buildRequest(finalUrl, method, requestBody, headers)
        return request.handelResultImpl(clazz)
    }

    @VisibleForTesting
    internal fun buildRequest(
        url: String,
        method: Method,
        requestBody: RequestBody?,
        headers: Map<String, String>
    ): Request {
        return Request.Builder()
            .url(url)
            .apply {
                headers(headers.toHeaders())
            }
            .method(method.name, requestBody)
            .build()
    }

    @VisibleForTesting
    internal suspend fun <T : Any> Request.handelResultImpl(clazz: Class<T>): T {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { result ->
                val client = provideOkHttpClient(
                    createInterceptor(this@handelResultImpl)
                ).newCall(this@handelResultImpl)

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
                            handleResponseImpl(response, result, clazz)
                        }
                    })
            }
        }
    }

    @VisibleForTesting
    internal fun <T : Any> handleResponseImpl(
        response: Response,
        result: kotlin.coroutines.Continuation<T>,
        clazz: Class<T>
    ) {
        val responseBody = response.body
        responseBody?.use { body ->
            if (response.isSuccessful) {
                handleSuccessfulResponseImpl(body.string(), result, clazz)
            } else {
                handleErrorResponse(response, body.string(), result)
            }
        } ?: result.resumeWithException(
            NetworkCommonException(
                CODE_NULL_POINTER_ERROR,
                message = "Failed null pointer error.",
                cause = NullPointerException()
            )
        )
    }

    @VisibleForTesting
    internal fun <T : Any> handleSuccessfulResponseImpl(
        bodyString: String,
        result: kotlin.coroutines.Continuation<T>,
        clazz: Class<T>
    ) {
        try {
            val data = createGson().fromJson(bodyString, clazz)
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
    }

    @VisibleForTesting
    internal fun handleErrorResponse(
        response: Response,
        bodyString: String,
        result: kotlin.coroutines.Continuation<*>
    ) {
        try {
            val data = createGson().fromJson(bodyString, ErrorResponse::class.java)
            result.resumeWithException(
                NetworkCommonException(
                    response.code,
                    data?.message ?: "Unknown error"
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

    @VisibleForTesting
    internal fun createGson(): Gson {
        return GsonBuilder().serializeNulls().create()
    }

    @VisibleForTesting
    internal fun createInterceptor(request: Request): Interceptor {
        return Interceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json;charset=UTF-8")
                    .url(request.url)
                    .build()
            ).newBuilder()
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .build()
        }
    }

    @VisibleForTesting
    internal fun provideOkHttpClient(
        apiClientInterceptor: Interceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(apiClientInterceptor)
            .addStethoInterceptor()
            .build()
}
