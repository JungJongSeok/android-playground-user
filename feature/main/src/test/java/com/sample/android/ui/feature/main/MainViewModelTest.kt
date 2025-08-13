package com.sample.android.ui.feature.main

import com.sample.android.data.UserMetaData
import com.sample.android.domain.entity.User
import com.sample.android.domain.entity.UserSearchResult
import com.sample.android.domain.usecase.AddToFavoritesUseCase
import com.sample.android.domain.usecase.GetFavoritesUseCase
import com.sample.android.domain.usecase.RemoveFromFavoritesUseCase
import com.sample.android.domain.usecase.SearchUsersUseCase
import com.sample.android.ui.feature.main.model.MainEffect
import com.sample.android.ui.feature.main.model.MainIntent
import com.sample.android.ui.feature.main.model.MainState
import com.sample.android.ui.feature.main.model.SearchTabBorder
import com.sample.android.ui.feature.main.model.SearchTabUiData
import com.sample.android.ui.model.UserUiData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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

    private val searchUsersUseCase: SearchUsersUseCase = mockk()
    private val getFavoritesUseCase: GetFavoritesUseCase = mockk()
    private val addToFavoritesUseCase: AddToFavoritesUseCase = mockk()
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase = mockk()

    private lateinit var viewModel: MainViewModel

    private val testUser = User(
        id = 1L,
        login = "testuser",
        avatarUrl = "https://example.com/image.jpg",
        htmlUrl = "https://github.com/testuser",
        type = "User",
        score = 1.0
    )

    private val testUser2 = User(
        id = 2L,
        login = "testuser2",
        avatarUrl = "https://example.com/image2.jpg",
        htmlUrl = "https://github.com/testuser2",
        type = "User",
        score = 2.0
    )

    private val testUserUiData
        get() = UserUiData(
            isFavorite = false,
            data = UserMetaData(
                title = testUser.login,
                thumbnail = testUser.avatarUrl,
                url = testUser.htmlUrl,
                datetime = null
            )
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Default mock behaviors
        coEvery { getFavoritesUseCase() } returns emptyList()
        coEvery { addToFavoritesUseCase(any()) } returns Unit
        coEvery { removeFromFavoritesUseCase(any()) } returns Unit

        viewModel = MainViewModel(
            searchUsersUseCase,
            getFavoritesUseCase,
            addToFavoritesUseCase,
            removeFromFavoritesUseCase
        )
    }

    @Test
    fun `initial state should be correct`() = testScope.runTest {
        val state = viewModel.state.first()

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
    fun `initialize intent should load favorites successfully`() = testScope.runTest {
        // Given
        val favoriteData = listOf(testUser, testUser2)
        coEvery { getFavoritesUseCase() } returns favoriteData

        // When
        viewModel.processIntent(MainIntent.Initialize)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals(2, state.favorites.size)
        assertTrue(state.favorites.all { it.isFavorite })
        assertEquals(testUser.login, state.favorites[0].data.title)
        assertEquals(testUser2.login, state.favorites[1].data.title)
    }

    @Test
    fun `initialize intent should handle error`() = testScope.runTest {
        // Given
        val error = IOException("Network error")
        coEvery { getFavoritesUseCase() } throws error

        val effects = mutableListOf<MainEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        // When
        viewModel.processIntent(MainIntent.Initialize)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, effects.size)
        assertTrue(effects[0] is MainEffect.ShowError)
        assertEquals(error, (effects[0] as MainEffect.ShowError).exception)
        job.cancel()
    }

    @Test
    fun `search intent should return results and emit scroll to top`() = testScope.runTest {
        // Given
        val query = "test"
        val searchResults = UserSearchResult(
            users = listOf(testUser, testUser2),
            totalCount = 2,
            incompleteResults = false
        )
        coEvery { searchUsersUseCase(query, 1) } returns searchResults

        val effects = mutableListOf<MainEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        // When
        viewModel.processIntent(MainIntent.Search(query))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals(query, state.query)
        assertEquals(2, state.currentPage)
        assertEquals(3, state.searches.size) // 2 users + 1 border

        val searchResult1 = state.searches[0] as SearchTabUiData
        val searchResult2 = state.searches[1] as SearchTabUiData
        assertEquals(testUser.login, searchResult1.data.data.title)
        assertEquals(testUser2.login, searchResult2.data.data.title)

        assertTrue(state.searches[2] is SearchTabBorder)

        assertEquals(1, effects.size)
        assertTrue(effects[0] is MainEffect.ScrollToTop)
        job.cancel()
    }

    @Test
    fun `search with blank query should not perform search`() = testScope.runTest {
        // When
        viewModel.processIntent(MainIntent.Search("   "))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { searchUsersUseCase(any(), any()) }
        val state = viewModel.state.first()
        assertFalse(state.isLoading)
    }

    @Test
    fun `search with empty results should show end state`() = testScope.runTest {
        // Given
        val query = "test"
        val searchResults =
            UserSearchResult(users = emptyList(), totalCount = 0, incompleteResults = false)
        coEvery { searchUsersUseCase(query, 1) } returns searchResults

        // When
        viewModel.processIntent(MainIntent.Search(query))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals(1, state.searches.size)
        assertTrue(state.searches[0] is SearchTabBorder)
        assertTrue((state.searches[0] as SearchTabBorder).isEnd)
        assertTrue(state.isEnd)
    }

    @Test
    fun `search more should append results`() = testScope.runTest {
        // Given
        val firstResults =
            UserSearchResult(users = listOf(testUser), totalCount = 2, incompleteResults = false)
        val secondResults =
            UserSearchResult(users = listOf(testUser2), totalCount = 2, incompleteResults = false)
        coEvery { searchUsersUseCase("test", 1) } returns firstResults
        coEvery { searchUsersUseCase("test", 2) } returns secondResults

        // When
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.processIntent(MainIntent.SearchMore("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.state.first()
        assertEquals(3, state.currentPage)
        assertEquals(4, state.searches.size) // 2 users + 2 borders
        assertFalse(state.isLoadingMore)

        val firstResult = state.searches[0] as SearchTabUiData
        val secondResult = state.searches[2] as SearchTabUiData
        assertEquals(testUser.login, firstResult.data.data.title)
        assertEquals(testUser2.login, secondResult.data.data.title)
    }

    @Test
    fun `search more should not execute when at end`() = testScope.runTest {
        // Given - search that returns empty results (end state)
        val searchResults =
            UserSearchResult(users = emptyList(), totalCount = 0, incompleteResults = false)
        coEvery { searchUsersUseCase(any(), any()) } returns searchResults

        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(MainIntent.SearchMore("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - search should only be called once (for initial search)
        coVerify(exactly = 1) { searchUsersUseCase(any(), any()) }
    }

    @Test
    fun `add favorite should update state and call use case`() = testScope.runTest {
        // Given
        val userUiData = testUserUiData

        // When
        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { addToFavoritesUseCase(any<User>()) }
        val state = viewModel.state.first()
        assertEquals(1, state.favorites.size)
        assertTrue(state.favorites[0].isFavorite)
    }

    @Test
    fun `remove favorite should update state and call use case`() = testScope.runTest {
        // Given
        val userUiData = testUserUiData.copy(isFavorite = true)
        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.processIntent(MainIntent.RemoveFavorite(userUiData))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { removeFromFavoritesUseCase(any<User>()) }
        val state = viewModel.state.first()
        assertEquals(0, state.favorites.size)
    }

    @Test
    fun `restore intent should sync favorites with search results`() = testScope.runTest {
        // Given
        val favoriteData = listOf(testUser)
        coEvery { getFavoritesUseCase() } returns favoriteData
        coEvery { searchUsersUseCase(any(), any()) } returns UserSearchResult(
            users = listOf(testUser, testUser2),
            totalCount = 2,
            incompleteResults = false
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
    fun `concurrent search operations should be handled safely`() = testScope.runTest {
        // Given
        val searchResults =
            UserSearchResult(users = listOf(testUser), totalCount = 1, incompleteResults = false)
        coEvery { searchUsersUseCase(any(), any()) } returns searchResults

        // When - fire multiple searches quickly
        viewModel.processIntent(MainIntent.Search("test1"))
        viewModel.processIntent(MainIntent.Search("test2"))
        viewModel.processIntent(MainIntent.Search("test3"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should not crash and final state should be stable
        val finalState = viewModel.state.first()
        assertFalse(finalState.isLoading)
        // At least one search should have been performed
        coVerify(atLeast = 1) { searchUsersUseCase(any(), any()) }
    }

    @Test
    fun `concurrent favorite operations should be protected by lock`() = testScope.runTest {
        // Given
        val userUiData = testUserUiData
        coEvery { addToFavoritesUseCase(any()) } coAnswers {
            delay(50)
        }

        // When - fire multiple favorite operations
        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should be called only once due to lock
        coVerify(exactly = 1) { addToFavoritesUseCase(any<User>()) }
    }

    @Test
    fun `loading states should be managed correctly during search`() = testScope.runTest {
        // Given
        val searchResults =
            UserSearchResult(users = listOf(testUser), totalCount = 1, incompleteResults = false)
        coEvery { searchUsersUseCase(any(), any()) } returns searchResults

        val loadingStates = mutableListOf<Boolean>()
        val job = launch {
            viewModel.state.collect { state ->
                loadingStates.add(state.isLoading)
            }
        }

        // When
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should have at least initial and final states
        assertTrue("Should have at least 2 loading states", loadingStates.size >= 2)
        assertFalse("Initial state should not be loading", loadingStates.first())
        assertFalse("Final state should not be loading", loadingStates.last())
        job.cancel()
    }

    @Test
    fun `error handling should emit appropriate effects`() = testScope.runTest {
        // Given
        val error = IOException("Network error")
        coEvery { searchUsersUseCase(any(), any()) } throws error

        val effects = mutableListOf<MainEffect>()
        val job = launch { viewModel.effect.toList(effects) }

        // When
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, effects.size)
        assertTrue(effects[0] is MainEffect.ShowError)
        assertEquals(error, (effects[0] as MainEffect.ShowError).exception)
        job.cancel()
    }

    @Test
    fun `search lock should prevent concurrent search operations`() = testScope.runTest {
        // Given
        val searchResults =
            UserSearchResult(users = listOf(testUser), totalCount = 1, incompleteResults = false)
        coEvery { searchUsersUseCase(any(), any()) } coAnswers {
            delay(100)
            searchResults
        }

        // When - trigger multiple concurrent searches
        viewModel.processIntent(MainIntent.Search("test1"))
        viewModel.processIntent(MainIntent.SearchMore("test1"))
        viewModel.processIntent(MainIntent.SearchMore("test1"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - only first search should execute due to lock
        coVerify(atMost = 1) { searchUsersUseCase(any(), any()) }
    }
}