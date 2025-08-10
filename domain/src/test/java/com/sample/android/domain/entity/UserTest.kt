package com.sample.android.domain.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserTest {

    @Test
    fun `user entity creation with all properties`() {
        val id = 12345L
        val login = "testuser"
        val avatarUrl = "https://avatars.githubusercontent.com/u/12345"
        val htmlUrl = "https://github.com/testuser"
        val type = "User"
        val score = 85.5

        val user = User(
            id = id,
            login = login,
            avatarUrl = avatarUrl,
            htmlUrl = htmlUrl,
            type = type,
            score = score
        )

        assertEquals(id, user.id)
        assertEquals(login, user.login)
        assertEquals(avatarUrl, user.avatarUrl)
        assertEquals(htmlUrl, user.htmlUrl)
        assertEquals(type, user.type)
        assertEquals(score, user.score, 0.001)
    }

    @Test
    fun `user entity equality test`() {
        val user1 = User(
            id = 1L,
            login = "user1",
            avatarUrl = "avatar1",
            htmlUrl = "html1",
            type = "User",
            score = 90.0
        )
        val user2 = User(
            id = 1L,
            login = "user1",
            avatarUrl = "avatar1",
            htmlUrl = "html1",
            type = "User",
            score = 90.0
        )
        val user3 = User(
            id = 2L,
            login = "user2",
            avatarUrl = "avatar2",
            htmlUrl = "html2",
            type = "User",
            score = 85.0
        )

        assertEquals(user1, user2)
        assertNotEquals(user1, user3)
        assertEquals(user1.hashCode(), user2.hashCode())
        assertNotEquals(user1.hashCode(), user3.hashCode())
    }

    @Test
    fun `user entity toString contains all properties`() {
        val user = User(
            id = 123L,
            login = "testuser",
            avatarUrl = "avatar",
            htmlUrl = "html",
            type = "User",
            score = 95.0
        )

        val toString = user.toString()

        assertTrue(toString.contains("123"))
        assertTrue(toString.contains("testuser"))
        assertTrue(toString.contains("avatar"))
        assertTrue(toString.contains("html"))
        assertTrue(toString.contains("User"))
        assertTrue(toString.contains("95.0"))
    }

    @Test
    fun `user entity copy function works correctly`() {
        val originalUser = User(
            id = 1L,
            login = "original",
            avatarUrl = "original_avatar",
            htmlUrl = "original_html",
            type = "User",
            score = 80.0
        )

        val copiedUser = originalUser.copy(login = "modified", score = 85.0)

        assertEquals(originalUser.id, copiedUser.id)
        assertEquals("modified", copiedUser.login)
        assertEquals(originalUser.avatarUrl, copiedUser.avatarUrl)
        assertEquals(originalUser.htmlUrl, copiedUser.htmlUrl)
        assertEquals(originalUser.type, copiedUser.type)
        assertEquals(85.0, copiedUser.score, 0.001)
    }
}