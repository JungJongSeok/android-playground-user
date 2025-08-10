package com.sample.android.domain.usecase

import com.sample.android.domain.entity.User
import com.sample.android.domain.repository.FavoriteRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for RemoveFromFavoritesUseCase
 */
class RemoveFromFavoritesUseCaseTest {

    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var removeFromFavoritesUseCase: RemoveFromFavoritesUseCase

    private val sampleUser = User(
        id = 1L,
        login = "testuser",
        avatarUrl = "https://avatar.com/testuser",
        htmlUrl = "https://github.com/testuser",
        type = "User",
        score = 95.0
    )

    @Before
    fun setUp() {
        favoriteRepository = mockk()
        removeFromFavoritesUseCase = RemoveFromFavoritesUseCase(favoriteRepository)
    }

    @Test
    fun `invoke removes user from favorites successfully`() = runTest {
        // Given
        coJustRun { favoriteRepository.removeFromFavorites(sampleUser) }

        // When
        removeFromFavoritesUseCase(sampleUser)

        // Then
        coVerify { favoriteRepository.removeFromFavorites(sampleUser) }
    }

    @Test
    fun `invoke removes different users from favorites`() = runTest {
        // Given
        val user1 = sampleUser
        val user2 = User(
            id = 2L,
            login = "user2",
            avatarUrl = "https://avatar.com/user2",
            htmlUrl = "https://github.com/user2",
            type = "User",
            score = 88.0
        )

        coJustRun { favoriteRepository.removeFromFavorites(user1) }
        coJustRun { favoriteRepository.removeFromFavorites(user2) }

        // When
        removeFromFavoritesUseCase(user1)
        removeFromFavoritesUseCase(user2)

        // Then
        coVerify { favoriteRepository.removeFromFavorites(user1) }
        coVerify { favoriteRepository.removeFromFavorites(user2) }
    }

    @Test
    fun `invoke with same user multiple times calls repository each time`() = runTest {
        // Given
        coJustRun { favoriteRepository.removeFromFavorites(sampleUser) }

        // When
        removeFromFavoritesUseCase(sampleUser)
        removeFromFavoritesUseCase(sampleUser)

        // Then
        coVerify(exactly = 2) { favoriteRepository.removeFromFavorites(sampleUser) }
    }

    @Test
    fun `invoke with repository exception propagates exception`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { favoriteRepository.removeFromFavorites(sampleUser) } throws exception

        // When & Then
        try {
            removeFromFavoritesUseCase(sampleUser)
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
            coVerify { favoriteRepository.removeFromFavorites(sampleUser) }
        }
    }

    @Test
    fun `invoke passes user data correctly to repository`() = runTest {
        // Given
        val specificUser = User(
            id = 12345L,
            login = "specific_user",
            avatarUrl = "https://avatar.com/specific",
            htmlUrl = "https://github.com/specific",
            type = "Organization",
            score = 100.0
        )
        coJustRun { favoriteRepository.removeFromFavorites(specificUser) }

        // When
        removeFromFavoritesUseCase(specificUser)

        // Then
        coVerify {
            favoriteRepository.removeFromFavorites(
                match { user ->
                    user.id == 12345L &&
                        user.login == "specific_user" &&
                        user.avatarUrl == "https://avatar.com/specific" &&
                        user.htmlUrl == "https://github.com/specific" &&
                        user.type == "Organization" &&
                        user.score == 100.0
                }
            )
        }
    }

    @Test
    fun `invoke with user having special characters works correctly`() = runTest {
        // Given
        val specialUser = User(
            id = 999L,
            login = "user-with_special@chars",
            avatarUrl = "https://avatar.com/user-with_special@chars",
            htmlUrl = "https://github.com/user-with_special@chars",
            type = "User",
            score = 75.5
        )
        coJustRun { favoriteRepository.removeFromFavorites(specialUser) }

        // When
        removeFromFavoritesUseCase(specialUser)

        // Then
        coVerify { favoriteRepository.removeFromFavorites(specialUser) }
    }

    @Test
    fun `invoke with non-existing user works correctly`() = runTest {
        // Given
        val nonExistingUser = User(
            id = -1L,
            login = "nonexistent",
            avatarUrl = "https://avatar.com/nonexistent",
            htmlUrl = "https://github.com/nonexistent",
            type = "User",
            score = 0.0
        )
        coJustRun { favoriteRepository.removeFromFavorites(nonExistingUser) }

        // When
        removeFromFavoritesUseCase(nonExistingUser)

        // Then
        coVerify { favoriteRepository.removeFromFavorites(nonExistingUser) }
    }

    @Test
    fun `invoke with minimum score user works correctly`() = runTest {
        // Given
        val minScoreUser = User(
            id = 100L,
            login = "minuser",
            avatarUrl = "https://avatar.com/minuser",
            htmlUrl = "https://github.com/minuser",
            type = "User",
            score = 0.0
        )
        coJustRun { favoriteRepository.removeFromFavorites(minScoreUser) }

        // When
        removeFromFavoritesUseCase(minScoreUser)

        // Then
        coVerify { favoriteRepository.removeFromFavorites(minScoreUser) }
    }

    @Test
    fun `invoke with maximum score user works correctly`() = runTest {
        // Given
        val maxScoreUser = User(
            id = 200L,
            login = "maxuser",
            avatarUrl = "https://avatar.com/maxuser",
            htmlUrl = "https://github.com/maxuser",
            type = "User",
            score = Double.MAX_VALUE
        )
        coJustRun { favoriteRepository.removeFromFavorites(maxScoreUser) }

        // When
        removeFromFavoritesUseCase(maxScoreUser)

        // Then
        coVerify { favoriteRepository.removeFromFavorites(maxScoreUser) }
    }
}