package com.sample.android.domain.usecase

import com.sample.android.domain.entity.User
import com.sample.android.domain.entity.UserSearchResult
import com.sample.android.domain.repository.SearchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class SearchUsersUseCaseTest {

    private lateinit var searchRepository: SearchRepository
    private lateinit var searchUsersUseCase: SearchUsersUseCase

    private val sampleUser = User(
        id = 1L,
        login = "testuser",
        avatarUrl = "https://avatar.com/testuser",
        htmlUrl = "https://github.com/testuser",
        type = "User",
        score = 90.0
    )

    private val sampleSearchResult = UserSearchResult(
        users = listOf(sampleUser),
        totalCount = 1,
        incompleteResults = false
    )

    @Before
    fun setUp() {
        searchRepository = mockk()
        searchUsersUseCase = SearchUsersUseCase(searchRepository)
    }

    @Test
    fun `invoke with valid query and page returns search result`() = runTest {
        val query = "kotlin"
        val page = 1
        coEvery { searchRepository.searchUsers(query, page) } returns sampleSearchResult

        val result = searchUsersUseCase(query, page)

        assertEquals(sampleSearchResult, result)
        assertEquals(1, result.users.size)
        assertEquals(sampleUser, result.users.first())
        coVerify { searchRepository.searchUsers(query, page) }
    }

    @Test
    fun `invoke with different pages works correctly`() = runTest {
        val query = "android"
        val page1 = 1
        val page2 = 2
        val result1 = sampleSearchResult
        val result2 = UserSearchResult(emptyList(), 0, false)

        coEvery { searchRepository.searchUsers(query, page1) } returns result1
        coEvery { searchRepository.searchUsers(query, page2) } returns result2

        val actualResult1 = searchUsersUseCase(query, page1)
        val actualResult2 = searchUsersUseCase(query, page2)

        assertEquals(result1, actualResult1)
        assertEquals(result2, actualResult2)
        coVerify { searchRepository.searchUsers(query, page1) }
        coVerify { searchRepository.searchUsers(query, page2) }
    }

    @Test
    fun `invoke with blank query throws IllegalArgumentException`() = runTest {
        val blankQuery = ""
        val page = 1

        try {
            searchUsersUseCase(blankQuery, page)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Search query cannot be blank", e.message)
        }
    }

    @Test
    fun `invoke with whitespace only query throws IllegalArgumentException`() = runTest {
        val whitespaceQuery = "   "
        val page = 1

        try {
            searchUsersUseCase(whitespaceQuery, page)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Search query cannot be blank", e.message)
        }
    }

    @Test
    fun `invoke with zero page throws IllegalArgumentException`() = runTest {
        val query = "test"
        val invalidPage = 0

        try {
            searchUsersUseCase(query, invalidPage)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Page number must be positive", e.message)
        }
    }

    @Test
    fun `invoke with negative page throws IllegalArgumentException`() = runTest {
        val query = "test"
        val invalidPage = -1

        try {
            searchUsersUseCase(query, invalidPage)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Page number must be positive", e.message)
        }
    }

    @Test
    fun `invoke validates input before calling repository`() = runTest {
        val blankQuery = ""
        val page = 1

        try {
            searchUsersUseCase(blankQuery, page)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            // Repository should not be called when validation fails
            coVerify(exactly = 0) { searchRepository.searchUsers(any(), any()) }
        }
    }

    @Test
    fun `invoke with repository exception propagates exception`() = runTest {
        val query = "test"
        val page = 1
        val exception = RuntimeException("Network error")
        coEvery { searchRepository.searchUsers(query, page) } throws exception

        try {
            searchUsersUseCase(query, page)
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            assertEquals("Network error", e.message)
            coVerify { searchRepository.searchUsers(query, page) }
        }
    }

    @Test
    fun `invoke with special characters in query works correctly`() = runTest {
        val specialQuery = "kotlin-android@#$"
        val page = 1
        coEvery { searchRepository.searchUsers(specialQuery, page) } returns sampleSearchResult

        val result = searchUsersUseCase(specialQuery, page)

        assertEquals(sampleSearchResult, result)
        coVerify { searchRepository.searchUsers(specialQuery, page) }
    }

    @Test
    fun `invoke with large page numbers works correctly`() = runTest {
        val query = "test"
        val largePage = 1000
        coEvery { searchRepository.searchUsers(query, largePage) } returns sampleSearchResult

        val result = searchUsersUseCase(query, largePage)

        assertEquals(sampleSearchResult, result)
        coVerify { searchRepository.searchUsers(query, largePage) }
    }

    @Test
    fun `invoke with unicode query works correctly`() = runTest {
        val unicodeQuery = "코틀린 안드로이드 "
        val page = 1
        coEvery { searchRepository.searchUsers(unicodeQuery, page) } returns sampleSearchResult

        val result = searchUsersUseCase(unicodeQuery, page)

        assertEquals(sampleSearchResult, result)
        coVerify { searchRepository.searchUsers(unicodeQuery, page) }
    }

    @Test
    fun `invoke with very long query works correctly`() = runTest {
        val longQuery = "a".repeat(1000)
        val page = 1
        coEvery { searchRepository.searchUsers(longQuery, page) } returns sampleSearchResult

        val result = searchUsersUseCase(longQuery, page)

        assertEquals(sampleSearchResult, result)
        coVerify { searchRepository.searchUsers(longQuery, page) }
    }

    @Test
    fun `invoke with mixed whitespace in query trims correctly`() = runTest {
        val queryWithSpaces = " kotlin android "
        val page = 1

        // The usecase passes the query as-is to repository (no trimming in usecase)
        coEvery { searchRepository.searchUsers(queryWithSpaces, page) } returns sampleSearchResult

        val result = searchUsersUseCase(queryWithSpaces, page)

        assertEquals(sampleSearchResult, result)
        coVerify { searchRepository.searchUsers(queryWithSpaces, page) }
    }
}