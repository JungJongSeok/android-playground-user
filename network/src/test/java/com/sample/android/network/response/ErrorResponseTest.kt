package com.sample.android.network.response

import com.google.gson.GsonBuilder
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class ErrorResponseTest {

    private val gson = GsonBuilder()
        .serializeNulls()
        .create()

    @Test
    fun `Gson deserializes all properties with correct JSON field names`() {
        val jsonString = """
            {
                "errorType": "VALIDATION_ERROR",
                "message": "Invalid request parameters"
            }
        """

        val errorResponse = gson.fromJson(jsonString, ErrorResponse::class.java)

        assertEquals("VALIDATION_ERROR", errorResponse.errorType)
        assertEquals("Invalid request parameters", errorResponse.message)
    }

    @Test
    fun `Gson deserializes with null values`() {
        val jsonString = """
            {
                "errorType": null,
                "message": null
            }
        """

        val errorResponse = gson.fromJson(jsonString, ErrorResponse::class.java)

        assertNull(errorResponse.errorType)
        assertNull(errorResponse.message)
    }

    @Test
    fun `Gson deserializes with missing fields`() {
        val jsonString = "{}"

        val errorResponse = gson.fromJson(jsonString, ErrorResponse::class.java)

        assertNull(errorResponse.errorType)
        assertNull(errorResponse.message)
    }

    @Test
    fun `Gson serializes all properties with correct JSON field names`() {
        val errorResponse = ErrorResponse(
            errorType = "NETWORK_ERROR",
            message = "Connection timeout"
        )

        val jsonObj = gson.toJsonTree(errorResponse).asJsonObject

        assertEquals("NETWORK_ERROR", jsonObj["errorType"].asString)
        assertEquals("Connection timeout", jsonObj["message"].asString)
    }

    @Test
    fun `Gson serializes with null values`() {
        val errorResponse = ErrorResponse(
            errorType = null,
            message = null
        )

        val jsonObj = gson.toJsonTree(errorResponse).asJsonObject

        assertEquals(true, jsonObj["errorType"].isJsonNull)
        assertEquals(true, jsonObj["message"].isJsonNull)
    }
}