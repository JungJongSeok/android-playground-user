package com.sample.android.mapper

import com.sample.android.data.UserMetaData
import com.sample.android.domain.entity.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for DataExtensions mapper functions
 */
class DataExtensionsTest {

    @Test
    fun `User toUserMetaData converts domain entity to data model correctly`() {
        // Given
        val user = User(
            id = 123L,
            login = "testuser",
            avatarUrl = "https://avatar.com/testuser",
            htmlUrl = "https://github.com/testuser",
            type = "User",
            score = 95.5
        )

        // When
        val userMetaData = user.toUserMetaData()

        // Then
        assertEquals("testuser", userMetaData.title)
        assertEquals("https://avatar.com/testuser", userMetaData.thumbnail)
        assertEquals("https://github.com/testuser", userMetaData.url)
        assertEquals(null, userMetaData.datetime)
    }

    @Test
    fun `User toUserMetaData handles null values correctly`() {
        // Given
        val user = User(
            id = 0L,
            login = "",
            avatarUrl = "",
            htmlUrl = "",
            type = "User",
            score = 0.0
        )

        // When
        val userMetaData = user.toUserMetaData()

        // Then
        assertNotNull(userMetaData)
        assertEquals("", userMetaData.title)
        assertEquals("", userMetaData.thumbnail)
        assertEquals("", userMetaData.url)
        assertEquals(null, userMetaData.datetime)
    }

    @Test
    fun `UserMetaData toUser converts data model to domain entity correctly`() {
        // Given
        val userMetaData = UserMetaData(
            title = "testuser",
            thumbnail = "https://avatar.com/testuser",
            url = "https://github.com/testuser",
            datetime = "2023-05-21T09:42:29.000+09:00"
        )

        // When
        val user = userMetaData.toUser()

        // Then
        assertEquals("testuser", user.login)
        assertEquals("https://avatar.com/testuser", user.avatarUrl)
        assertEquals("https://github.com/testuser", user.htmlUrl)
        assertEquals("user", user.type)
        assertEquals(0.0, user.score, 0.0)
        assertEquals("testuser".hashCode().toLong(), user.id)
    }

    @Test
    fun `UserMetaData toUser handles null title correctly`() {
        // Given
        val userMetaData = UserMetaData(
            title = null,
            thumbnail = "https://avatar.com/user",
            url = "https://github.com/user",
            datetime = null
        )

        // When
        val user = userMetaData.toUser()

        // Then
        assertEquals("", user.login)
        assertEquals("https://avatar.com/user", user.avatarUrl)
        assertEquals("https://github.com/user", user.htmlUrl)
        assertEquals("user", user.type)
        assertEquals(0.0, user.score, 0.0)
        assertEquals(0L, user.id)
    }

    @Test
    fun `UserMetaData toUser handles all null values correctly`() {
        // Given
        val userMetaData = UserMetaData(
            title = null,
            thumbnail = null,
            url = null,
            datetime = null
        )

        // When
        val user = userMetaData.toUser()

        // Then
        assertEquals("", user.login)
        assertEquals("", user.avatarUrl)
        assertEquals("", user.htmlUrl)
        assertEquals("user", user.type)
        assertEquals(0.0, user.score, 0.0)
        assertEquals(0L, user.id)
    }

    @Test
    fun `bidirectional conversion maintains data integrity`() {
        // Given
        val originalUser = User(
            id = 456L,
            login = "originaluser",
            avatarUrl = "https://avatar.com/originaluser",
            htmlUrl = "https://github.com/originaluser",
            type = "User",
            score = 88.5
        )

        // When
        val userMetaData = originalUser.toUserMetaData()
        val convertedUser = userMetaData.toUser()

        // Then
        assertEquals(originalUser.login, convertedUser.login)
        assertEquals(originalUser.avatarUrl, convertedUser.avatarUrl)
        assertEquals(originalUser.htmlUrl, convertedUser.htmlUrl)
        // Note: ID will be different as it's generated from login hash
        assertEquals("user", convertedUser.type)
        assertEquals(0.0, convertedUser.score, 0.0)
    }
}