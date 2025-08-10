package com.sample.android.repository

import com.sample.android.network.UserService
import com.sample.android.network.request.UserRequest
import com.sample.android.network.response.UserDob
import com.sample.android.network.response.UserResponse
import com.sample.android.network.response.UserResponseInfo
import com.sample.android.network.response.UserResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SearchRepositoryImpl
 */
class SearchRepositoryTest {
    @MockK(relaxed = true)
    lateinit var userService: UserService

    private lateinit var repository: SearchRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        repository = SearchRepositoryImpl(userService)
    }

    @Test
    fun `searchUsers returns empty result when no users found`() =
        runTest {
            // Given
            val query = "test"
            val page = 1
            val userRequest = UserRequest(seed = query, page = page)
            val response = UserResponse(
                info = UserResponseInfo(
                    page = 1,
                    results = 0,
                    seed = "test",
                    version = "version"
                ),
                results = emptyList()
            )
            coEvery { userService.search(userRequest) } returns response

            // When
            val result = repository.searchUsers(query, page)

            // Then
            assertTrue(result.users.isEmpty())
            assertEquals(0, result.totalCount)
        }

    @Test
    fun `searchUsers returns users when found`() = runTest {
        // Given
        val query = "test"
        val page = 1
        val userRequest = UserRequest(seed = query, page = page)

        val userResult = UserResult(
            cell = "123-456-789",
            dob = UserDob(age = 30, date = "2023-05-21T09:42:29.000+09:00"),
            email = "user@example.com",
            gender = "male",
            id = null,
            location = null,
            login = null,
            name = null,
            nat = null,
            phone = "123-456-789",
            picture = null,
            registered = null
        )

        val response = UserResponse(
            info = UserResponseInfo(
                page = 1,
                results = 1,
                seed = "test",
                version = "version"
            ),
            results = listOf(userResult)
        )

        coEvery { userService.search(userRequest) } returns response

        // When
        val result = repository.searchUsers(query, page)

        // Then
        assertEquals(1, result.users.size)
        assertEquals(1, result.totalCount)
        assertEquals("user@example.com", result.users[0].htmlUrl)
    }

    @Test
    fun `searchUsers handles network response correctly with multiple users`() = runTest {
        // Given
        val query = "developers"
        val page = 1
        val userRequest = UserRequest(seed = query, page = page)

        val userResults = listOf(
            UserResult(
                cell = null,
                dob = UserDob(age = 25, date = "2023-05-21T09:42:29.000+09:00"),
                email = "user1@example.com",
                gender = null,
                id = null,
                location = null,
                login = null,
                name = null,
                nat = null,
                phone = null,
                picture = null,
                registered = null
            ),
            UserResult(
                cell = null,
                dob = UserDob(age = 28, date = "2023-05-20T09:42:29.000+09:00"),
                email = "user2@example.com",
                gender = null,
                id = null,
                location = null,
                login = null,
                name = null,
                nat = null,
                phone = null,
                picture = null,
                registered = null
            )
        )

        val response = UserResponse(
            info = UserResponseInfo(
                page = 1,
                results = 2,
                seed = "developers",
                version = "version"
            ),
            results = userResults
        )

        coEvery { userService.search(userRequest) } returns response

        // When
        val result = repository.searchUsers(query, page)

        // Then
        assertEquals(2, result.users.size)
        assertEquals(2, result.totalCount)
        assertEquals(false, result.incompleteResults)
    }

    @Test
    fun `searchUsers with different page numbers`() = runTest {
        // Given
        val query = "test"
        val page = 2
        val userRequest = UserRequest(seed = query, page = page)
        val response = UserResponse(
            info = UserResponseInfo(
                page = 2,
                results = 0,
                seed = "test",
                version = "version"
            ),
            results = emptyList()
        )
        coEvery { userService.search(userRequest) } returns response

        // When
        val result = repository.searchUsers(query, page)

        // Then
        assertTrue(result.users.isEmpty())
        assertEquals(0, result.totalCount)
    }
}