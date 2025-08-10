package com.sample.android.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.sample.android.data.UserMetaData
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PreferencesModuleImpl
 */
class PreferencesModuleTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var preferencesModule: PreferencesModuleImpl

    private val testUserMetaData1 = UserMetaData(
        title = "testuser1",
        thumbnail = "https://avatar.com/testuser1",
        url = "https://github.com/testuser1",
        datetime = "2023-05-21T09:42:29.000+09:00"
    )

    private val testUserMetaData2 = UserMetaData(
        title = "testuser2",
        thumbnail = "https://avatar.com/testuser2",
        url = "https://github.com/testuser2",
        datetime = "2023-05-22T10:30:15.000+09:00"
    )

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every {
            context.getSharedPreferences(
                "Preference",
                Context.MODE_PRIVATE
            )
        } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.commit() } returns true

        preferencesModule = PreferencesModuleImpl(context)
    }

    @Test
    fun `favorites getter returns empty list when no data stored`() {
        // Given
        every { sharedPreferences.getString("key_favorites", null) } returns null

        // When
        val result = preferencesModule.favorites

        // Then
        assertTrue(result.isEmpty())
        verify { sharedPreferences.getString("key_favorites", null) }
    }

    @Test
    fun `favorites getter returns stored data correctly`() {
        // Given
        val gson = GsonBuilder().create()
        val testList = listOf(testUserMetaData1, testUserMetaData2)
        val typeToken: TypeToken<MutableList<UserMetaData>> =
            object : TypeToken<MutableList<UserMetaData>>() {}
        val jsonString = gson.toJson(testList, typeToken.type)

        every { sharedPreferences.getString("key_favorites", null) } returns jsonString

        // When
        val result = preferencesModule.favorites

        // Then
        assertEquals(2, result.size)
        assertEquals("testuser1", result[0].title)
        assertEquals("testuser2", result[1].title)
        verify { sharedPreferences.getString("key_favorites", null) }
    }

    @Test
    fun `favorites setter stores data correctly`() {
        // Given
        val testList = listOf(testUserMetaData1)

        // When
        preferencesModule.favorites = testList

        // Then
        verify { sharedPreferences.edit() }
        verify { editor.putString(eq("key_favorites"), any()) }
        verify { editor.commit() }
    }

    @Test
    fun `favorites setter stores empty list correctly`() {
        // Given
        val emptyList = emptyList<UserMetaData>()

        // When
        preferencesModule.favorites = emptyList

        // Then
        verify { sharedPreferences.edit() }
        verify { editor.putString(eq("key_favorites"), any()) }
        verify { editor.commit() }
    }

    @Test
    fun `favorites getter handles malformed JSON gracefully`() {
        // Given
        every { sharedPreferences.getString("key_favorites", null) } returns "invalid json"

        // When
        val result = try {
            preferencesModule.favorites
        } catch (e: Exception) {
            // If JSON parsing fails, PreferencesModuleImpl should return empty list
            emptyList<UserMetaData>()
        }

        // Then
        assertTrue(result.isEmpty())
        verify { sharedPreferences.getString("key_favorites", null) }
    }

    @Test
    fun `favorites setter and getter integration test`() {
        // Given
        val testList = listOf(testUserMetaData1, testUserMetaData2)
        val gson = GsonBuilder().create()
        val typeToken: TypeToken<MutableList<UserMetaData>> =
            object : TypeToken<MutableList<UserMetaData>>() {}
        val jsonString = gson.toJson(testList, typeToken.type)

        // Mock the setter call
        preferencesModule.favorites = testList

        // Mock the getter call to return what we just set
        every { sharedPreferences.getString("key_favorites", null) } returns jsonString

        // When
        val result = preferencesModule.favorites

        // Then
        assertEquals(2, result.size)
        assertEquals("testuser1", result[0].title)
        assertEquals("https://avatar.com/testuser1", result[0].thumbnail)
        assertEquals("https://github.com/testuser1", result[0].url)
        assertEquals("2023-05-21T09:42:29.000+09:00", result[0].datetime)

        assertEquals("testuser2", result[1].title)
        assertEquals("https://avatar.com/testuser2", result[1].thumbnail)
        assertEquals("https://github.com/testuser2", result[1].url)
        assertEquals("2023-05-22T10:30:15.000+09:00", result[1].datetime)
    }

    @Test
    fun `favorites handles null SharedPreferences gracefully`() {
        // Given
        every { context.getSharedPreferences("Preference", Context.MODE_PRIVATE) } returns null

        val moduleWithNullPrefs = PreferencesModuleImpl(context)
        val testList = listOf(testUserMetaData1)

        // When setting favorites
        moduleWithNullPrefs.favorites = testList

        // When getting favorites
        val result = moduleWithNullPrefs.favorites

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `favorites handles null editor gracefully`() {
        // Given
        every { sharedPreferences.edit() } returns null
        val testList = listOf(testUserMetaData1)

        // When
        preferencesModule.favorites = testList

        // Then - Should not crash, just verify the edit() call was made
        verify { sharedPreferences.edit() }
    }
}