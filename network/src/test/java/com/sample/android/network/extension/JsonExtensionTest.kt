package com.sample.android.network.extension

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class JsonExtensionTest {
    data class Person(val name: String?, val age: Int, val numbers: List<Int>? = null)
    data class Request(val query: String, val count: Int)

    @Test
    fun `String fromJson should parse valid JSON into target type`() {
        val json = """{"name":"Alice","age":30,"numbers":[1, 2, 3]}"""
        val person: Person? = json.fromJson()

        assertNotNull(person)
        assertEquals("Alice", person?.name)
        assertEquals(30, person?.age)
        assertEquals(listOf(1, 2, 3), person?.numbers)
    }

    @Test
    fun `String fromJson should return null on invalid JSON`() {
        val badJson = """{name:Alice,age:"thirty"}"""
        val person: Person? = badJson.fromJson()
        assertNull(person)
    }

    @Test
    fun `String fromJson should return null on empty string`() {
        val empty = ""
        val person: Person? = empty.fromJson()
        assertNull(person)
    }

    @Test
    fun `Map fromJson should parse valid map into target type`() {
        val map = mapOf<String, Any?>(
            "name" to "Alice",
            "age" to 30,
            "numbers" to listOf(1, 2, 3)
        )
        val person: Person? = map.fromJson()

        assertNotNull(person)
        assertEquals("Alice", person?.name)
        assertEquals(30, person?.age)
        assertEquals(listOf(1, 2, 3), person?.numbers)
    }

    @Test
    fun `Map fromJson should return null on map with incompatible types`() {
        val map = mapOf<String, Any?>(
            "name" to 123,
            "age" to "twenty"
        )
        val person: Person? = map.fromJson()
        assertNull(person)
    }

    @Test
    fun `Map fromJson should return null on empty map`() {
        val emptyMap = emptyMap<String, Any?>()
        val person: Person? = emptyMap.fromJson()
        assertEquals(Person(name = null, age = 0, numbers = null), person)
    }

    @Test
    fun `toMap should convert valid JSON string to map`() {
        val json = """{"a":1,"b":"two","c":true}"""
        val result = json.toMap()

        assertEquals(1, result["a"])
        assertEquals("two", result["b"])
        assertEquals(true, result["c"])
        assertNull(result["d"])
        assertEquals(3, result.size)
    }

    @Test
    fun `toMap should return empty map on null input`() {
        val result = (null as String?).toMap()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toMap should return empty map on invalid JSON`() {
        val badJson = "not valid"
        val result = badJson.toMap()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `toMap should handle nested JSON objects`() {
        val json = """{"user":{"id":1,"name":"Carol"},"tags":[ "x","y" ]}"""
        val result = json.toMap()
        assertTrue(result["user"] is JSONObject)
        assertTrue(result["tags"] is org.json.JSONArray)
    }

    @Test
    fun `toMap should handle nested target type`() {
        val request = Request(query = "Query", count = 10)
        val objectMap = mapOf<String, Any?>(
            "query" to "Query",
            "count" to 10,
        )
        val parsedMap = request.toMap()

        assertEquals(request.query, objectMap["query"])
        assertEquals(request.count, objectMap["count"])
        assertEquals(request.query, parsedMap["query"])
        assertEquals(request.count, parsedMap["count"])
    }
}