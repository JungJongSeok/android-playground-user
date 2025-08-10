package com.sample.android.utils


import android.content.Context
import android.content.SharedPreferences
import com.sample.android.data.UserMetaData
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class PreferencesModuleTest {

    companion object {
        private const val PREF_NAME = "Preference"
        private const val KEY_FAVORITES = "key_favorites"
    }

    @MockK(relaxed = true)
    lateinit var context: Context
    @MockK(relaxed = true)
    lateinit var sharedPrefs: SharedPreferences
    @MockK(relaxed = true)
    lateinit var editor: SharedPreferences.Editor

    private lateinit var prefs: PreferencesModuleImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.commit() } returns true

        prefs = PreferencesModuleImpl(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `favorites setter writes JSON and getter reads it back correctly`() {
        val data1 = UserMetaData(
            thumbnail = "thumbnail",
            title     = "title",
            url       = "http://example.com/1",
            datetime  = "2025-05-16T09:42:29.000+09:00"
        )
        val data2 = UserMetaData(
            thumbnail = "thumbnail",
            title     = "title",
            url       = "http://example.com/2",
            datetime  = "2025-05-17T09:42:29.000+09:00"
        )
        val listToSave = listOf(data1, data2)

        val jsonSlot = slot<String>()
        every { editor.putString(KEY_FAVORITES, capture(jsonSlot)) } returns editor

        prefs.favorites = listToSave

        verify(exactly = 1) { editor.putString(KEY_FAVORITES, any()) }
        verify(exactly = 1) { editor.commit() }

        every { sharedPrefs.getString(KEY_FAVORITES, null) } returns jsonSlot.captured

        val result = prefs.favorites

        assertEquals(2, result.size)
        assertTrue(result[0] == data1)
        assertTrue(result[1] == data2)
    }
}