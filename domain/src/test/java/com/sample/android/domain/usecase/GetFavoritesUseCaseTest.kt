package com.sample.android.domain.usecase

import com.sample.android.domain.entity.User
import com.sample.android.domain.repository.FavoriteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class GetFavoritesUseCaseTest {

    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var getFavoritesUseCase: GetFavoritesUseCase

    private val sampleUser1 = User(
        id = 1L,
        login = "user1",
        avatarUrl = "https://avatar.com/user1",
        htmlUrl = "https://github.com/user1",
        type = "User",
        score = 85.0
    )

    private val sampleUser2 = User(
        id = 2L,
        login = "user2",
        avatarUrl = "https://avatar.com/user2",
        htmlUrl = "https://github.com/user2",
        type = "User",
        score = 90.0
    )

    @Before
    fun setUp() {
        favoriteRepository = mockk()
        getFavoritesUseCase = GetFavoritesUseCase(favoriteRepository)
    }

    @Test
    fun `invoke returns list of favorite users`() = runTest {
        val favoriteUsers = listOf(sampleUser1, sampleUser2)
        coEvery { favoriteRepository.getFavorites() } returns favoriteUsers

        val result = getFavoritesUseCase()

        assertEquals(favoriteUsers, result)
        assertEquals(2, result.size)
        assertEquals(sampleUser1, result[0])
        assertEquals(sampleUser2, result[1])
        coVerify { favoriteRepository.getFavorites() }
    }

    @Test
    fun `invoke returns empty list when no favorites exist`() = runTest {
        val emptyFavorites = emptyList<User>()
        coEvery { favoriteRepository.getFavorites() } returns emptyFavorites

        val result = getFavoritesUseCase()

        assertEquals(emptyFavorites, result)
        assertTrue(result.isEmpty())
        coVerify { favoriteRepository.getFavorites() }
    }

    @Test
    fun `invoke returns single favorite user correctly`() = runTest {
        val singleFavorite = listOf(sampleUser1)
        coEvery { favoriteRepository.getFavorites() } returns singleFavorite

        val result = getFavoritesUseCase()

        assertEquals(singleFavorite, result)
        assertEquals(1, result.size)
        assertEquals(sampleUser1, result.first())
        coVerify { favoriteRepository.getFavorites() }
    }

    @Test
    fun `invoke with repository exception propagates exception`() = runTest {
        val exception = RuntimeException("Database error")
        coEvery { favoriteRepository.getFavorites() } throws exception

        try {
            getFavoritesUseCase()
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
            coVerify { favoriteRepository.getFavorites() }
        }
    }

    @Test
    fun `invoke multiple times calls repository each time`() = runTest {
        val favoriteUsers = listOf(sampleUser1)
        coEvery { favoriteRepository.getFavorites() } returns favoriteUsers

        val result1 = getFavoritesUseCase()
        val result2 = getFavoritesUseCase()

        assertEquals(favoriteUsers, result1)
        assertEquals(favoriteUsers, result2)
        coVerify(exactly = 2) { favoriteRepository.getFavorites() }
    }

    @Test
    fun `invoke returns immutable list`() = runTest {
        val favoriteUsers = listOf(sampleUser1, sampleUser2)
        coEvery { favoriteRepository.getFavorites() } returns favoriteUsers

        val result = getFavoritesUseCase()

        assertEquals(favoriteUsers, result)
        // Verify it's a proper list implementation
        assertTrue(result is List<User>)
        coVerify { favoriteRepository.getFavorites() }
    }

    @Test
    fun `invoke preserves user data integrity`() = runTest {
        val favoriteUsers = listOf(sampleUser1)
        coEvery { favoriteRepository.getFavorites() } returns favoriteUsers

        val result = getFavoritesUseCase()

        val returnedUser = result.first()
        assertEquals(sampleUser1.id, returnedUser.id)
        assertEquals(sampleUser1.login, returnedUser.login)
        assertEquals(sampleUser1.avatarUrl, returnedUser.avatarUrl)
        assertEquals(sampleUser1.htmlUrl, returnedUser.htmlUrl)
        assertEquals(sampleUser1.type, returnedUser.type)
        assertEquals(sampleUser1.score, returnedUser.score, 0.001)
        coVerify { favoriteRepository.getFavorites() }
    }
}