package com.sample.android.ui.feature.main

import com.sample.android.data.UserMetaData
import com.sample.android.data.UserMetaDataList
import com.sample.android.network.request.UserRequest
import com.sample.android.repository.FavoriteRepository
import com.sample.android.repository.SearchRepository
import com.sample.android.ui.feature.main.model.SearchTabBorder
import com.sample.android.ui.feature.main.model.SearchTabMetaData
import com.sample.android.ui.feature.main.model.UserUiData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val searchRepository: SearchRepository = mockk()
    private val favoriteRepository: FavoriteRepository = mockk()

    private lateinit var viewModel: MainViewModel

    private val testUserData = UserMetaData(
        title = "Test User",
        thumbnail = "https://example.com/image.jpg",
        url = "test@example.com",
        datetime = "2023-01-01T12:00:00.000Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { favoriteRepository.get() } returns emptyList()
        coEvery { favoriteRepository.add(any()) } returns Unit
        coEvery { favoriteRepository.remove(any()) } returns Unit

        viewModel = MainViewModel(searchRepository, favoriteRepository)
    }

    @After
    fun tearDown() {
        // Clean up if needed
    }

    @Test
    fun `initialize should load favorites`() = testScope.runTest {
        // Given
        val favoriteData = listOf(testUserData)
        coEvery { favoriteRepository.get() } returns favoriteData

        // When
        viewModel.initialize()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val favorites = viewModel.favorites.first()
        assertEquals(1, favorites.size)
        assertEquals(testUserData, favorites[0].data)
        assertTrue(favorites[0].isFavorite)
    }

    @Test
    fun `search should update searches state`() = testScope.runTest {
        // Given
        val query = "test"
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // When
        viewModel.search(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searches = viewModel.searches.first()
        assertEquals(2, searches.size) // 1 data + 1 border
        assertTrue(searches[0] is SearchTabMetaData)
        assertTrue(searches[1] is SearchTabBorder)

        val metaData = searches[0] as SearchTabMetaData
        assertEquals(testUserData, metaData.data.data)
        assertFalse(metaData.data.isFavorite)
    }

    @Test
    fun `search with empty query should not call repository`() = testScope.runTest {
        // Given
        val query = ""

        // When
        viewModel.search(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { searchRepository.searchItem(any()) }
    }

    @Test
    fun `addFavoriteData should add to favorites and update search`() = testScope.runTest {
        // Given
        val userUiData = UserUiData(isFavorite = false, data = testUserData)
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // Setup initial search
        viewModel.search("test")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.addFavoriteData(userUiData)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { favoriteRepository.add(testUserData) }

        val favorites = viewModel.favorites.first()
        assertEquals(1, favorites.size)
        assertTrue(favorites[0].isFavorite)

        val searches = viewModel.searches.first()
        val searchMetaData = searches[0] as SearchTabMetaData
        assertTrue(searchMetaData.data.isFavorite)
    }

    @Test
    fun `removeFavoriteData should remove from favorites and update search`() = testScope.runTest {
        // Given
        val userUiData = UserUiData(isFavorite = true, data = testUserData)
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // Setup initial data
        viewModel.search("test")
        viewModel.addFavoriteData(userUiData)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.removeFavoriteData(userUiData)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { favoriteRepository.remove(testUserData) }

        val favorites = viewModel.favorites.first()
        assertEquals(0, favorites.size)

        val searches = viewModel.searches.first()
        val searchMetaData = searches[0] as SearchTabMetaData
        assertFalse(searchMetaData.data.isFavorite)
    }

    @Test
    fun `searchMore should append results to existing searches`() = testScope.runTest {
        // Given
        val query = "test"
        val firstResults = UserMetaDataList(listOf(testUserData))
        val secondUserData = testUserData.copy(title = "Second User")
        val secondResults = UserMetaDataList(listOf(secondUserData))

        coEvery { searchRepository.searchItem(UserRequest(query, 1)) } returns firstResults
        coEvery { searchRepository.searchItem(UserRequest(query, 2)) } returns secondResults

        // When
        viewModel.search(query)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searches = viewModel.searches.first()
        assertEquals(4, searches.size) // 2 data + 2 borders

        val firstMeta = searches[0] as SearchTabMetaData
        val secondMeta = searches[2] as SearchTabMetaData

        assertEquals(testUserData, firstMeta.data.data)
        assertEquals(secondUserData, secondMeta.data.data)
    }
}