package com.sample.android.network.request


import com.google.gson.GsonBuilder
import junit.framework.TestCase.assertEquals
import org.junit.Test

class UserRequestTest {

    private val gson = GsonBuilder()
        .serializeNulls()
        .create()

    @Test
    fun `Gson serializes all properties with correct JSON field names`() {
        val request = UserRequest(
            seed = "kotlin",
            page = 5,
            results = 20
        )

        val jsonObj = gson.toJsonTree(request).asJsonObject

        assertEquals("kotlin", jsonObj["seed"].asString)
        assertEquals(5, jsonObj["page"].asInt)
        assertEquals(20, jsonObj["results"].asInt)
    }

    @Test
    fun `Gson applies default values when sort and size are omitted`() {
        val request = UserRequest(
            seed = "search",
            page = 1,
        )

        val jsonObj = gson.toJsonTree(request).asJsonObject

        assertEquals("search", jsonObj["seed"].asString)
        assertEquals(1, jsonObj["page"].asInt)
        assertEquals(10, jsonObj["results"].asInt)
    }
}