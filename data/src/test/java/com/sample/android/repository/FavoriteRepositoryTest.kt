package com.sample.android.repository

import com.sample.android.data.UserMetaData
import com.sample.android.domain.entity.User
import com.sample.android.utils.PreferencesModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for FavoriteRepositoryImpl
 */
class FavoriteRepositoryTest {

    private lateinit var preferencesModule: PreferencesModule
    private lateinit var repository: FavoriteRepositoryImpl

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

    private val userMetaData1 = UserMetaData(
        title = "user1",
        thumbnail = "https://avatar.com/user1",
        url = "https://github.com/user1",
        datetime = null
    )

    private val userMetaData2 = UserMetaData(
        title = "user2",
        thumbnail = "https://avatar.com/user2",
        url = "https://github.com/user2",
        datetime = null
    )

    @Before
    fun setUp() {
        preferencesModule = mockk(relaxed = true)
        repository = FavoriteRepositoryImpl(
            preferencesModule = preferencesModule,
            dispatcher = Dispatchers.Unconfined
        )
    }

    @Test
    fun `getFavorites returns empty list when no favorites exist`() = runTest {
        // Given
        every { preferencesModule.favorites } returns emptyList()

        // When
        val result = repository.getFavorites()

        // Then
        assertTrue(result.isEmpty())
        verify { preferencesModule.favorites }
    }

    @Test
    fun `getFavorites returns converted user list when favorites exist`() = runTest {
        // Given
        every { preferencesModule.favorites } returns listOf(userMetaData1, userMetaData2)

        // When
        val result = repository.getFavorites()

        // Then
        assertEquals(2, result.size)
        assertEquals("user1", result[0].login)
        assertEquals("user2", result[1].login)
        assertEquals("https://avatar.com/user1", result[0].avatarUrl)
        assertEquals("https://github.com/user1", result[0].htmlUrl)
        verify { preferencesModule.favorites }
    }

    @Test
    fun `addToFavorites converts and adds single user to preferences`() = runTest {
        // Given
        val existingFavorites = emptyList<UserMetaData>()
        every { preferencesModule.favorites } returns existingFavorites
        every { preferencesModule.favorites = any() } returns Unit

        // When
        repository.addToFavorites(sampleUser1)

        // Then
        verify {
            preferencesModule.favorites = match { favorites ->
                favorites.size == 1 &&
                    favorites[0].title == "user1" &&
                    favorites[0].thumbnail == "https://avatar.com/user1" &&
                    favorites[0].url == "https://github.com/user1"
            }
        }
    }

    @Test
    fun `addToFavorites appends to existing favorites list`() = runTest {
        // Given
        val existingFavorites = listOf(userMetaData1)
        every { preferencesModule.favorites } returns existingFavorites
        every { preferencesModule.favorites = any() } returns Unit

        // When
        repository.addToFavorites(sampleUser2)

        // Then
        verify {
            preferencesModule.favorites = match { favorites ->
                favorites.size == 2 &&
                    favorites.any { it.title == "user1" } &&
                    favorites.any { it.title == "user2" }
            }
        }
    }

    @Test
    fun `removeFromFavorites removes user from favorites list`() = runTest {
        // Given
        val existingFavorites = listOf(userMetaData1, userMetaData2)
        every { preferencesModule.favorites } returns existingFavorites
        every { preferencesModule.favorites = any() } returns Unit

        // When
        repository.removeFromFavorites(sampleUser1)

        // Then
        verify {
            preferencesModule.favorites = match { favorites ->
                favorites.size == 1 &&
                    favorites[0].title == "user2"
            }
        }
    }

    @Test
    fun `removeFromFavorites handles empty favorites list`() = runTest {
        // Given
        every { preferencesModule.favorites } returns emptyList()
        every { preferencesModule.favorites = any() } returns Unit

        // When
        repository.removeFromFavorites(sampleUser1)

        // Then
        verify {
            preferencesModule.favorites = emptyList()
        }
    }

    @Test
    fun `removeFromFavorites handles non-existing user`() = runTest {
        // Given
        val existingFavorites = listOf(userMetaData1)
        every { preferencesModule.favorites } returns existingFavorites
        every { preferencesModule.favorites = any() } returns Unit

        val nonExistingUser = User(
            id = 999L,
            login = "nonexisting",
            avatarUrl = "https://avatar.com/nonexisting",
            htmlUrl = "https://github.com/nonexisting",
            type = "User",
            score = 0.0
        )

        // When
        repository.removeFromFavorites(nonExistingUser)

        // Then
        verify {
            preferencesModule.favorites = match { favorites ->
                favorites.size == 1 && favorites[0].title == "user1"
            }
        }
    }

    @Test
    fun `addToFavorites and removeFromFavorites integration test`() = runTest {
        // Given
        every { preferencesModule.favorites } returns emptyList()
        every { preferencesModule.favorites = any() } returns Unit

        // When - Add multiple users
        repository.addToFavorites(sampleUser1)
        every { preferencesModule.favorites } returns listOf(userMetaData1)

        repository.addToFavorites(sampleUser2)
        every { preferencesModule.favorites } returns listOf(userMetaData1, userMetaData2)

        // Then - Verify both users added
        val favorites = repository.getFavorites()
        assertEquals(2, favorites.size)

        // When - Remove one user
        repository.removeFromFavorites(sampleUser1)
        every { preferencesModule.favorites } returns listOf(userMetaData2)

        // Then - Verify only one user remains
        val remainingFavorites = repository.getFavorites()
        assertEquals(1, remainingFavorites.size)
        assertEquals("user2", remainingFavorites[0].login)
    }
}