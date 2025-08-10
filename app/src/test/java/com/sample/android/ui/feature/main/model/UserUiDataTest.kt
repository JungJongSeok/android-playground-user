package com.sample.android.ui.feature.main.model

import com.sample.android.data.UserMetaData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserUiDataTest {

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

    @Test
    fun `removeUiData should remove matching item from list`() {
        val userUiData1 = UserUiData(isFavorite = false, data = testUserData1)
        val userUiData2 = UserUiData(isFavorite = true, data = testUserData2)
        val list = listOf(userUiData1, userUiData2)

        val result = list.removeUiData(userUiData1)

        assertEquals(1, result.size)
        assertEquals(userUiData2, result[0])
    }

    @Test
    fun `removeUiData should return original list if item not found`() {
        val userUiData1 = UserUiData(isFavorite = false, data = testUserData1)
        val userUiData2 = UserUiData(isFavorite = true, data = testUserData2)
        val nonExistentItem =
            UserUiData(isFavorite = false, data = testUserData1.copy(title = "Non-existent"))
        val list = listOf(userUiData1, userUiData2)

        val result = list.removeUiData(nonExistentItem)

        assertEquals(2, result.size)
        assertEquals(userUiData1, result[0])
        assertEquals(userUiData2, result[1])
    }

    @Test
    fun `addUiData should append item to list`() {
        val userUiData1 = UserUiData(isFavorite = false, data = testUserData1)
        val userUiData2 = UserUiData(isFavorite = true, data = testUserData2)
        val list = listOf(userUiData1)

        val result = list.addUiData(userUiData2)

        assertEquals(2, result.size)
        assertEquals(userUiData1, result[0])
        assertEquals(userUiData2, result[1])
    }

    @Test
    fun `like should change favorite status to true for matching item`() {
        val userUiData1 = UserUiData(isFavorite = false, data = testUserData1)
        val userUiData2 = UserUiData(isFavorite = false, data = testUserData2)
        val list = listOf(userUiData1, userUiData2)

        val result = list.like(userUiData1)

        assertEquals(2, result.size)
        assertTrue(result[0].isFavorite)
        assertFalse(result[1].isFavorite)
        assertEquals(testUserData1, result[0].data)
        assertEquals(testUserData2, result[1].data)
    }

    @Test
    fun `unlike should change favorite status to false for matching item`() {
        val userUiData1 = UserUiData(isFavorite = true, data = testUserData1)
        val userUiData2 = UserUiData(isFavorite = true, data = testUserData2)
        val list = listOf(userUiData1, userUiData2)

        val result = list.unlike(userUiData1)

        assertEquals(2, result.size)
        assertFalse(result[0].isFavorite)
        assertTrue(result[1].isFavorite)
        assertEquals(testUserData1, result[0].data)
        assertEquals(testUserData2, result[1].data)
    }

    @Test
    fun `like should not change other items`() {
        val userUiData1 = UserUiData(isFavorite = false, data = testUserData1)
        val userUiData2 = UserUiData(isFavorite = true, data = testUserData2)
        val list = listOf(userUiData1, userUiData2)

        val result = list.like(userUiData1)

        assertEquals(2, result.size)
        assertTrue(result[0].isFavorite)
        assertTrue(result[1].isFavorite) // Should remain unchanged
    }

    @Test
    fun `unlike should not change other items`() {
        val userUiData1 = UserUiData(isFavorite = true, data = testUserData1)
        val userUiData2 = UserUiData(isFavorite = false, data = testUserData2)
        val list = listOf(userUiData1, userUiData2)

        val result = list.unlike(userUiData1)

        assertEquals(2, result.size)
        assertFalse(result[0].isFavorite)
        assertFalse(result[1].isFavorite) // Should remain unchanged
    }

    @Test
    fun `operations on empty list should work correctly`() {
        val emptyList = emptyList<UserUiData>()
        val userUiData = UserUiData(isFavorite = false, data = testUserData1)

        assertEquals(0, emptyList.removeUiData(userUiData).size)
        assertEquals(1, emptyList.addUiData(userUiData).size)
        assertEquals(0, emptyList.like(userUiData).size)
        assertEquals(0, emptyList.unlike(userUiData).size)
    }

    @Test
    fun `removeUiData should remove the specified user from list`() {
        val userList = listOf(
            UserUiData(isFavorite = false, data = testUserData1.copy(title = "User 1")),
            UserUiData(isFavorite = false, data = testUserData2.copy(title = "User 2")),
            UserUiData(isFavorite = false, data = testUserData1.copy(title = "User 3"))
        )

        val result = userList.removeUiData(userList[1])

        assertEquals(2, result.size)
        assertEquals("User 1", result[0].data.title)
        assertEquals("User 3", result[1].data.title)
    }

    @Test
    fun `addUiData should add user to the end of list`() {
        val userList = listOf(
            UserUiData(isFavorite = false, data = testUserData1.copy(title = "User 1"))
        )
        val newUser = UserUiData(isFavorite = false, data = testUserData2.copy(title = "User 2"))

        val result = userList.addUiData(newUser)

        assertEquals(2, result.size)
        assertEquals("User 1", result[0].data.title)
        assertEquals("User 2", result[1].data.title)
    }

    @Test
    fun `like should change favorite status to true`() {
        val userList = listOf(
            UserUiData(isFavorite = false, data = testUserData1),
            UserUiData(isFavorite = true, data = testUserData2.copy(title = "User 2"))
        )

        val result = userList.like(userList[0])

        assertTrue(result[0].isFavorite)
        assertTrue(result[1].isFavorite)
    }

    @Test
    fun `unlike should change favorite status to false`() {
        val userList = listOf(
            UserUiData(isFavorite = true, data = testUserData1),
            UserUiData(isFavorite = false, data = testUserData2.copy(title = "User 2"))
        )

        val result = userList.unlike(userList[0])

        assertFalse(result[0].isFavorite)
        assertFalse(result[1].isFavorite)
    }

    @Test
    fun `changeFavoriteStatus should only affect matching user`() {
        val user1 = UserUiData(isFavorite = false, data = testUserData1)
        val user2 = UserUiData(isFavorite = false, data = testUserData2.copy(title = "User 2"))
        val userList = listOf(user1, user2)

        val result = userList.like(user1)

        assertTrue(result[0].isFavorite)
        assertFalse(result[1].isFavorite)
    }
}