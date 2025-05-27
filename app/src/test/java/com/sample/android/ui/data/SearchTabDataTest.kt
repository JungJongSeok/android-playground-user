package com.sample.android.ui.data

import com.sample.android.data.UserMetaData
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SearchTabDataTest {
    private val data1 = UserMetaData(
        thumbnail = "thumbnail1",
        title = "title1",
        url = "url1",
        datetime = "2025-05-19T09:42:29.000+09:00"
    )
    private val data2 = UserMetaData(
        thumbnail = "thumbnail2",
        title = "title2",
        url = "url2",
        datetime = "2025-05-19T09:42:29.000+09:00"
    )

    private val uiData1 = UserUiData(
        isFavorite = false,
        data = data1
    )
    private val uiData2 = UserUiData(
        isFavorite = false,
        data = data2
    )
    private val uiData1True = uiData1.copy(isFavorite = true)
    private val uiData2True = uiData2.copy(isFavorite = true)

    private val searchTabData1 = SearchTabMetaData(data = uiData1)
    private val searchTabData2 = SearchTabMetaData(data = uiData2)
    private val border = SearchTabBorder(text = "1", isEnd = false)

    private val baseList: List<SearchTabData> = listOf(searchTabData1, border, searchTabData2)

    @Test
    fun `like sets only the matching meta data to isFavorite = true`() {
        val result = baseList.like(uiData1)

        val first = result[0] as SearchTabMetaData
        assertTrue(first.data.isFavorite)

        val middle = result[1] as SearchTabBorder
        assertEquals(border, middle)

        val third = result[2] as SearchTabMetaData
        assertFalse(third.data.isFavorite)
    }

    @Test
    fun `unlike sets only the matching meta data to isFavorite = false`() {
        val likedList = listOf(
            SearchTabMetaData(data = uiData1True),
            border,
            SearchTabMetaData(data = uiData2True)
        )

        val result = likedList.unlike(uiData1True)

        val first = result[0] as SearchTabMetaData
        assertFalse(first.data.isFavorite)

        val third = result[2] as SearchTabMetaData
        assertTrue(third.data.isFavorite)
    }

    @Test
    fun `like does nothing if searchUiData not found`() {
        val unknownUi = uiData1.copy(data = data1.copy("unknown"))
        val result = baseList.like(unknownUi)
        assertEquals(baseList, result)
    }

    @Test
    fun `unlike does nothing if searchUiData not found`() {
        val unknownUi = uiData2.copy(data = data2.copy("unknown"))
        val result = baseList.unlike(unknownUi)
        assertEquals(baseList, result)
    }
}