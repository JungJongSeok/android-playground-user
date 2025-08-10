package com.sample.android.domain.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
        val users = listOf(
            createSampleUser(1L, "user1"),
            createSampleUser(2L, "user2")
        )
        val totalCount = 150
        val incompleteResults = false

        val searchResult = UserSearchResult(
            users = users,
            totalCount = totalCount,
            incompleteResults = incompleteResults
        )

        assertEquals(users, searchResult.users)
        assertEquals(totalCount, searchResult.totalCount)
        assertEquals(incompleteResults, searchResult.incompleteResults)
        assertEquals(2, searchResult.users.size)
    }

    @Test
    fun `userSearchResult with empty user list`() {
        val users = emptyList<User>()
        val totalCount = 0
        val incompleteResults = false

        val searchResult = UserSearchResult(
            users = users,
            totalCount = totalCount,
            incompleteResults = incompleteResults
        )

        assertTrue(searchResult.users.isEmpty())
        assertEquals(0, searchResult.totalCount)
        assertFalse(searchResult.incompleteResults)
    }

    @Test
    fun `userSearchResult with incomplete results flag`() {
        val users = listOf(createSampleUser(1L, "user1"))
        val totalCount = 1000
        val incompleteResults = true

        val searchResult = UserSearchResult(
            users = users,
            totalCount = totalCount,
            incompleteResults = incompleteResults
        )

        assertEquals(1, searchResult.users.size)
        assertEquals(1000, searchResult.totalCount)
        assertTrue(searchResult.incompleteResults)
    }

    @Test
    fun `userSearchResult equality test`() {
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

        assertEquals(searchResult1, searchResult2)
        assertNotEquals(searchResult1, searchResult3)
        assertEquals(searchResult1.hashCode(), searchResult2.hashCode())
        assertNotEquals(searchResult1.hashCode(), searchResult3.hashCode())
    }

    @Test
    fun `userSearchResult copy function works correctly`() {
        val originalUsers = listOf(createSampleUser(1L, "user1"))
        val originalSearchResult = UserSearchResult(
            users = originalUsers,
            totalCount = 100,
            incompleteResults = false
        )
        val newUsers = listOf(createSampleUser(2L, "user2"))

        val copiedSearchResult = originalSearchResult.copy(
            users = newUsers,
            incompleteResults = true
        )

        assertEquals(newUsers, copiedSearchResult.users)
        assertEquals(originalSearchResult.totalCount, copiedSearchResult.totalCount)
        assertTrue(copiedSearchResult.incompleteResults)
    }

    @Test
    fun `userSearchResult toString contains all properties`() {
        val users = listOf(createSampleUser(1L, "testuser"))
        val searchResult = UserSearchResult(
            users = users,
            totalCount = 500,
            incompleteResults = true
        )

        val toString = searchResult.toString()

        assertTrue(toString.contains("500"))
        assertTrue(toString.contains("true"))
    }
}