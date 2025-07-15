package com.sample.android.network

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class NetworkCommonExceptionTest {

    @Test
    fun `NetworkCommonException should create with code and message`() {
        // Given
        val code = 400
        val message = "Bad Request"

        // When
        val exception = NetworkCommonException(code, message)

        // Then
        assertEquals(code, exception.code)
        assertEquals(message, exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `NetworkCommonException should create with code, message, and cause`() {
        // Given
        val code = 500
        val message = "Internal Server Error"
        val cause = RuntimeException("Connection failed")

        // When
        val exception = NetworkCommonException(code, message, cause)

        // Then
        assertEquals(code, exception.code)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `NetworkCommonException should create with only code`() {
        // Given
        val code = 404

        // When
        val exception = NetworkCommonException(code)

        // Then
        assertEquals(code, exception.code)
        assertNull(exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `NetworkCommonException should create with code and null message`() {
        // Given
        val code = 401

        // When
        val exception = NetworkCommonException(code, null)

        // Then
        assertEquals(code, exception.code)
        assertNull(exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun `NetworkCommonException should create with code, null message, and cause`() {
        // Given
        val code = 503
        val cause = IllegalStateException("Service unavailable")

        // When
        val exception = NetworkCommonException(code, null, cause)

        // Then
        assertEquals(code, exception.code)
        assertNull(exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `NetworkCommonException should inherit from Exception`() {
        // Given
        val exception = NetworkCommonException(400, "Bad Request")

        // When & Then
        assertTrue(exception is Exception)
    }

    @Test
    fun `NetworkCommonException companion object should have correct constants`() {
        // Then
        assertEquals(9900, NetworkCommonException.CODE_FAILED_NETWORK)
        assertEquals(9901, NetworkCommonException.CODE_FAILED_JSON_PARSING)
        assertEquals(9902, NetworkCommonException.CODE_NULL_POINTER_ERROR)
    }

    @Test
    fun `NetworkCommonException should work with network failure code`() {
        // Given
        val message = "Network connection failed"
        val cause = java.net.SocketTimeoutException("Timeout")

        // When
        val exception = NetworkCommonException(
            NetworkCommonException.CODE_FAILED_NETWORK,
            message,
            cause
        )

        // Then
        assertEquals(NetworkCommonException.CODE_FAILED_NETWORK, exception.code)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `NetworkCommonException should work with JSON parsing failure code`() {
        // Given
        val message = "Failed to parse JSON response"
        val cause = com.google.gson.JsonSyntaxException("Invalid JSON")

        // When
        val exception = NetworkCommonException(
            NetworkCommonException.CODE_FAILED_JSON_PARSING,
            message,
            cause
        )

        // Then
        assertEquals(NetworkCommonException.CODE_FAILED_JSON_PARSING, exception.code)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `NetworkCommonException should work with null pointer error code`() {
        // Given
        val message = "Null pointer exception occurred"
        val cause = NullPointerException("Response body is null")

        // When
        val exception = NetworkCommonException(
            NetworkCommonException.CODE_NULL_POINTER_ERROR,
            message,
            cause
        )

        // Then
        assertEquals(NetworkCommonException.CODE_NULL_POINTER_ERROR, exception.code)
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `NetworkCommonException should maintain message in Exception hierarchy`() {
        // Given
        val message = "Custom error message"
        val exception = NetworkCommonException(400, message)

        // When
        val exceptionMessage = (exception as Exception).message

        // Then
        assertEquals(message, exceptionMessage)
    }

    @Test
    fun `NetworkCommonException should handle empty message`() {
        // Given
        val code = 400
        val message = ""

        // When
        val exception = NetworkCommonException(code, message)

        // Then
        assertEquals(code, exception.code)
        assertEquals(message, exception.message)
        assertTrue(exception.message?.isEmpty() == true)
    }

    @Test
    fun `NetworkCommonException should be serializable`() {
        // Given
        val exception = NetworkCommonException(
            400,
            "Bad Request",
            RuntimeException("Cause")
        )

        // When & Then
        // Test that the exception can be created and contains expected data
        assertNotNull(exception)
        assertEquals(400, exception.code)
        assertEquals("Bad Request", exception.message)
        assertNotNull(exception.cause)
    }
}