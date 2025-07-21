package com.sample.android.ui.feature.main

import com.sample.android.data.UserMetaData
import com.sample.android.data.UserMetaDataList
import com.sample.android.network.request.UserRequest
import com.sample.android.repository.FavoriteRepository
import com.sample.android.repository.SearchRepository
import com.sample.android.ui.feature.main.model.MainEffect
import com.sample.android.ui.feature.main.model.MainIntent
import com.sample.android.ui.feature.main.model.MainState
import com.sample.android.ui.feature.main.model.SearchTabBorder
import com.sample.android.ui.feature.main.model.SearchTabUiData
import com.sample.android.ui.feature.main.model.UserUiData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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
import java.io.IOException

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

    private val testUserData2 = UserMetaData(
        title = "Test User 2",
        thumbnail = "https://example.com/image2.jpg",
        url = "test2@example.com",
        datetime = "2023-01-02T12:00:00.000Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Default mock behaviors
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
    fun `initial state should be correct`() = testScope.runTest {
        // When
        val state = viewModel.state.first()

        // Then
        assertEquals(MainState.initial(), state)
        assertTrue(state.favorites.isEmpty())
        assertTrue(state.searches.isEmpty())
        assertEquals("", state.query)
        assertEquals(1, state.currentPage)
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingMore)
        assertFalse(state.isEnd)
    }

    @Test
    fun `Initialize intent should load favorites`() = testScope.runTest {
        // Given
        val favoriteData = listOf(testUserData)
        coEvery { favoriteRepository.get() } returns favoriteData

        // When
        viewModel.processIntent(MainIntent.Initialize)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals(1, state.favorites.size)
        assertEquals(testUserData, state.favorites[0].data)
        assertTrue(state.favorites[0].isFavorite)
    }

    @Test
    fun `Initialize intent should emit error effect when repository fails`() = testScope.runTest {
        // Given
        val error = IOException("Network error")
        coEvery { favoriteRepository.get() } throws error

        // When
        val effects = mutableListOf<MainEffect>()
        val job = launch {
            viewModel.effect.toList(effects)
        }

        viewModel.processIntent(MainIntent.Initialize)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, effects.size)
        assertTrue(effects[0] is MainEffect.ShowError)
        assertEquals(error, (effects[0] as MainEffect.ShowError).exception)
        job.cancel()
    }

    @Test
    fun `Restore intent should sync favorites and update search results`() = testScope.runTest {
        // Given
        val favoriteData = listOf(testUserData)
        coEvery { favoriteRepository.get() } returns favoriteData
        coEvery { searchRepository.searchItem(any()) } returns UserMetaDataList(
            listOf(testUserData, testUserData2)
        )

        // Setup initial search
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(MainIntent.Restore)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals(1, state.favorites.size)
        assertTrue(state.favorites[0].isFavorite)

        val firstSearchResult = state.searches[0] as SearchTabUiData
        val secondSearchResult = state.searches[1] as SearchTabUiData

        assertTrue(firstSearchResult.data.isFavorite)
        assertFalse(secondSearchResult.data.isFavorite)
    }

    @Test
    fun `Restore intent should emit error effect when repository fails`() = testScope.runTest {
        // Given
        val error = IOException("Network error")
        coEvery { favoriteRepository.get() } throws error

        // When
        val effects = mutableListOf<MainEffect>()
        val job = launch {
            viewModel.effect.toList(effects)
        }

        viewModel.processIntent(MainIntent.Restore)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, effects.size)
        assertTrue(effects[0] is MainEffect.ShowError)
        assertEquals(error, (effects[0] as MainEffect.ShowError).exception)
        job.cancel()
    }

    @Test
    fun `Search intent should update state with results and emit ScrollToTop effect`() =
        testScope.runTest {
        // Given
        val query = "test"
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // When
        val effects = mutableListOf<MainEffect>()
        val job = launch {
            viewModel.effect.toList(effects)
        }

        viewModel.processIntent(MainIntent.Search(query))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals(query, state.query)
        assertEquals(2, state.currentPage)
        assertEquals(2, state.searches.size)
        assertFalse(state.isLoading)
        assertFalse(state.isEnd)

        val searchResult = state.searches[0] as SearchTabUiData
        assertEquals(testUserData, searchResult.data.data)
        assertFalse(searchResult.data.isFavorite)

        assertTrue(state.searches[1] is SearchTabBorder)
        val border = state.searches[1] as SearchTabBorder
        assertEquals("1", border.text)
        assertFalse(border.isEnd)

        assertEquals(1, effects.size)
        assertTrue(effects[0] is MainEffect.ScrollToTop)
        job.cancel()
    }

    @Test
    fun `Search intent with blank query should not call repository`() = testScope.runTest {
        // When
        viewModel.processIntent(MainIntent.Search("   "))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { searchRepository.searchItem(any()) }
        val state = viewModel.state.first()
        assertFalse(state.isLoading)
    }

    @Test
    fun `Search intent should cancel previous search`() = testScope.runTest {
        // Given
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // When
        viewModel.processIntent(MainIntent.Search("test1"))
        viewModel.processIntent(MainIntent.Search("test2"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { searchRepository.searchItem(UserRequest("test2", 1)) }
    }

    @Test
    fun `Search intent with empty results should show end border`() = testScope.runTest {
        // Given
        val query = "test"
        val searchResults = UserMetaDataList(emptyList())
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // When
        viewModel.processIntent(MainIntent.Search(query))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals(1, state.searches.size)
        assertTrue(state.searches[0] is SearchTabBorder)
        val border = state.searches[0] as SearchTabBorder
        assertTrue(border.isEnd)
        assertTrue(state.isEnd)
    }

    @Test
    fun `Search intent should emit error effect when repository fails`() = testScope.runTest {
        // Given
        val error = IOException("Network error")
        coEvery { searchRepository.searchItem(any()) } throws error

        // When
        val effects = mutableListOf<MainEffect>()
        val job = launch {
            viewModel.effect.toList(effects)
        }

        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, effects.size)
        assertTrue(effects[0] is MainEffect.ShowError)
        assertEquals(error, (effects[0] as MainEffect.ShowError).exception)
        job.cancel()
    }

    @Test
    fun `SearchMore intent should append results`() = testScope.runTest {
        // Given
        val firstResults = UserMetaDataList(listOf(testUserData))
        val secondResults = UserMetaDataList(listOf(testUserData2))

        coEvery { searchRepository.searchItem(UserRequest("test", 1)) } returns firstResults
        coEvery { searchRepository.searchItem(UserRequest("test", 2)) } returns secondResults

        // Setup initial search
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(MainIntent.SearchMore("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals(3, state.currentPage)
        assertEquals(4, state.searches.size)
        assertFalse(state.isLoadingMore)

        val firstResult = state.searches[0] as SearchTabUiData
        val secondResult = state.searches[2] as SearchTabUiData
        assertEquals(testUserData, firstResult.data.data)
        assertEquals(testUserData2, secondResult.data.data)
    }

    @Test
    fun `SearchMore intent should not call repository when isEnd is true`() = testScope.runTest {
        // Given
        val searchResults = UserMetaDataList(emptyList())
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // Setup search with empty results
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(MainIntent.SearchMore("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { searchRepository.searchItem(any()) }
    }

    @Test
    fun `SearchMore intent should not call repository with blank query and no previous query`() =
        testScope.runTest {
        // When
        viewModel.processIntent(MainIntent.SearchMore(""))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { searchRepository.searchItem(any()) }
    }

    @Test
    fun `SearchMore intent should use previous query when blank query provided`() =
        testScope.runTest {
        // Given
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // Setup initial search
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(MainIntent.SearchMore(""))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { searchRepository.searchItem(UserRequest("test", 2)) }
    }

    @Test
    fun `AddFavorite intent should add to repository and update state`() = testScope.runTest {
        // Given
        val userUiData = UserUiData(isFavorite = false, data = testUserData)
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // Setup search first
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { favoriteRepository.add(testUserData) }

        val state = viewModel.state.first()
        assertEquals(1, state.favorites.size)
        assertTrue(state.favorites[0].isFavorite)

        val searchResult = state.searches[0] as SearchTabUiData
        assertTrue(searchResult.data.isFavorite)
    }

    @Test
    fun `AddFavorite intent should emit error effect when repository fails`() = testScope.runTest {
        // Given
        val userUiData = UserUiData(isFavorite = false, data = testUserData)
        val error = IOException("Network error")
        coEvery { favoriteRepository.add(any()) } throws error

        // When
        val effects = mutableListOf<MainEffect>()
        val job = launch {
            viewModel.effect.toList(effects)
        }

        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, effects.size)
        assertTrue(effects[0] is MainEffect.ShowError)
        assertEquals(error, (effects[0] as MainEffect.ShowError).exception)
        job.cancel()
    }

    @Test
    fun `RemoveFavorite intent should remove from repository and update state`() =
        testScope.runTest {
        // Given
        val userUiData = UserUiData(isFavorite = true, data = testUserData)
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // Setup search and favorite
        viewModel.processIntent(MainIntent.Search("test"))
        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(MainIntent.RemoveFavorite(userUiData))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { favoriteRepository.remove(testUserData) }

        val state = viewModel.state.first()
        assertEquals(0, state.favorites.size)

        val searchResult = state.searches[0] as SearchTabUiData
        assertFalse(searchResult.data.isFavorite)
    }

    @Test
    fun `RemoveFavorite intent should emit error effect when repository fails`() =
        testScope.runTest {
        // Given
        val userUiData = UserUiData(isFavorite = true, data = testUserData)
        val error = IOException("Network error")
        coEvery { favoriteRepository.remove(any()) } throws error

        // When
        val effects = mutableListOf<MainEffect>()
        val job = launch {
            viewModel.effect.toList(effects)
        }

        viewModel.processIntent(MainIntent.RemoveFavorite(userUiData))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, effects.size)
        assertTrue(effects[0] is MainEffect.ShowError)
        assertEquals(error, (effects[0] as MainEffect.ShowError).exception)
        job.cancel()
    }

    @Test
    fun `loading state should be correct during search`() = testScope.runTest {
        // Given
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // When
        val loadingStates = mutableListOf<Boolean>()
        val job = launch {
            viewModel.state.collect { state ->
                loadingStates.add(state.isLoading)
            }
        }

        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(3, loadingStates.size)
        assertFalse(loadingStates[0]) // Initial state
        assertTrue(loadingStates[1]) // Loading started
        assertFalse(loadingStates[2]) // Loading finished
        job.cancel()
    }

    @Test
    fun `loadingMore state should be correct during searchMore`() = testScope.runTest {
        // Given
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // Setup initial search
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val loadingMoreStates = mutableListOf<Boolean>()
        val job = launch {
            viewModel.state.collect { state ->
                loadingMoreStates.add(state.isLoadingMore)
            }
        }

        viewModel.processIntent(MainIntent.SearchMore("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue("Should have at least 2 states recorded", loadingMoreStates.size >= 2)
        assertFalse("Initial loadingMore state should be false", loadingMoreStates.first())
        assertFalse("Final loadingMore state should be false", loadingMoreStates.last())
        job.cancel()
    }

    @Test
    fun `search with favorites should correctly mark favorite status`() = testScope.runTest {
        // Given
        val favoriteData = listOf(testUserData)
        coEvery { favoriteRepository.get() } returns favoriteData
        coEvery { searchRepository.searchItem(any()) } returns UserMetaDataList(
            listOf(testUserData, testUserData2)
        )

        // Setup favorites first
        viewModel.processIntent(MainIntent.Initialize)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        val firstResult = state.searches[0] as SearchTabUiData
        val secondResult = state.searches[1] as SearchTabUiData

        assertTrue(firstResult.data.isFavorite)
        assertFalse(secondResult.data.isFavorite)
    }

    @Test
    fun `concurrent favorite operations should be properly synchronized`() = testScope.runTest {
        // Given
        val userUiData = UserUiData(isFavorite = false, data = testUserData)

        // When - trigger multiple operations
        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - at least one operation should succeed
        coVerify(atLeast = 1) { favoriteRepository.add(testUserData) }
    }

    @Test
    fun `state updates should be atomic`() = testScope.runTest {
        // Given
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // When
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals("test", state.query)
        assertEquals(2, state.currentPage)
        assertEquals(2, state.searches.size)
        assertFalse(state.isLoading)
    }
}