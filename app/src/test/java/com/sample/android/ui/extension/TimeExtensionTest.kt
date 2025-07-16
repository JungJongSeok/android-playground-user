package com.sample.android.ui.extension

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeExtensionTest {

    private val mockContext: Context = mockk()

    @Test
    fun `setTimeText should return formatted time for valid timestamp`() {
        // Given
        val timestamp = 1640995200000L // 2022-01-01 00:00:00 UTC
        val resourceId = android.R.string.ok
        val formatString = "yyyy-MM-dd HH:mm:ss"

        every { mockContext.getString(resourceId) } returns formatString

        // When
        val result = timestamp.setTimeText(mockContext, resourceId)

        // Then - The exact result depends on the device timezone, but it should not be empty
        assert(result.isNotEmpty())
        assert(result.contains("2022") || result.contains("2021")) // Could be different timezone
    }

    @Test
    fun `setTimeText should return empty string for zero timestamp`() {
        // Given
        val timestamp = 0L
        val resourceId = android.R.string.ok
        val formatString = "yyyy-MM-dd HH:mm:ss"

        every { mockContext.getString(resourceId) } returns formatString

        // When
        val result = timestamp.setTimeText(mockContext, resourceId)

        // Then
        assertEquals("", result)
    }

    @Test
    fun `setTimeText should return empty string for negative timestamp`() {
        // Given
        val timestamp = -1L
        val resourceId = android.R.string.ok
        val formatString = "yyyy-MM-dd HH:mm:ss"

        every { mockContext.getString(resourceId) } returns formatString

        // When
        val result = timestamp.setTimeText(mockContext, resourceId)

        // Then
        assertEquals("", result)
    }

    @Test
    fun `setTimeText should use correct format pattern`() {
        // Given
        val timestamp = 1640995200000L // 2022-01-01 00:00:00 UTC
        val resourceId = android.R.string.ok
        val formatString = "yyyy"

        every { mockContext.getString(resourceId) } returns formatString

        // When
        val result = timestamp.setTimeText(mockContext, resourceId)

        // Then
        assert(result == "2022" || result == "2021") // Could be different timezone
    }

    @Test
    fun `setTimeText should handle different format patterns`() {
        // Given
        val timestamp = 1640995200000L // 2022-01-01 00:00:00 UTC
        val resourceId = android.R.string.ok
        val formatString = "MM/dd/yyyy"

        every { mockContext.getString(resourceId) } returns formatString

        // When
        val result = timestamp.setTimeText(mockContext, resourceId)

        // Then
        assert(result.isNotEmpty())
        assert(result.contains("/"))
        assert(result.contains("01") || result.contains("12")) // Could be different timezone
    }
}