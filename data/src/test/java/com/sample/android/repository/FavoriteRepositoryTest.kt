package com.sample.android.repository

import com.sample.android.data.model.UserMetaData
import com.sample.android.data.repository.FavoriteRepositoryImpl
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

class FavoriteRepositoryTest {

    private lateinit var preferencesModule: PreferencesModule
    private lateinit var repository: FavoriteRepositoryImpl

    private val data1 = UserMetaData(
        thumbnail = "thumbnail1",
        title = "title1",
        url = "url1",
        datetime = "2025-05-19T09:42:29.000+09:00"
    )
    private val data2 = UserMetaData(
        thumbnail = "thumbnail2",
        title = "title2",
        url = "url2",
        datetime = "2025-05-19T09:42:29.000+09:00"
    )

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
    fun `getFavorites returns empty list initially`() = runTest {
        every { preferencesModule.favorites } returns emptyList()

        val result = repository.getFavorites()

        assertTrue(result.isEmpty())
        verify { preferencesModule.favorites }
    }

    @Test
    fun `getFavorites returns converted user list`() = runTest {
        every { preferencesModule.favorites } returns listOf(userMetaData1, userMetaData2)

        val result = repository.getFavorites()

        assertEquals(2, result.size)
        assertEquals("user1", result[0].login)
        assertEquals("user2", result[1].login)
        verify { preferencesModule.favorites }
    }

    @Test
    fun `addToFavorites converts and adds user to preferences`() = runTest {
        every { preferencesModule.favorites } returns emptyList()
        every { preferencesModule.favorites = any() } returns Unit

        repository.addToFavorites(sampleUser1)

        verify {
            preferencesModule.favorites = match { favorites ->
                favorites.size == 1 && favorites[0].title == "user1"
            }
        }
    }

    @Test
    fun `addToFavorites appends to existing favorites`() = runTest {
        every { preferencesModule.favorites } returns listOf(userMetaData1)
        every { preferencesModule.favorites = any() } returns Unit

        repository.addToFavorites(sampleUser2)

        verify {
            preferencesModule.favorites = match { favorites ->
                favorites.size == 2 && favorites[0].title == "user1" && favorites[1].title == "user2"
            }
        }
    }

    @Test
    fun `removeFromFavorites filters out the specified user`() = runTest {
        every { preferencesModule.favorites } returns listOf(userMetaData1, userMetaData2)
        every { preferencesModule.favorites = any() } returns Unit

        repository.removeFromFavorites(sampleUser1)

        verify {
            preferencesModule.favorites = match { favorites ->
                favorites.size == 1 && favorites[0].title == "user2"
            }
        }
    }
}