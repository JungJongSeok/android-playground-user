package com.sample.android.network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class NetworkModuleTest {
    data class Dummy(val foo: String)

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `call should parse successful JSON into Dummy`() = runBlocking {
        val body = """{"foo":"bar"}"""
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val endpoint = server.url("/test").toString()
        val result: Dummy = NetworkModule.call(NetworkModule.Method.GET, endpoint)

        assertEquals("bar", result.foo)
    }

    @Test
    fun `call should throw JSON parsing exception on invalid JSON`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("not a json"))

        val endpoint = server.url("/test").toString()
        try {
            NetworkModule.call<Dummy>(NetworkModule.Method.GET, endpoint)
            fail("Expected NetworkCommonException")
        } catch (e: NetworkCommonException) {
            assertEquals(NetworkCommonException.CODE_FAILED_JSON_PARSING, e.code)
            assertTrue(e.message!!.contains("Failed json parsing"))
        }
    }

    @Test
    fun `call should throw with error code and message from error JSON`() = runBlocking {
        val errJson = """{"message":"Error message"}"""
        server.enqueue(MockResponse().setResponseCode(400).setBody(errJson))

        val endpoint = server.url("/test").toString()
        try {
            NetworkModule.call<Dummy>(NetworkModule.Method.GET, endpoint)
            fail("Expected NetworkCommonException")
        } catch (e: NetworkCommonException) {
            assertEquals(400, e.code)
            assertEquals("Error message", e.message)
        }
    }

    @Test
    fun `call should throw with HTTP message on malformed error body`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(500).setBody("oops"))

        val endpoint = server.url("/test").toString()
        try {
            NetworkModule.call<Dummy>(NetworkModule.Method.GET, endpoint)
            fail("Expected NetworkCommonException")
        } catch (e: NetworkCommonException) {
            assertEquals(500, e.code)
            assertEquals("Server Error", e.message)
        }
    }
}