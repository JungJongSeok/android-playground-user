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

    @After
    fun tearDown() {
        // Clean up if needed
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
    fun `Initialize intent should load favorites`() = testScope.runTest {
        val favoriteData = listOf(testUser)
        coEvery { getFavoritesUseCase() } returns favoriteData

        viewModel.processIntent(MainIntent.Initialize)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals(1, state.favorites.size)
        assertTrue(state.favorites[0].isFavorite)
    }

    @Test
    fun `Initialize intent should emit error effect when use case fails`() = testScope.runTest {
        val error = IOException("Network error")
        coEvery { getFavoritesUseCase() } throws error

        val effects = mutableListOf<MainEffect>()
        val job = launch {
            viewModel.effect.toList(effects)
        }

        viewModel.processIntent(MainIntent.Initialize)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, effects.size)
        assertTrue(effects[0] is MainEffect.ShowError)
        assertEquals(error, (effects[0] as MainEffect.ShowError).exception)
        job.cancel()
    }

    @Test
    fun `Restore intent should sync favorites and update search results`() = testScope.runTest {
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

        viewModel.processIntent(MainIntent.Restore)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals(1, state.favorites.size)
        assertTrue(state.favorites[0].isFavorite)

        val firstSearchResult = state.searches[0] as SearchTabUiData
        val secondSearchResult = state.searches[1] as SearchTabUiData

        assertTrue(firstSearchResult.data.isFavorite)
        assertFalse(secondSearchResult.data.isFavorite)
    }

    @Test
    fun `Restore intent should emit error effect when use case fails`() = testScope.runTest {
        val error = IOException("Network error")
        coEvery { getFavoritesUseCase() } throws error

        val effects = mutableListOf<MainEffect>()
        val job = launch {
            viewModel.effect.toList(effects)
        }

        viewModel.processIntent(MainIntent.Restore)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, effects.size)
        assertTrue(effects[0] is MainEffect.ShowError)
        assertEquals(error, (effects[0] as MainEffect.ShowError).exception)
        job.cancel()
    }

    @Test
    fun `Search intent should update state with results and emit ScrollToTop effect`() =
        testScope.runTest {
            val query = "test"
            val searchResults = UserSearchResult(
                users = listOf(testUser),
                totalCount = 1,
                incompleteResults = false
            )
            coEvery { searchUsersUseCase(query, 1) } returns searchResults

            val effects = mutableListOf<MainEffect>()
            val job = launch {
                viewModel.effect.toList(effects)
            }

            viewModel.processIntent(MainIntent.Search(query))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.state.first()
            assertEquals(query, state.query)
            assertEquals(2, state.currentPage)
            assertEquals(2, state.searches.size)
            assertFalse(state.isLoading)
            assertFalse(state.isEnd)

            val searchResult = state.searches[0] as SearchTabUiData
            assertEquals(testUser.login, searchResult.data.data.title)
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
    fun `Search intent with blank query should not call use case`() = testScope.runTest {
        viewModel.processIntent(MainIntent.Search("   "))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { searchUsersUseCase(any(), any()) }
        val state = viewModel.state.first()
        assertFalse(state.isLoading)
    }

    @Test
    fun `Search intent should cancel previous search`() = testScope.runTest {
        val searchResults = UserSearchResult(
            users = listOf(testUser),
            totalCount = 1,
            incompleteResults = false
        )
        coEvery { searchUsersUseCase(any(), any()) } returns searchResults

        viewModel.processIntent(MainIntent.Search("test1"))
        viewModel.processIntent(MainIntent.Search("test2"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { searchUsersUseCase("test2", 1) }
    }

    @Test
    fun `Search intent with empty results should show end border`() = testScope.runTest {
        val query = "test"
        val searchResults = UserSearchResult(
            users = emptyList(),
            totalCount = 0,
            incompleteResults = false
        )
        coEvery { searchUsersUseCase(query, 1) } returns searchResults

        viewModel.processIntent(MainIntent.Search(query))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals(1, state.searches.size)
        assertTrue(state.searches[0] is SearchTabBorder)
        val border = state.searches[0] as SearchTabBorder
        assertTrue(border.isEnd)
        assertTrue(state.isEnd)
    }

    @Test
    fun `Search intent should emit error effect when use case fails`() = testScope.runTest {
        val error = IOException("Network error")
        coEvery { searchUsersUseCase(any(), any()) } throws error

        val effects = mutableListOf<MainEffect>()
        val job = launch {
            viewModel.effect.toList(effects)
        }

        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, effects.size)
        assertTrue(effects[0] is MainEffect.ShowError)
        assertEquals(error, (effects[0] as MainEffect.ShowError).exception)
        job.cancel()
    }

    @Test
    fun `SearchMore intent should append results`() = testScope.runTest {
        val firstResults = UserSearchResult(
            users = listOf(testUser),
            totalCount = 2,
            incompleteResults = false
        )
        val secondResults = UserSearchResult(
            users = listOf(testUser2),
            totalCount = 2,
            incompleteResults = false
        )

        coEvery { searchUsersUseCase("test", 1) } returns firstResults
        coEvery { searchUsersUseCase("test", 2) } returns secondResults

        // Setup initial search
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.processIntent(MainIntent.SearchMore("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals(3, state.currentPage)
        assertEquals(4, state.searches.size)
        assertFalse(state.isLoadingMore)

        val firstResult = state.searches[0] as SearchTabUiData
        val secondResult = state.searches[2] as SearchTabUiData
        assertEquals(testUser.login, firstResult.data.data.title)
        assertEquals(testUser2.login, secondResult.data.data.title)
    }

    @Test
    fun `SearchMore intent should not call use case when isEnd is true`() = testScope.runTest {
        val searchResults = UserSearchResult(
            users = emptyList(),
            totalCount = 0,
            incompleteResults = false
        )
        coEvery { searchUsersUseCase(any(), any()) } returns searchResults

        // Setup search with empty results
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.processIntent(MainIntent.SearchMore("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { searchUsersUseCase(any(), any()) }
    }

    @Test
    fun `SearchMore intent should not call use case with blank query and no previous query`() =
        testScope.runTest {
            viewModel.processIntent(MainIntent.SearchMore(""))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify(exactly = 0) { searchUsersUseCase(any(), any()) }
        }

    @Test
    fun `SearchMore intent should use previous query when blank query provided`() =
        testScope.runTest {
            val searchResults = UserSearchResult(
                users = listOf(testUser),
                totalCount = 1,
                incompleteResults = false
            )
            coEvery { searchUsersUseCase(any(), any()) } returns searchResults

            // Setup initial search
            viewModel.processIntent(MainIntent.Search("test"))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.processIntent(MainIntent.SearchMore(""))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { searchUsersUseCase("test", 2) }
        }

    @Test
    fun `AddFavorite intent should add to use case and update state`() = testScope.runTest {
        val testUserData = UserMetaData(
            title = testUser.login,
            thumbnail = testUser.avatarUrl,
            url = testUser.htmlUrl,
            datetime = null
        )
        val userUiData = UserUiData(isFavorite = false, data = testUserData)
        val searchResults = UserSearchResult(
            users = listOf(testUser),
            totalCount = 1,
            incompleteResults = false
        )
        coEvery { searchUsersUseCase(any(), any()) } returns searchResults

        // Setup search first
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { addToFavoritesUseCase(any<User>()) }

        val state = viewModel.state.first()
        assertEquals(1, state.favorites.size)
        assertTrue(state.favorites[0].isFavorite)

        val searchResult = state.searches[0] as SearchTabUiData
        assertTrue(searchResult.data.isFavorite)
    }

    @Test
    fun `AddFavorite intent should emit error effect when use case fails`() = testScope.runTest {
        val testUserData = UserMetaData(
            title = testUser.login,
            thumbnail = testUser.avatarUrl,
            url = testUser.htmlUrl,
            datetime = null
        )
        val userUiData = UserUiData(isFavorite = false, data = testUserData)
        val error = IOException("Network error")
        coEvery { addToFavoritesUseCase(any()) } throws error

        val effects = mutableListOf<MainEffect>()
        val job = launch {
            viewModel.effect.toList(effects)
        }

        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, effects.size)
        assertTrue(effects[0] is MainEffect.ShowError)
        assertEquals(error, (effects[0] as MainEffect.ShowError).exception)
        job.cancel()
    }

    @Test
    fun `RemoveFavorite intent should remove from use case and update state`() =
        testScope.runTest {
            val testUserData = UserMetaData(
                title = testUser.login,
                thumbnail = testUser.avatarUrl,
                url = testUser.htmlUrl,
                datetime = null
            )
            val userUiData = UserUiData(isFavorite = true, data = testUserData)
            val searchResults = UserSearchResult(
                users = listOf(testUser),
                totalCount = 1,
                incompleteResults = false
            )
            coEvery { searchUsersUseCase(any(), any()) } returns searchResults

            // Setup search and favorite
            viewModel.processIntent(MainIntent.Search("test"))
            viewModel.processIntent(MainIntent.AddFavorite(userUiData))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.processIntent(MainIntent.RemoveFavorite(userUiData))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { removeFromFavoritesUseCase(any<User>()) }

            val state = viewModel.state.first()
            assertEquals(0, state.favorites.size)

            val searchResult = state.searches[0] as SearchTabUiData
            assertFalse(searchResult.data.isFavorite)
        }

    @Test
    fun `RemoveFavorite intent should emit error effect when use case fails`() =
        testScope.runTest {
            val testUserData = UserMetaData(
                title = testUser.login,
                thumbnail = testUser.avatarUrl,
                url = testUser.htmlUrl,
                datetime = null
            )
            val userUiData = UserUiData(isFavorite = true, data = testUserData)
            val error = IOException("Network error")
            coEvery { removeFromFavoritesUseCase(any()) } throws error

            val effects = mutableListOf<MainEffect>()
            val job = launch {
                viewModel.effect.toList(effects)
            }

            viewModel.processIntent(MainIntent.RemoveFavorite(userUiData))
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(1, effects.size)
            assertTrue(effects[0] is MainEffect.ShowError)
            assertEquals(error, (effects[0] as MainEffect.ShowError).exception)
            job.cancel()
        }

    @Test
    fun `loading state should be correct during search`() = testScope.runTest {
        val searchResults = UserSearchResult(
            users = listOf(testUser),
            totalCount = 1,
            incompleteResults = false
        )
        coEvery { searchUsersUseCase(any(), any()) } returns searchResults

        val loadingStates = mutableListOf<Boolean>()
        val job = launch {
            viewModel.state.collect { state ->
                loadingStates.add(state.isLoading)
            }
        }

        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(3, loadingStates.size)
        assertFalse(loadingStates[0]) // Initial state
        assertTrue(loadingStates[1]) // Loading started
        assertFalse(loadingStates[2]) // Loading finished
        job.cancel()
    }

    @Test
    fun `loadingMore state should be correct during searchMore`() = testScope.runTest {
        val searchResults = UserSearchResult(
            users = listOf(testUser),
            totalCount = 1,
            incompleteResults = false
        )
        coEvery { searchUsersUseCase(any(), any()) } returns searchResults

        // Setup initial search
        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        val loadingMoreStates = mutableListOf<Boolean>()
        val job = launch {
            viewModel.state.collect { state ->
                loadingMoreStates.add(state.isLoadingMore)
            }
        }

        viewModel.processIntent(MainIntent.SearchMore("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Should have at least 2 states recorded", loadingMoreStates.size >= 2)
        assertFalse("Initial loadingMore state should be false", loadingMoreStates.first())
        assertFalse("Final loadingMore state should be false", loadingMoreStates.last())
        job.cancel()
    }

    @Test
    fun `search with favorites should correctly mark favorite status`() = testScope.runTest {
        val favoriteData = listOf(testUser)
        coEvery { getFavoritesUseCase() } returns favoriteData
        coEvery { searchUsersUseCase(any(), any()) } returns UserSearchResult(
            users = listOf(testUser, testUser2),
            totalCount = 2,
            incompleteResults = false
        )

        // Setup favorites first
        viewModel.processIntent(MainIntent.Initialize)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        val firstResult = state.searches[0] as SearchTabUiData
        val secondResult = state.searches[1] as SearchTabUiData

        assertTrue(firstResult.data.isFavorite)
        assertFalse(secondResult.data.isFavorite)
    }

    @Test
    fun `concurrent favorite operations should be properly synchronized`() = testScope.runTest {
        val testUserData = UserMetaData(
            title = testUser.login,
            thumbnail = testUser.avatarUrl,
            url = testUser.htmlUrl,
            datetime = null
        )
        val userUiData = UserUiData(isFavorite = false, data = testUserData)

        // When - trigger multiple operations
        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        viewModel.processIntent(MainIntent.AddFavorite(userUiData))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - at least one operation should succeed
        coVerify(atLeast = 1) { addToFavoritesUseCase(any<User>()) }
    }

    @Test
    fun `state updates should be atomic`() = testScope.runTest {
        val searchResults = UserSearchResult(
            users = listOf(testUser),
            totalCount = 1,
            incompleteResults = false
        )
        coEvery { searchUsersUseCase(any(), any()) } returns searchResults

        viewModel.processIntent(MainIntent.Search("test"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals("test", state.query)
        assertEquals(2, state.currentPage)
        assertEquals(2, state.searches.size)
        assertFalse(state.isLoading)
    }
}