package com.sample.android.domain.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for UserSearchResult domain entity
 */
class UserSearchResultTest {

    private fun createSampleUser(id: Long, login: String) = User(
        id = id,
        login = login,
        avatarUrl = "https://avatar.com/$login",
        htmlUrl = "https://github.com/$login",
        type = "User",
        score = 85.0
    )

    @Test
    fun `userSearchResult creation with valid data`() {
        // Given
        val users = listOf(
            createSampleUser(1L, "user1"),
            createSampleUser(2L, "user2")
        )
        val totalCount = 150
        val incompleteResults = false

        // When
        val searchResult = UserSearchResult(
            users = users,
            totalCount = totalCount,
            incompleteResults = incompleteResults
        )

        // Then
        assertEquals(users, searchResult.users)
        assertEquals(totalCount, searchResult.totalCount)
        assertEquals(incompleteResults, searchResult.incompleteResults)
        assertEquals(2, searchResult.users.size)
    }

    @Test
    fun `userSearchResult with empty user list`() {
        // Given
        val users = emptyList<User>()
        val totalCount = 0
        val incompleteResults = false

        // When
        val searchResult = UserSearchResult(
            users = users,
            totalCount = totalCount,
            incompleteResults = incompleteResults
        )

        // Then
        assertTrue(searchResult.users.isEmpty())
        assertEquals(0, searchResult.totalCount)
        assertFalse(searchResult.incompleteResults)
    }

    @Test
    fun `userSearchResult with incomplete results flag`() {
        // Given
        val users = listOf(createSampleUser(1L, "user1"))
        val totalCount = 1000
        val incompleteResults = true

        // When
        val searchResult = UserSearchResult(
            users = users,
            totalCount = totalCount,
            incompleteResults = incompleteResults
        )

        // Then
        assertEquals(1, searchResult.users.size)
        assertEquals(1000, searchResult.totalCount)
        assertTrue(searchResult.incompleteResults)
    }

    @Test
    fun `userSearchResult equality test`() {
        // Given
        val users = listOf(createSampleUser(1L, "user1"))
        val searchResult1 = UserSearchResult(
            users = users,
            totalCount = 100,
            incompleteResults = false
        )
        val searchResult2 = UserSearchResult(
            users = users,
            totalCount = 100,
            incompleteResults = false
        )
        val searchResult3 = UserSearchResult(
            users = users,
            totalCount = 200,
            incompleteResults = true
        )

        // Then
        assertEquals(searchResult1, searchResult2)
        assertNotEquals(searchResult1, searchResult3)
        assertEquals(searchResult1.hashCode(), searchResult2.hashCode())
        assertNotEquals(searchResult1.hashCode(), searchResult3.hashCode())
    }

    @Test
    fun `userSearchResult copy function works correctly`() {
        // Given
        val originalUsers = listOf(createSampleUser(1L, "user1"))
        val originalSearchResult = UserSearchResult(
            users = originalUsers,
            totalCount = 100,
            incompleteResults = false
        )
        val newUsers = listOf(createSampleUser(2L, "user2"))

        // When
        val copiedSearchResult = originalSearchResult.copy(
            users = newUsers,
            incompleteResults = true
        )

        // Then
        assertEquals(newUsers, copiedSearchResult.users)
        assertEquals(originalSearchResult.totalCount, copiedSearchResult.totalCount)
        assertTrue(copiedSearchResult.incompleteResults)
    }

    @Test
    fun `userSearchResult toString contains all properties`() {
        // Given
        val users = listOf(createSampleUser(1L, "testuser"))
        val searchResult = UserSearchResult(
            users = users,
            totalCount = 500,
            incompleteResults = true
        )

        // When
        val toString = searchResult.toString()

        // Then
        assertTrue(toString.contains("500"))
        assertTrue(toString.contains("true"))
    }
}