package com.sample.android.ui.feature.main.model

import com.sample.android.R
import org.junit.Assert.assertEquals
import org.junit.Test

class MainTabTest {

    @Test
    fun `MainTab SEARCH should have correct index and title resource`() {
        // Given & When
        val tab = MainTab.SEARCH

        // Then
        assertEquals(0, tab.index)
        assertEquals(R.string.main_tab_search, tab.titleRes)
    }

    @Test
    fun `MainTab FAVORITE should have correct index and title resource`() {
        // Given & When
        val tab = MainTab.FAVORITE

        // Then
        assertEquals(1, tab.index)
        assertEquals(R.string.main_tab_favorite, tab.titleRes)
    }

    @Test
    fun `MainTab enum should have correct number of values`() {
        // Given & When
        val values = MainTab.values()

        // Then
        assertEquals(2, values.size)
    }

    @Test
    fun `MainTab enum values should be in correct order`() {
        // Given & When
        val values = MainTab.values()

        // Then
        assertEquals(MainTab.SEARCH, values[0])
        assertEquals(MainTab.FAVORITE, values[1])
    }

    @Test
    fun `MainTab valueOf should return correct enum value`() {
        // Given & When & Then
        assertEquals(MainTab.SEARCH, MainTab.valueOf("SEARCH"))
        assertEquals(MainTab.FAVORITE, MainTab.valueOf("FAVORITE"))
    }

    @Test
    fun `MainTab index should match array position`() {
        // Given
        val values = MainTab.values()

        // When & Then
        values.forEachIndexed { index, tab ->
            assertEquals(index, tab.index)
        }
    }
}