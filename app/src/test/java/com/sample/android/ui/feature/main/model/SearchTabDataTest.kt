package com.sample.android.ui.feature.main.model

import com.sample.android.data.UserMetaData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTabDataTest {

    private val testUserData1 = UserMetaData(
        title = "Test User 1",
        thumbnail = "https://example.com/image1.jpg",
        url = "test1@example.com",
        datetime = "2023-01-01T12:00:00.000Z"
    )

    private val testUserData2 = UserMetaData(
        title = "Test User 2",
        thumbnail = "https://example.com/image2.jpg",
        url = "test2@example.com",
        datetime = "2023-01-02T12:00:00.000Z"
    )

    private val sampleUserData = UserMetaData(
        title = "John Doe",
        thumbnail = "https://example.com/thumb.jpg",
        url = "john@example.com",
        datetime = "2023-01-01T12:00:00.000Z"
    )

    private val sampleUserUiData = UserUiData(
        isFavorite = false,
        data = sampleUserData
    )

    @Test
    fun `like should change favorite status to true for matching SearchTabMetaData`() {
        // Given
        val userUiData1 = UserUiData(isFavorite = false, data = testUserData1)
        val userUiData2 = UserUiData(isFavorite = false, data = testUserData2)
        val searchTabData1 = SearchTabMetaData(userUiData1)
        val searchTabData2 = SearchTabMetaData(userUiData2)
        val border = SearchTabBorder("1", false)
        val list = listOf<SearchTabData>(searchTabData1, searchTabData2, border)

        // When
        val result = list.like(userUiData1)

        // Then
        assertEquals(3, result.size)

        val updatedSearchTab1 = result[0] as SearchTabMetaData
        val updatedSearchTab2 = result[1] as SearchTabMetaData
        val updatedBorder = result[2] as SearchTabBorder

        assertTrue(updatedSearchTab1.data.isFavorite)
        assertFalse(updatedSearchTab2.data.isFavorite)
        assertEquals(testUserData1, updatedSearchTab1.data.data)
        assertEquals(testUserData2, updatedSearchTab2.data.data)
        assertEquals(border, updatedBorder)
    }

    @Test
    fun `unlike should change favorite status to false for matching SearchTabMetaData`() {
        // Given
        val userUiData1 = UserUiData(isFavorite = true, data = testUserData1)
        val userUiData2 = UserUiData(isFavorite = true, data = testUserData2)
        val searchTabData1 = SearchTabMetaData(userUiData1)
        val searchTabData2 = SearchTabMetaData(userUiData2)
        val border = SearchTabBorder("1", false)
        val list = listOf<SearchTabData>(searchTabData1, searchTabData2, border)

        // When
        val result = list.unlike(userUiData1)

        // Then
        assertEquals(3, result.size)

        val updatedSearchTab1 = result[0] as SearchTabMetaData
        val updatedSearchTab2 = result[1] as SearchTabMetaData
        val updatedBorder = result[2] as SearchTabBorder

        assertFalse(updatedSearchTab1.data.isFavorite)
        assertTrue(updatedSearchTab2.data.isFavorite)
        assertEquals(testUserData1, updatedSearchTab1.data.data)
        assertEquals(testUserData2, updatedSearchTab2.data.data)
        assertEquals(border, updatedBorder)
    }

    @Test
    fun `like should not change non-matching SearchTabMetaData`() {
        // Given
        val userUiData1 = UserUiData(isFavorite = false, data = testUserData1)
        val userUiData2 = UserUiData(isFavorite = false, data = testUserData2)
        val nonMatchingUserUiData =
            UserUiData(isFavorite = false, data = testUserData1.copy(title = "Non-matching"))
        val searchTabData1 = SearchTabMetaData(userUiData1)
        val searchTabData2 = SearchTabMetaData(userUiData2)
        val list = listOf<SearchTabData>(searchTabData1, searchTabData2)

        // When
        val result = list.like(nonMatchingUserUiData)

        // Then
        assertEquals(2, result.size)

        val updatedSearchTab1 = result[0] as SearchTabMetaData
        val updatedSearchTab2 = result[1] as SearchTabMetaData

        assertFalse(updatedSearchTab1.data.isFavorite)
        assertFalse(updatedSearchTab2.data.isFavorite)
    }

    @Test
    fun `unlike should not change non-matching SearchTabMetaData`() {
        // Given
        val userUiData1 = UserUiData(isFavorite = true, data = testUserData1)
        val userUiData2 = UserUiData(isFavorite = true, data = testUserData2)
        val nonMatchingUserUiData =
            UserUiData(isFavorite = true, data = testUserData1.copy(title = "Non-matching"))
        val searchTabData1 = SearchTabMetaData(userUiData1)
        val searchTabData2 = SearchTabMetaData(userUiData2)
        val list = listOf<SearchTabData>(searchTabData1, searchTabData2)

        // When
        val result = list.unlike(nonMatchingUserUiData)

        // Then
        assertEquals(2, result.size)

        val updatedSearchTab1 = result[0] as SearchTabMetaData
        val updatedSearchTab2 = result[1] as SearchTabMetaData

        assertTrue(updatedSearchTab1.data.isFavorite)
        assertTrue(updatedSearchTab2.data.isFavorite)
    }

    @Test
    fun `operations should preserve SearchTabBorder items unchanged`() {
        // Given
        val userUiData = UserUiData(isFavorite = false, data = testUserData1)
        val searchTabData = SearchTabMetaData(userUiData)
        val border1 = SearchTabBorder("1", false)
        val border2 = SearchTabBorder("End", true)
        val list = listOf<SearchTabData>(searchTabData, border1, border2)

        // When
        val likeResult = list.like(userUiData)
        val unlikeResult = list.unlike(userUiData)

        // Then
        assertEquals(3, likeResult.size)
        assertEquals(3, unlikeResult.size)

        assertTrue(likeResult[1] is SearchTabBorder)
        assertTrue(likeResult[2] is SearchTabBorder)
        assertTrue(unlikeResult[1] is SearchTabBorder)
        assertTrue(unlikeResult[2] is SearchTabBorder)

        assertEquals(border1, likeResult[1])
        assertEquals(border2, likeResult[2])
        assertEquals(border1, unlikeResult[1])
        assertEquals(border2, unlikeResult[2])
    }

    @Test
    fun `operations on empty list should return empty list`() {
        // Given
        val emptyList = emptyList<SearchTabData>()
        val userUiData = UserUiData(isFavorite = false, data = testUserData1)

        // When & Then
        assertEquals(0, emptyList.like(userUiData).size)
        assertEquals(0, emptyList.unlike(userUiData).size)
    }

    @Test
    fun `operations on list with only borders should return unchanged list`() {
        // Given
        val border1 = SearchTabBorder("1", false)
        val border2 = SearchTabBorder("End", true)
        val list = listOf<SearchTabData>(border1, border2)
        val userUiData = UserUiData(isFavorite = false, data = testUserData1)

        // When
        val likeResult = list.like(userUiData)
        val unlikeResult = list.unlike(userUiData)

        // Then
        assertEquals(2, likeResult.size)
        assertEquals(2, unlikeResult.size)
        assertEquals(border1, likeResult[0])
        assertEquals(border2, likeResult[1])
        assertEquals(border1, unlikeResult[0])
        assertEquals(border2, unlikeResult[1])
    }

    @Test
    fun `like should update favorite status of matching SearchTabMetaData`() {
        val searchTabList = listOf(
            SearchTabMetaData(sampleUserUiData.copy(isFavorite = false)),
            SearchTabBorder("Page 1", false),
            SearchTabMetaData(
                sampleUserUiData.copy(
                    isFavorite = false,
                    data = sampleUserData.copy(title = "Jane Doe")
                )
            )
        )

        val result = searchTabList.like(sampleUserUiData)

        assertTrue((result[0] as SearchTabMetaData).data.isFavorite)
        assertTrue(result[1] is SearchTabBorder)
        assertFalse((result[2] as SearchTabMetaData).data.isFavorite)
    }

    @Test
    fun `unlike should update favorite status of matching SearchTabMetaData`() {
        val searchTabList = listOf(
            SearchTabMetaData(sampleUserUiData.copy(isFavorite = true)),
            SearchTabBorder("Page 1", false),
            SearchTabMetaData(
                sampleUserUiData.copy(
                    isFavorite = true,
                    data = sampleUserData.copy(title = "Jane Doe")
                )
            )
        )

        val result = searchTabList.unlike(sampleUserUiData)

        assertFalse((result[0] as SearchTabMetaData).data.isFavorite)
        assertTrue(result[1] is SearchTabBorder)
        assertTrue((result[2] as SearchTabMetaData).data.isFavorite)
    }

    @Test
    fun `changeFavoriteStatus should not affect SearchTabBorder items`() {
        val borderItem = SearchTabBorder("End of results", true)
        val searchTabList = listOf(borderItem)

        val result = searchTabList.like(sampleUserUiData)

        assertEquals(1, result.size)
        assertTrue(result[0] is SearchTabBorder)
        assertEquals("End of results", (result[0] as SearchTabBorder).text)
        assertTrue((result[0] as SearchTabBorder).isEnd)
    }

    @Test
    fun `changeFavoriteStatus should only affect exact matching UserUiData`() {
        val user1 = sampleUserUiData.copy(isFavorite = false)
        val user2 = sampleUserUiData.copy(
            isFavorite = false,
            data = sampleUserData.copy(title = "Different User")
        )
        val searchTabList = listOf(
            SearchTabMetaData(user1),
            SearchTabMetaData(user2)
        )

        val result = searchTabList.like(user1)

        assertTrue((result[0] as SearchTabMetaData).data.isFavorite)
        assertFalse((result[1] as SearchTabMetaData).data.isFavorite)
    }

    @Test
    fun `SearchTabBorder should maintain its properties`() {
        val border = SearchTabBorder("Test text", true)

        assertEquals("Test text", border.text)
        assertTrue(border.isEnd)
    }

    @Test
    fun `SearchTabMetaData should maintain its properties`() {
        val metaData = SearchTabMetaData(sampleUserUiData)

        assertEquals(sampleUserUiData, metaData.data)
    }
}