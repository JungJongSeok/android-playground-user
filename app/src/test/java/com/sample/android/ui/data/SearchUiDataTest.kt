package com.sample.android.ui.data

import com.sample.android.data.UserMetaData
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test


class SearchUiDataTest {
    private val data1 = UserMetaData(
        thumbnail = "thumbnail1",
        title     = "title1",
        url       = "http://example.com/1",
        datetime  = "2025-05-18T12:00:00.000+09:00"
    )
    private val data2 = UserMetaData(
        thumbnail = "thumbnail2",
        title     = "title2",
        url       = "http://example.com/2",
        datetime  = "2025-05-18T13:00:00.000+09:00"
    )
    private val data3 = UserMetaData(
        thumbnail = "thumbnail3",
        title     = "title3",
        url       = "http://example.com/3",
        datetime  = "2025-05-18T14:00:00.000+09:00"
    )

    private val uiData1 = UserUiData(isFavorite = false, data = data1)
    private val uiData2 = UserUiData(isFavorite = false, data = data2)
    private val uiData3 = UserUiData(isFavorite = false, data = data3)

    @Test
    fun `addSearchUiData appends an item`() {
        val empty: List<UserUiData> = emptyList()
        val result = empty.addUiData(uiData1)

        assertEquals(1, result.size)
        assertEquals(uiData1, result[0])
    }

    @Test
    fun `removeSearchUiData removes only the matching item`() {
        val list: List<UserUiData> = listOf(uiData1, uiData3, uiData2)
        val result = list.removeUiData(uiData3)

        assertEquals(2, result.size)
        assertEquals(listOf(uiData1, uiData2), result)
    }

    @Test
    fun `removeSearchUiData does nothing if item not found`() {
        val list: List<UserUiData> = listOf(uiData1, uiData3)
        val other = uiData2
        val result = list.removeUiData(other)

        assertEquals(list, result)
    }

    @Test
    fun `like sets isFavorite=true for matching item only`() {
        val list: List<UserUiData> = listOf(uiData1, uiData3)
        val result = list.like(uiData1)

        val first = result[0]
        assertTrue(first.isFavorite)

        val second = result[1]
        assertFalse(second.isFavorite)
    }

    @Test
    fun `unlike sets isFavorite=false for matching item only`() {
        val likedData1 = uiData1.copy(isFavorite = true)
        val likedData3 = uiData3.copy(isFavorite = true)
        val list: List<UserUiData> = listOf(likedData1, likedData3)

        val result = list.unlike(likedData3)

        val first = result[0]
        assertTrue(first.isFavorite)
        val second = result[1]
        assertFalse(second.isFavorite)
    }

    @Test
    fun `like does nothing if item not found`() {
        val list: List<UserUiData> = listOf(uiData1)
        val other = uiData2
        val result = list.like(other)

        assertEquals(list, result)
    }

    @Test
    fun `unlike does nothing if item not found`() {
        val likedData = uiData1.copy(isFavorite = true)
        val list: List<UserUiData> = listOf(likedData)
        val other = uiData2.copy(isFavorite = true)
        val result = list.unlike(other)

        assertEquals(list, result)
    }
}