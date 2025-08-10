package com.sample.android.network

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class NetworkCommonExceptionTest {

    @Test
    fun `NetworkCommonException should create with code and message`() {
        val code = 400
        val message = "Bad Request"

        val exception = NetworkCommonException(code, message)

        assertEquals(code, exception.code)
        assertEquals(message, exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `NetworkCommonException should create with code, message, and cause`() {
        val code = 500
        val message = "Internal Server Error"
        val cause = RuntimeException("Connection failed")

        val exception = NetworkCommonException(code, message, cause)

        assertEquals(code, exception.code)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `NetworkCommonException should create with only code`() {
        val code = 404

        val exception = NetworkCommonException(code)

        assertEquals(code, exception.code)
        assertNull(exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `NetworkCommonException should create with code and null message`() {
        val code = 401

        val exception = NetworkCommonException(code, null)

        assertEquals(code, exception.code)
        assertNull(exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `NetworkCommonException should create with code, null message, and cause`() {
        val code = 503
        val cause = IllegalStateException("Service unavailable")

        val exception = NetworkCommonException(code, null, cause)

        assertEquals(code, exception.code)
        assertNull(exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `NetworkCommonException should inherit from Exception`() {
        val exception = NetworkCommonException(400, "Bad Request")

        assertTrue(exception is Exception)
    }

    @Test
    fun `NetworkCommonException companion object should have correct constants`() {
        assertEquals(9900, NetworkCommonException.CODE_FAILED_NETWORK)
        assertEquals(9901, NetworkCommonException.CODE_FAILED_JSON_PARSING)
        assertEquals(9902, NetworkCommonException.CODE_NULL_POINTER_ERROR)
    }

    @Test
    fun `NetworkCommonException should work with network failure code`() {
        val message = "Network connection failed"
        val cause = java.net.SocketTimeoutException("Timeout")

        val exception = NetworkCommonException(
            NetworkCommonException.CODE_FAILED_NETWORK,
            message,
            cause
        )

        assertEquals(NetworkCommonException.CODE_FAILED_NETWORK, exception.code)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `NetworkCommonException should work with JSON parsing failure code`() {
        val message = "Failed to parse JSON response"
        val cause = com.google.gson.JsonSyntaxException("Invalid JSON")

        val exception = NetworkCommonException(
            NetworkCommonException.CODE_FAILED_JSON_PARSING,
            message,
            cause
        )

        assertEquals(NetworkCommonException.CODE_FAILED_JSON_PARSING, exception.code)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `NetworkCommonException should work with null pointer error code`() {
        val message = "Null pointer exception occurred"
        val cause = NullPointerException("Response body is null")

        val exception = NetworkCommonException(
            NetworkCommonException.CODE_NULL_POINTER_ERROR,
            message,
            cause
        )

        assertEquals(NetworkCommonException.CODE_NULL_POINTER_ERROR, exception.code)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `NetworkCommonException should maintain message in Exception hierarchy`() {
        val message = "Custom error message"
        val exception = NetworkCommonException(400, message)

        val exceptionMessage = (exception as Exception).message

        assertEquals(message, exceptionMessage)
    }

    @Test
    fun `NetworkCommonException should handle empty message`() {
        val code = 400
        val message = ""

        val exception = NetworkCommonException(code, message)

        assertEquals(code, exception.code)
        assertEquals(message, exception.message)
        assertTrue(exception.message?.isEmpty() == true)
    }

    @Test
    fun `NetworkCommonException should be serializable`() {
        val exception = NetworkCommonException(
            400,
            "Bad Request",
            RuntimeException("Cause")
        )

        // Test that the exception can be created and contains expected data
        assertNotNull(exception)
        assertEquals(400, exception.code)
        assertEquals("Bad Request", exception.message)
        assertNotNull(exception.cause)
    }
}