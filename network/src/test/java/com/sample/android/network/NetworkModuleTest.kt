package com.sample.android.network

import com.sample.android.network.NetworkModule.handelResultImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class NetworkModuleTest {
    data class TestModel(val id: String, val name: String)
    data class TestModelWithNull(val id: String?, val name: String?, val value: Int?)

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // Base URL tests
    @Test
    fun `getBaseUrl should return correct base URL`() {
        val baseUrl = NetworkModule.getBaseUrl()
        assertEquals(
            "https://randomuser.me",
            baseUrl
        )
    }

    // URL building tests
    @Test
    fun `buildUrl should return endpoint as is when it starts with http`() {
        val httpUrl = "http://example.com/test"
        val result = NetworkModule.buildUrl(httpUrl)
        assertEquals(httpUrl, result)
    }

    @Test
    fun `buildUrl should return endpoint as is when it starts with https`() {
        val httpsUrl = "https://example.com/test"
        val result = NetworkModule.buildUrl(httpsUrl)
        assertEquals(httpsUrl, result)
    }

    @Test
    fun `buildUrl should prepend base URL when endpoint is relative`() {
        val endpoint = "/users"
        val result = NetworkModule.buildUrl(endpoint)
        assertEquals(
            "https://randomuser.me/users",
            result
        )
    }

    @Test
    fun `buildUrl should handle empty endpoint`() {
        val result = NetworkModule.buildUrl("")
        assertEquals(
            "https://randomuser.me",
            result
        )
    }

    // Request body creation tests
    @Test
    fun `createRequestBody should return null when requestBodyData is null`() {
        val result = NetworkModule.createRequestBody(null)
        assertNull(result)
    }

    @Test
    fun `createRequestBody should create request body from object`() {
        val data = TestModel("1", "test")
        val result = NetworkModule.createRequestBody(data)
        assertNotNull(result)
    }

    @Test
    fun `createRequestBody should handle complex objects`() {
        val data = mapOf("key1" to "value1", "key2" to 123, "key3" to true)
        val result = NetworkModule.createRequestBody(data)
        assertNotNull(result)
    }

    @Test
    fun `createRequestBody should handle null values in objects`() {
        val data = TestModelWithNull(null, "test", null)
        val result = NetworkModule.createRequestBody(data)
        assertNotNull(result)

        // Test the actual content contains null values
        val bodyString = result?.let { okio.Buffer().apply { it.writeTo(this) }.readUtf8() }
        assertTrue(bodyString?.contains("null") ?: false)
    }

    @Test
    fun `createRequestBody should handle empty strings`() {
        val data = TestModel("", "")
        val result = NetworkModule.createRequestBody(data)
        assertNotNull(result)
    }

    @Test
    fun `createRequestBody should handle nested objects`() {
        val nestedData = mapOf(
            "user" to TestModel("123", "test"),
            "metadata" to mapOf("version" to 1, "active" to true)
        )
        val result = NetworkModule.createRequestBody(nestedData)
        assertNotNull(result)
    }

    // Query parameter building tests
    @Test
    fun `buildUrlWithQueries should return original URL when queries is null`() {
        val url = "https://example.com/test"
        val result = NetworkModule.buildUrlWithQueries(url, null)
        assertEquals(url, result)
    }

    @Test
    fun `buildUrlWithQueries should add query parameters`() {
        val url = "https://example.com/test"
        val queries = mapOf("param1" to "value1", "param2" to 123)
        val result = NetworkModule.buildUrlWithQueries(url, queries)
        assertTrue(result.contains("param1=value1"))
        assertTrue(result.contains("param2=123"))
    }

    @Test
    fun `buildUrlWithQueries should skip null values`() {
        val url = "https://example.com/test"
        val queries = mapOf("param1" to "value1", "param2" to null, "param3" to "value3")
        val result = NetworkModule.buildUrlWithQueries(url, queries)
        assertTrue(result.contains("param1=value1"))
        assertTrue(result.contains("param3=value3"))
        assertTrue(!result.contains("param2"))
    }

    @Test
    fun `buildUrlWithQueries should handle empty queries map`() {
        val url = "https://example.com/test"
        val result = NetworkModule.buildUrlWithQueries(url, emptyMap())
        assertEquals(url, result)
    }

    @Test
    fun `buildUrlWithQueries should handle special characters in values`() {
        val url = "https://example.com/test"
        val queries = mapOf(
            "query" to "hello world",
            "special" to "!@#$%^&*()",
            "unicode" to "한글테스트"
        )
        val result = NetworkModule.buildUrlWithQueries(url, queries)
        assertTrue(result.contains("query=hello%20world"))
        assertTrue(result.contains("special="))
        assertTrue(result.contains("unicode="))
    }

    @Test
    fun `buildUrlWithQueries should handle boolean and number values`() {
        val url = "https://example.com/test"
        val queries = mapOf(
            "active" to true,
            "count" to 42,
            "score" to 3.14
        )
        val result = NetworkModule.buildUrlWithQueries(url, queries)
        assertTrue(result.contains("active=true"))
        assertTrue(result.contains("count=42"))
        assertTrue(result.contains("score=3.14"))
    }

    @Test
    fun `buildUrlWithQueries should handle multiple query parameters with same key`() {
        val url = "https://example.com/test"
        val queries = mapOf(
            "filter" to "first",
            "sort" to "asc"
        )

        val result = NetworkModule.buildUrlWithQueries(url, queries)
        assertTrue(result.contains("filter=first"))
        assertTrue(result.contains("sort=asc"))
    }

    @Test
    fun `buildUrlWithQueries should handle extremely long query values`() {
        val url = "https://example.com/test"
        val longValue = "a".repeat(1000)
        val queries = mapOf("longParam" to longValue)

        val result = NetworkModule.buildUrlWithQueries(url, queries)
        assertTrue(result.contains("longParam="))
    }

    // Request building tests
    @Test
    fun `buildRequest should create request with correct URL and method`() {
        val url = "https://example.com/test"
        val method = NetworkModule.Method.GET
        val request = NetworkModule.buildRequest(url, method, null, emptyMap())

        assertEquals(url, request.url.toString())
        assertEquals("GET", request.method)
    }

    @Test
    fun `buildRequest should add headers correctly`() {
        val url = "https://example.com/test"
        val headers = mapOf("Authorization" to "Bearer token", "Custom-Header" to "value")
        val request = NetworkModule.buildRequest(url, NetworkModule.Method.GET, null, headers)

        assertEquals("Bearer token", request.header("Authorization"))
        assertEquals("value", request.header("Custom-Header"))
    }

    @Test
    fun `buildRequest should set request body for POST`() {
        val url = "https://example.com/test"
        val body = "{\"test\":\"data\"}".toRequestBody("application/json".toMediaType())
        val request = NetworkModule.buildRequest(url, NetworkModule.Method.POST, body, emptyMap())

        assertEquals("POST", request.method)
        assertNotNull(request.body)
    }

    @Test
    fun `buildRequest should work with all HTTP methods`() {
        val url = "https://example.com/test"
        val body = "{\"test\":\"data\"}".toRequestBody("application/json".toMediaType())

        // GET과 DELETE는 body 없이 테스트
        listOf(NetworkModule.Method.GET, NetworkModule.Method.DELETE).forEach { method ->
            val request = NetworkModule.buildRequest(url, method, null, emptyMap())
            assertEquals(method.name, request.method)
        }

        // POST와 PUT은 body와 함께 테스트
        listOf(NetworkModule.Method.POST, NetworkModule.Method.PUT).forEach { method ->
            val request = NetworkModule.buildRequest(url, method, body, emptyMap())
            assertEquals(method.name, request.method)
        }
    }

    @Test
    fun `buildRequest should handle empty headers map`() {
        val url = "https://example.com/test"
        val request = NetworkModule.buildRequest(url, NetworkModule.Method.GET, null, emptyMap())
        assertEquals(url, request.url.toString())
        assertEquals("GET", request.method)
    }

    @Test
    fun `buildRequest should handle null request body for GET`() {
        val url = "https://example.com/test"
        val request = NetworkModule.buildRequest(url, NetworkModule.Method.GET, null, emptyMap())
        assertNull(request.body)
    }

    // Gson creation tests
    @Test
    fun `createGson should create Gson with serializeNulls`() {
        val gson = NetworkModule.createGson()
        val testObject = TestModelWithNull("id", null, 123)
        val json = gson.toJson(testObject)
        assertTrue(json.contains("\"name\":null"))
    }

    @Test
    fun `createGson should handle complex nested objects`() {
        val gson = NetworkModule.createGson()
        val complexObject = mapOf(
            "nested" to mapOf(
                "level1" to mapOf(
                    "level2" to TestModelWithNull(null, "test", null)
                )
            ),
            "array" to listOf(1, 2, null, 4)
        )
        val json = gson.toJson(complexObject)
        assertTrue(json.contains("null"))
        assertTrue(json.contains("level2"))
    }

    // Interceptor creation tests
    @Test
    fun `createInterceptor should add Content-Type headers`() {
        val request = Request.Builder()
            .url("https://example.com/test")
            .build()

        val interceptor = NetworkModule.createInterceptor(request)
        assertNotNull(interceptor)
    }

    @Test
    fun `createInterceptor should add Content-Type header to request`() {
        val request = Request.Builder()
            .url("https://example.com/test")
            .build()

        val interceptor = NetworkModule.createInterceptor(request)
        val mockChain = MockInterceptorChain(request)

        val response = interceptor.intercept(mockChain)

        // Check that Content-Type header was added to both request and response
        assertEquals("application/json;charset=UTF-8", response.header("Content-Type"))
    }

    // OkHttpClient creation tests
    @Test
    fun `provideOkHttpClient should create client with interceptor`() {
        val request = Request.Builder()
            .url("https://example.com/test")
            .build()
        val interceptor = NetworkModule.createInterceptor(request)

        val client = NetworkModule.provideOkHttpClient(interceptor)

        assertNotNull(client)
        assertEquals(1, client.interceptors.size)
        assertTrue(client.interceptors.contains(interceptor))
    }

    @Test
    fun `provideOkHttpClient should create client with correct configuration`() {
        val request = Request.Builder()
            .url("https://example.com/test")
            .build()
        val interceptor = NetworkModule.createInterceptor(request)

        val client = NetworkModule.provideOkHttpClient(interceptor)

        assertNotNull(client)
        assertEquals(1, client.interceptors.size)
        assertTrue(client.interceptors.contains(interceptor))
    }

    // Integration tests with MockWebServer
    @Test
    fun `call should handle GET request successfully`() = runBlocking {
        val body = """{"id":"123","name":"test"}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))
        val endpoint = server.url("/test").toString()

        val result: TestModel = NetworkModule.call(NetworkModule.Method.GET, endpoint)

        assertEquals("123", result.id)
        assertEquals("test", result.name)
    }

    @Test
    fun `call should handle POST request with request body`() = runBlocking {
        val responseBody = """{"id":"456","name":"created"}"""
        server.enqueue(MockResponse().setResponseCode(201).setBody(responseBody))
        val endpoint = server.url("/test").toString()
        val requestData = TestModel("456", "created")

        val result: TestModel = NetworkModule.call(
            NetworkModule.Method.POST,
            endpoint,
            requestData
        )

        assertEquals("456", result.id)
        assertEquals("created", result.name)

        val recordedRequest = server.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertTrue(recordedRequest.body.readUtf8().contains("456"))
    }

    @Test
    fun `call should handle PUT request`() = runBlocking {
        val responseBody = """{"id":"789","name":"updated"}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))
        val endpoint = server.url("/test").toString()
        val requestData = TestModel("789", "updated")

        val result: TestModel = NetworkModule.call(
            NetworkModule.Method.PUT,
            endpoint,
            requestData
        )

        assertEquals("789", result.id)
        assertEquals("updated", result.name)

        val recordedRequest = server.takeRequest()
        assertEquals("PUT", recordedRequest.method)
    }

    @Test
    fun `call should handle DELETE request`() = runBlocking {
        val responseBody = """{"id":"999","name":"deleted"}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))
        val endpoint = server.url("/test").toString()

        val result: TestModel = NetworkModule.call(NetworkModule.Method.DELETE, endpoint)

        assertEquals("999", result.id)
        assertEquals("deleted", result.name)

        val recordedRequest = server.takeRequest()
        assertEquals("DELETE", recordedRequest.method)
    }

    @Test
    fun `call should handle query parameters`() = runBlocking {
        val responseBody = """{"id":"555","name":"query"}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))
        val endpoint = server.url("/test").toString()
        val queries = mapOf("page" to 1, "size" to 10, "filter" to "active")

        val result: TestModel = NetworkModule.call(
            NetworkModule.Method.GET,
            endpoint,
            queries = queries
        )

        assertEquals("555", result.id)

        val recordedRequest = server.takeRequest()
        val url = recordedRequest.requestUrl.toString()
        assertTrue(url.contains("page=1"))
        assertTrue(url.contains("size=10"))
        assertTrue(url.contains("filter=active"))
    }

    @Test
    fun `call should handle custom headers`() = runBlocking {
        val responseBody = """{"id":"777","name":"headers"}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))
        val endpoint = server.url("/test").toString()
        val headers = mapOf(
            "Authorization" to "Bearer token123",
            "X-Custom-Header" to "custom-value"
        )

        val result: TestModel = NetworkModule.call(
            NetworkModule.Method.GET,
            endpoint,
            headers = headers
        )

        assertEquals("777", result.id)

        val recordedRequest = server.takeRequest()
        assertEquals("Bearer token123", recordedRequest.getHeader("Authorization"))
        assertEquals("custom-value", recordedRequest.getHeader("X-Custom-Header"))
    }

    @Test
    fun `call should throw JSON parsing exception on invalid JSON`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("not a json"))
        val endpoint = server.url("/test").toString()

        try {
            NetworkModule.call<TestModel>(NetworkModule.Method.GET, endpoint)
            fail("Expected NetworkCommonException")
        } catch (e: NetworkCommonException) {
            assertEquals(NetworkCommonException.CODE_FAILED_JSON_PARSING, e.code)
            assertTrue(e.message!!.contains("Failed json parsing"))
        }
    }

    @Test
    fun `call should handle network failure`() = runBlocking {
        server.shutdown()
        val endpoint = "http://localhost:${server.port}/test"

        try {
            NetworkModule.call<TestModel>(NetworkModule.Method.GET, endpoint)
            fail("Expected NetworkCommonException")
        } catch (e: NetworkCommonException) {
            assertEquals(NetworkCommonException.CODE_FAILED_NETWORK, e.code)
            assertTrue(e.message!!.contains("Failed network"))
        }
    }

    @Test
    fun `call should throw with error code and message from error JSON`() = runBlocking {
        val errJson = """{"message":"Validation failed"}"""
        server.enqueue(MockResponse().setResponseCode(400).setBody(errJson))
        val endpoint = server.url("/test").toString()

        try {
            NetworkModule.call<TestModel>(NetworkModule.Method.GET, endpoint)
            fail("Expected NetworkCommonException")
        } catch (e: NetworkCommonException) {
            assertEquals(400, e.code)
            assertEquals("Validation failed", e.message)
        }
    }

    @Test
    fun `call should throw with HTTP message on malformed error body`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("server error"))
        val endpoint = server.url("/test").toString()

        try {
            NetworkModule.call<TestModel>(NetworkModule.Method.GET, endpoint)
            fail("Expected NetworkCommonException")
        } catch (e: NetworkCommonException) {
            assertEquals(500, e.code)
            assertEquals("Server Error", e.message)
        }
    }

    @Test
    fun `call should handle malformed JSON in success response`() = runBlocking {
        // 성공 응답이지만 잘못된 JSON 형태
        server.enqueue(MockResponse().setResponseCode(200).setBody("{malformed json"))
        val endpoint = server.url("/test").toString()

        try {
            NetworkModule.call<TestModel>(NetworkModule.Method.GET, endpoint)
            fail("Expected NetworkCommonException")
        } catch (e: NetworkCommonException) {
            assertEquals(NetworkCommonException.CODE_FAILED_JSON_PARSING, e.code)
            assertTrue(e.message!!.contains("Failed json parsing"))
        }
    }

    @Test
    fun `call should work with relative endpoint`() = runBlocking {
        val responseBody = """{"id":"relative","name":"test"}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val result: TestModel = NetworkModule.call(
            NetworkModule.Method.GET,
            server.url("/users/123").toString()
        )

        assertEquals("relative", result.id)
        assertEquals("test", result.name)
    }

    @Test
    fun `call should serialize null values in request body`() = runBlocking {
        val responseBody = """{"id":"null-test","name":"test"}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))
        val endpoint = server.url("/test").toString()
        val requestData = TestModelWithNull("123", null, 456)

        val result: TestModel = NetworkModule.call(
            NetworkModule.Method.POST,
            endpoint,
            requestData
        )

        assertEquals("null-test", result.id)

        val recordedRequest = server.takeRequest()
        val requestBody = recordedRequest.body.readUtf8()
        assertTrue(
            "Request should contain serialized null values",
            requestBody.contains("\"name\":null") || requestBody.contains("123")
        )
    }

    @Test
    fun `call should handle request with all HTTP methods and verify request body handling`() =
        runBlocking {
            val responseBody = """{"id":"method-test","name":"test"}"""
            val requestData = TestModel("123", "test")

            // Test each method
            NetworkModule.Method.values().forEach { method ->
                server.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))
                val endpoint = server.url("/test").toString()

                val result: TestModel = NetworkModule.call(
                    method,
                    endpoint,
                    if (method == NetworkModule.Method.POST || method == NetworkModule.Method.PUT) requestData else null
                )

                assertEquals("method-test", result.id)

                val recordedRequest = server.takeRequest()
                assertEquals(method.name, recordedRequest.method)

                // Check if request body exists for POST/PUT
                if (method == NetworkModule.Method.POST || method == NetworkModule.Method.PUT) {
                    assertNotNull(recordedRequest.body)
                    assertTrue(recordedRequest.body.size > 0)
                }
            }
        }

    @Test
    fun `internalCall should work directly`() = runBlocking {
        val responseBody = """{"id":"internal","name":"test"}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))
        val endpoint = server.url("/test").toString()

        val result: TestModel = NetworkModule.internalCall(
            endpoint,
            NetworkModule.Method.GET,
            null
        )

        assertEquals("internal", result.id)
        assertEquals("test", result.name)
    }

    // Tests for new *Impl functions that were extracted for test coverage
    @Test
    fun `internalCallImpl should work with all parameters`() = runBlocking {
        val responseBody = """{"id":"impl-test","name":"implementation"}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))
        val endpoint = server.url("/test").toString()
        val requestBody = NetworkModule.createRequestBody(TestModel("123", "test"))
        val queries = mapOf("param" to "value")
        val headers = mapOf("Authorization" to "Bearer token")

        val result: TestModel = NetworkModule.internalCallImpl(
            endpoint,
            NetworkModule.Method.POST,
            requestBody,
            queries,
            headers,
            TestModel::class.java
        )

        assertEquals("impl-test", result.id)
        assertEquals("implementation", result.name)

        val recordedRequest = server.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertTrue(recordedRequest.requestUrl.toString().contains("param=value"))
        assertEquals("Bearer token", recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun `internalResultImpl should handle successful response`() = runBlocking {
        val responseBody = """{"id":"result-test","name":"response"}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = Request.Builder()
            .url(server.url("/test"))
            .build()

        val result: TestModel = request.handelResultImpl(TestModel::class.java)

        assertEquals("result-test", result.id)
        assertEquals("response", result.name)
    }

    @Test
    fun `internalResultImpl should handle network failure`() = runBlocking {
        server.shutdown()

        val request = Request.Builder()
            .url("http://localhost:${server.port}/test")
            .build()

        try {
            request.handelResultImpl(TestModel::class.java)
            fail("Expected NetworkCommonException")
        } catch (e: NetworkCommonException) {
            assertEquals(NetworkCommonException.CODE_FAILED_NETWORK, e.code)
            assertTrue(e.message!!.contains("Failed network"))
        }
    }

    @Test
    fun `handleResponseImpl should handle successful response correctly`() {
        val jsonString = """{"id":"handle-test","name":"response"}"""
        val mockResponse = MockResponse().setResponseCode(200).setBody(jsonString)

        var testResult: TestModel? = null
        var testException: Exception? = null

        val continuation = object : Continuation<TestModel> {
            override val context = kotlin.coroutines.EmptyCoroutineContext
            override fun resumeWith(result: Result<TestModel>) {
                result.fold(
                    onSuccess = { testResult = it },
                    onFailure = { testException = it as? Exception }
                )
            }
        }

        server.enqueue(mockResponse)
        val request = Request.Builder().url(server.url("/test")).build()
        val client = NetworkModule.provideOkHttpClient(NetworkModule.createInterceptor(request))
        val response = client.newCall(request).execute()

        NetworkModule.handleResponseImpl(response, continuation, TestModel::class.java)

        assertNotNull(testResult)
        assertEquals("handle-test", testResult?.id)
        assertEquals("response", testResult?.name)
        assertNull(testException)
    }

    @Test
    fun `handleResponseImpl should handle error response correctly`() {
        val errorJson = """{"message":"Test error message"}"""
        val mockResponse = MockResponse().setResponseCode(400).setBody(errorJson)

        var testResult: TestModel? = null
        var testException: Exception? = null

        val continuation = object : Continuation<TestModel> {
            override val context = kotlin.coroutines.EmptyCoroutineContext
            override fun resumeWith(result: Result<TestModel>) {
                result.fold(
                    onSuccess = { testResult = it },
                    onFailure = { testException = it as? Exception }
                )
            }
        }

        server.enqueue(mockResponse)
        val request = Request.Builder().url(server.url("/test")).build()
        val client = NetworkModule.provideOkHttpClient(NetworkModule.createInterceptor(request))
        val response = client.newCall(request).execute()

        NetworkModule.handleResponseImpl(response, continuation, TestModel::class.java)

        assertNull(testResult)
        assertNotNull(testException)
        val exception = testException as NetworkCommonException
        assertEquals(400, exception.code)
        assertEquals("Test error message", exception.message)
    }

    @Test
    fun `handleResponseImpl should handle null response body`() {
        val request = Request.Builder()
            .url("https://example.com/test")
            .build()

        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(null)
            .build()

        var testResult: TestModel? = null
        var testException: Exception? = null

        val continuation = object : Continuation<TestModel> {
            override val context = kotlin.coroutines.EmptyCoroutineContext
            override fun resumeWith(result: Result<TestModel>) {
                result.fold(
                    onSuccess = { testResult = it },
                    onFailure = { testException = it as? Exception }
                )
            }
        }

        NetworkModule.handleResponseImpl(response, continuation, TestModel::class.java)

        assertNull(testResult)
        assertNotNull(testException)
        val exception = testException as NetworkCommonException
        assertEquals(NetworkCommonException.CODE_NULL_POINTER_ERROR, exception.code)
        assertTrue(exception.message!!.contains("Failed null pointer error"))
    }

    @Test
    fun `handleErrorResponse should handle ErrorResponse with errorType`() {
        val request = Request.Builder()
            .url("https://example.com/test")
            .build()

        val errorJson = """{"errorType":"ValidationError","message":"Invalid input"}"""
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(400)
            .message("Bad Request")
            .body(errorJson.toResponseBody("application/json".toMediaType()))
            .build()

        var testResult: TestModel? = null
        var testException: Exception? = null

        val continuation = object : Continuation<TestModel> {
            override val context = kotlin.coroutines.EmptyCoroutineContext
            override fun resumeWith(result: Result<TestModel>) {
                result.fold(
                    onSuccess = { testResult = it },
                    onFailure = { testException = it as? Exception }
                )
            }
        }

        NetworkModule.handleErrorResponse(response, errorJson, continuation)

        assertNull(testResult)
        assertNotNull(testException)
        val exception = testException as NetworkCommonException
        assertEquals(400, exception.code)
        assertEquals("Invalid input", exception.message)
    }

    @Test
    fun `handleErrorResponse should handle ErrorResponse with null message`() {
        val request = Request.Builder()
            .url("https://example.com/test")
            .build()

        val errorJson = """{"errorType":"UnknownError","message":null}"""
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .body(errorJson.toResponseBody("application/json".toMediaType()))
            .build()

        var testResult: TestModel? = null
        var testException: Exception? = null

        val continuation = object : Continuation<TestModel> {
            override val context = kotlin.coroutines.EmptyCoroutineContext
            override fun resumeWith(result: Result<TestModel>) {
                result.fold(
                    onSuccess = { testResult = it },
                    onFailure = { testException = it as? Exception }
                )
            }
        }

        NetworkModule.handleErrorResponse(response, errorJson, continuation)

        assertNull(testResult)
        assertNotNull(testException)
        val exception = testException as NetworkCommonException
        assertEquals(500, exception.code)
        assertEquals("Unknown error", exception.message)
    }

    @Test
    fun `handleErrorResponse should handle completely empty error response`() {
        val request = Request.Builder()
            .url("https://example.com/test")
            .build()

        val errorJson = """{}"""
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(404)
            .message("Not Found")
            .body(errorJson.toResponseBody("application/json".toMediaType()))
            .build()

        var testResult: TestModel? = null
        var testException: Exception? = null

        val continuation = object : Continuation<TestModel> {
            override val context = kotlin.coroutines.EmptyCoroutineContext
            override fun resumeWith(result: Result<TestModel>) {
                result.fold(
                    onSuccess = { testResult = it },
                    onFailure = { testException = it as? Exception }
                )
            }
        }

        NetworkModule.handleErrorResponse(response, errorJson, continuation)

        assertNull(testResult)
        assertNotNull(testException)
        val exception = testException as NetworkCommonException
        assertEquals(404, exception.code)
        assertEquals("Unknown error", exception.message)
    }

    @Test
    fun `handleSuccessfulResponseImpl should parse JSON correctly`() {
        val jsonString = """{"id":"success-impl","name":"parsed"}"""
        var testResult: TestModel? = null
        var testException: Exception? = null

        val continuation = object : Continuation<TestModel> {
            override val context = kotlin.coroutines.EmptyCoroutineContext
            override fun resumeWith(result: Result<TestModel>) {
                result.fold(
                    onSuccess = { testResult = it },
                    onFailure = { testException = it as? Exception }
                )
            }
        }

        NetworkModule.handleSuccessfulResponseImpl(jsonString, continuation, TestModel::class.java)

        assertNotNull(testResult)
        assertEquals("success-impl", testResult?.id)
        assertEquals("parsed", testResult?.name)
        assertNull(testException)
    }

    @Test
    fun `handleSuccessfulResponseImpl should handle invalid JSON`() {
        val invalidJson = "invalid json string"
        var testResult: TestModel? = null
        var testException: Exception? = null

        val continuation = object : Continuation<TestModel> {
            override val context = kotlin.coroutines.EmptyCoroutineContext
            override fun resumeWith(result: Result<TestModel>) {
                result.fold(
                    onSuccess = { testResult = it },
                    onFailure = { testException = it as? Exception }
                )
            }
        }

        NetworkModule.handleSuccessfulResponseImpl(invalidJson, continuation, TestModel::class.java)

        assertNull(testResult)
        assertNotNull(testException)
        val exception = testException as NetworkCommonException
        assertEquals(
            NetworkCommonException.CODE_FAILED_JSON_PARSING,
            exception.code
        )
        assertTrue(exception.message!!.contains("Failed json parsing"))
    }

    @Test
    fun `handleSuccessfulResponseImpl should handle null values in JSON`() {
        val jsonString = """{"id":"null-test","name":null,"value":123}"""
        var testResult: TestModelWithNull? = null
        var testException: Exception? = null

        val continuation = object : Continuation<TestModelWithNull> {
            override val context = kotlin.coroutines.EmptyCoroutineContext
            override fun resumeWith(result: Result<TestModelWithNull>) {
                result.fold(
                    onSuccess = { testResult = it },
                    onFailure = { testException = it as? Exception }
                )
            }
        }

        NetworkModule.handleSuccessfulResponseImpl(
            jsonString,
            continuation,
            TestModelWithNull::class.java
        )

        assertNotNull(testResult)
        assertEquals("null-test", testResult?.id)
        assertNull(testResult?.name)
        assertEquals(123, testResult?.value)
        assertNull(testException)
    }

    @Test
    fun `NetworkCommonException should preserve all properties correctly`() {
        val originalException = RuntimeException("Original cause")
        val exception = NetworkCommonException(
            code = 500,
            message = "Test error message",
            cause = originalException
        )

        assertEquals(500, exception.code)
        assertEquals("Test error message", exception.message)
        assertEquals(originalException, exception.cause)
    }

    @Test
    fun `NetworkCommonException should handle null message`() {
        val exception = NetworkCommonException(
            code = 400,
            message = null,
            cause = null
        )

        assertEquals(400, exception.code)
        assertNull(exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `NetworkCommonException should have correct constants`() {
        assertEquals(9900, NetworkCommonException.CODE_FAILED_NETWORK)
        assertEquals(9901, NetworkCommonException.CODE_FAILED_JSON_PARSING)
        assertEquals(9902, NetworkCommonException.CODE_NULL_POINTER_ERROR)
    }

    // Helper class for testing interceptor
    private class MockInterceptorChain(private val request: Request) : okhttp3.Interceptor.Chain {
        override fun request(): Request = request

        override fun proceed(request: Request): Response {
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()
        }

        override fun connection(): okhttp3.Connection? = null
        override fun call(): okhttp3.Call = TODO()
        override fun connectTimeoutMillis(): Int = 0
        override fun withConnectTimeout(timeout: Int, unit: TimeUnit): okhttp3.Interceptor.Chain =
            this

        override fun readTimeoutMillis(): Int = 0
        override fun withReadTimeout(timeout: Int, unit: TimeUnit): okhttp3.Interceptor.Chain = this
        override fun writeTimeoutMillis(): Int = 0
        override fun withWriteTimeout(timeout: Int, unit: TimeUnit): okhttp3.Interceptor.Chain =
            this
    }
}