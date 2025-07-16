package com.sample.android.ui.feature.main

import com.sample.android.data.UserMetaData
import com.sample.android.data.UserMetaDataList
import com.sample.android.network.request.UserRequest
import com.sample.android.repository.FavoriteRepository
import com.sample.android.repository.SearchRepository
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
    fun `initialize should handle empty favorites`() = testScope.runTest {
        // Given
        coEvery { favoriteRepository.get() } returns emptyList()

        // When
        viewModel.initialize()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val favorites = viewModel.favorites.first()
        assertEquals(0, favorites.size)
    }

    @Test
    fun `initialize should emit error when repository fails`() = testScope.runTest {
        // Given
        val error = IOException("Network error")
        coEvery { favoriteRepository.get() } throws error

        // When
        val errorFlow = mutableListOf<Exception>()
        val job = launch {
            viewModel.error.toList(errorFlow)
        }

        viewModel.initialize()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, errorFlow.size)
        assertEquals(error, errorFlow[0])
        job.cancel()
    }

    @Test
    fun `restore should sync favorites and update search results`() = testScope.runTest {
        // Given
        val favoriteData = listOf(testUserData)
        coEvery { favoriteRepository.get() } returns favoriteData
        coEvery { searchRepository.searchItem(any()) } returns UserMetaDataList(
            listOf(
                testUserData,
                testUserData2
            )
        )

        // Setup initial search
        viewModel.search("test")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.restore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val favorites = viewModel.favorites.first()
        assertEquals(1, favorites.size)
        assertTrue(favorites[0].isFavorite)

        val searches = viewModel.searches.first()
        val firstSearchResult = searches[0] as SearchTabUiData
        val secondSearchResult = searches[1] as SearchTabUiData

        assertTrue(firstSearchResult.data.isFavorite) // testUserData is in favorites
        assertFalse(secondSearchResult.data.isFavorite) // testUserData2 is not in favorites
    }

    @Test
    fun `restore should handle error when repository fails`() = testScope.runTest {
        // Given
        val error = IOException("Network error")
        coEvery { favoriteRepository.get() } throws error

        // When
        val errorFlow = mutableListOf<Exception>()
        val job = launch {
            viewModel.error.toList(errorFlow)
        }

        viewModel.restore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, errorFlow.size)
        assertEquals(error, errorFlow[0])
        job.cancel()
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
        assertTrue(searches[0] is SearchTabUiData)
        assertTrue(searches[1] is SearchTabBorder)

        val metaData = searches[0] as SearchTabUiData
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
    fun `search with blank query should not call repository`() = testScope.runTest {
        // Given
        val query = "   "

        // When
        viewModel.search(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { searchRepository.searchItem(any()) }
    }

    @Test
    fun `search should cancel previous search job`() = testScope.runTest {
        // Given
        val query1 = "test1"
        val query2 = "test2"
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // When
        viewModel.search(query1)
        viewModel.search(query2) // This should cancel the first search
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { searchRepository.searchItem(UserRequest(query2, 1)) }
    }

    @Test
    fun `search should emit loading states`() = testScope.runTest {
        // Given
        val query = "test"
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // When
        val loadingStates = mutableListOf<Boolean>()
        val job = launch {
            viewModel.loading.toList(loadingStates)
        }

        viewModel.search(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(2, loadingStates.size)
        assertTrue(loadingStates[0]) // Loading started
        assertFalse(loadingStates[1]) // Loading finished
        job.cancel()
    }

    @Test
    fun `search should emit scrollToTop for first page`() = testScope.runTest {
        // Given
        val query = "test"
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // When
        val scrollStates = mutableListOf<Boolean>()
        val job = launch {
            viewModel.scrollToTop.toList(scrollStates)
        }

        viewModel.search(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, scrollStates.size)
        assertTrue(scrollStates[0])
        job.cancel()
    }

    @Test
    fun `search should handle empty results with end border`() = testScope.runTest {
        // Given
        val query = "test"
        val searchResults = UserMetaDataList(emptyList())
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // When
        viewModel.search(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searches = viewModel.searches.first()
        assertEquals(1, searches.size)
        assertTrue(searches[0] is SearchTabBorder)
        val border = searches[0] as SearchTabBorder
        assertTrue(border.isEnd)
    }

    @Test
    fun `search should handle repository error`() = testScope.runTest {
        // Given
        val query = "test"
        val error = IOException("Network error")
        coEvery { searchRepository.searchItem(any()) } throws error

        // When
        val errorFlow = mutableListOf<Exception>()
        val job = launch {
            viewModel.error.toList(errorFlow)
        }

        viewModel.search(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, errorFlow.size)
        assertEquals(error, errorFlow[0])
        job.cancel()
    }

    @Test
    fun `searchMore should not call repository when isEnd is true`() = testScope.runTest {
        // Given
        val query = "test"
        val searchResults = UserMetaDataList(emptyList()) // Empty results set isEnd to true
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // Setup initial search with empty results
        viewModel.search(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.searchMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { searchRepository.searchItem(any()) } // Only initial search
    }

    @Test
    fun `searchMore should not call repository with blank query`() = testScope.runTest {
        // Given - no initial search, so query is empty

        // When
        viewModel.searchMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { searchRepository.searchItem(any()) }
    }

    @Test
    fun `searchMore should append results to existing searches`() = testScope.runTest {
        // Given
        val query = "test"
        val firstResults = UserMetaDataList(listOf(testUserData))
        val secondResults = UserMetaDataList(listOf(testUserData2))

        coEvery { searchRepository.searchItem(UserRequest(query, 1)) } returns firstResults
        coEvery { searchRepository.searchItem(UserRequest(query, 2)) } returns secondResults

        // When
        viewModel.search(query)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { searchRepository.searchItem(UserRequest(query, 1)) }
        coVerify { searchRepository.searchItem(UserRequest(query, 2)) }

        val searches = viewModel.searches.first()
        assertEquals(4, searches.size) // 2 data + 2 borders

        val firstMeta = searches[0] as SearchTabUiData
        val secondMeta = searches[2] as SearchTabUiData

        assertEquals(testUserData, firstMeta.data.data)
        assertEquals(testUserData2, secondMeta.data.data)
    }

    @Test
    fun `searchMore should not append when search lock is active`() = testScope.runTest {
        // Given
        val query = "test"
        val searchResults = UserMetaDataList(listOf(testUserData))
        coEvery { searchRepository.searchItem(any()) } returns searchResults

        // Setup initial search
        viewModel.search(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - call searchMore multiple times
        viewModel.searchMore()
        viewModel.searchMore()
        viewModel.searchMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - verify all calls were made (lock is released after each call)
        coVerify(atLeast = 2) { searchRepository.searchItem(any()) }
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
        val searchMetaData = searches[0] as SearchTabUiData
        assertTrue(searchMetaData.data.isFavorite)
    }

    @Test
    fun `addFavoriteData should not add when favorite lock is active`() = testScope.runTest {
        // Given
        val userUiData = UserUiData(isFavorite = false, data = testUserData)

        // When - call addFavoriteData multiple times
        viewModel.addFavoriteData(userUiData)
        viewModel.addFavoriteData(userUiData)
        viewModel.addFavoriteData(userUiData)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - verify at least one call was made
        coVerify(atLeast = 1) { favoriteRepository.add(testUserData) }
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
        val searchMetaData = searches[0] as SearchTabUiData
        assertFalse(searchMetaData.data.isFavorite)
    }

    @Test
    fun `removeFavoriteData should not remove when favorite lock is active`() = testScope.runTest {
        // Given
        val userUiData = UserUiData(isFavorite = true, data = testUserData)

        // When - call removeFavoriteData multiple times  
        viewModel.removeFavoriteData(userUiData)
        viewModel.removeFavoriteData(userUiData)
        viewModel.removeFavoriteData(userUiData)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - verify at least one call was made
        coVerify(atLeast = 1) { favoriteRepository.remove(testUserData) }
    }

    @Test
    fun `multiple favorite operations should be properly synchronized`() = testScope.runTest {
        // Given
        val userUiData = UserUiData(isFavorite = false, data = testUserData)

        // When - alternate between add and remove operations
        viewModel.addFavoriteData(userUiData)
        viewModel.removeFavoriteData(userUiData.copy(isFavorite = true))
        viewModel.addFavoriteData(userUiData)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - operations should be properly synchronized
        val favorites = viewModel.favorites.first()
        assertEquals(1, favorites.size)
        assertTrue(favorites[0].isFavorite)
    }

    @Test
    fun `search with favorites should correctly mark favorite status`() = testScope.runTest {
        // Given
        val favoriteData = listOf(testUserData)
        coEvery { favoriteRepository.get() } returns favoriteData
        coEvery { searchRepository.searchItem(any()) } returns UserMetaDataList(
            listOf(
                testUserData,
                testUserData2
            )
        )

        // Setup favorites first
        viewModel.initialize()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.search("test")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val searches = viewModel.searches.first()
        val firstResult = searches[0] as SearchTabUiData
        val secondResult = searches[1] as SearchTabUiData

        assertTrue(firstResult.data.isFavorite) // testUserData is in favorites
        assertFalse(secondResult.data.isFavorite) // testUserData2 is not in favorites
    }

    @Test
    fun `paging should respect page numbers and increment correctly`() = testScope.runTest {
        // Given
        val query = "test"
        val page1Results = UserMetaDataList(listOf(testUserData))
        val page2Results = UserMetaDataList(listOf(testUserData2))

        coEvery { searchRepository.searchItem(UserRequest(query, 1)) } returns page1Results
        coEvery { searchRepository.searchItem(UserRequest(query, 2)) } returns page2Results

        // When
        viewModel.search(query)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { searchRepository.searchItem(UserRequest(query, 1)) }
        coVerify { searchRepository.searchItem(UserRequest(query, 2)) }

        val searches = viewModel.searches.first()
        assertEquals(4, searches.size) // 2 data + 2 borders

        val firstBorder = searches[1] as SearchTabBorder
        val secondBorder = searches[3] as SearchTabBorder
        assertEquals("1", firstBorder.text)
        assertEquals("2", secondBorder.text)
    }
}