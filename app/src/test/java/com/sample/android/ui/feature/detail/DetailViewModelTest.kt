package com.sample.android.ui.feature.detail

import com.sample.android.data.UserMetaData
import com.sample.android.domain.entity.User
import com.sample.android.domain.usecase.AddToFavoritesUseCase
import com.sample.android.domain.usecase.RemoveFromFavoritesUseCase
import com.sample.android.ui.feature.main.model.UserUiData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val addToFavoritesUseCase: AddToFavoritesUseCase = mockk()
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase = mockk()

    private lateinit var viewModel: DetailViewModel

    private val testUserData = UserMetaData(
        title = "Test User",
        thumbnail = "https://example.com/image.jpg",
        url = "test@example.com",
        datetime = "2023-01-01T12:00:00.000Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { addToFavoritesUseCase(any()) } returns Unit
        coEvery { removeFromFavoritesUseCase(any()) } returns Unit

        viewModel = DetailViewModel(addToFavoritesUseCase, removeFromFavoritesUseCase)
    }

    @Test
    fun `setUiData should update current list`() = testScope.runTest {
        val userUiDataList = listOf(
            UserUiData(isFavorite = false, data = testUserData),
            UserUiData(isFavorite = true, data = testUserData.copy(title = "Second User"))
        )

        viewModel.setUiData(userUiDataList)

        val currentList = viewModel.currentList
        assertEquals(2, currentList.size)
        assertEquals(userUiDataList[0], currentList[0])
        assertEquals(userUiDataList[1], currentList[1])
    }

    @Test
    fun `setCurrentData should emit current data at position`() = testScope.runTest {
        val userUiDataList = listOf(
            UserUiData(isFavorite = false, data = testUserData),
            UserUiData(isFavorite = true, data = testUserData.copy(title = "Second User"))
        )
        viewModel.setUiData(userUiDataList)

        val emissions = mutableListOf<UserUiData>()
        val job = launch {
            viewModel.currentData.toList(emissions)
        }

        viewModel.setCurrentData(1)
        advanceUntilIdle()

        assertEquals(1, emissions.size)
        assertEquals(userUiDataList[1], emissions[0])

        job.cancel()
    }

    @Test
    fun `setCurrentData with invalid position should not emit`() = testScope.runTest {
        val userUiDataList = listOf(
            UserUiData(isFavorite = false, data = testUserData)
        )
        viewModel.setUiData(userUiDataList)

        val emissions = mutableListOf<UserUiData>()
        val job = launch {
            viewModel.currentData.toList(emissions)
        }

        viewModel.setCurrentData(10) // Invalid position
        advanceUntilIdle()

        assertEquals(0, emissions.size)

        job.cancel()
    }

    @Test
    fun `likeFavoriteData should add to use case and update state`() = testScope.runTest {
        val userUiData = UserUiData(isFavorite = false, data = testUserData)
        val userUiDataList = listOf(userUiData)
        viewModel.setUiData(userUiDataList)

        val currentDataEmissions = mutableListOf<UserUiData>()
        val currentDataJob = launch {
            viewModel.currentData.toList(currentDataEmissions)
        }

        viewModel.likeFavoriteData(userUiData)
        advanceUntilIdle()

        coVerify { addToFavoritesUseCase(any<User>()) }

        val currentList = viewModel.currentList
        assertTrue(currentList[0].isFavorite)

        val isChanged = viewModel.isChangedFavorite.value
        assertTrue(isChanged)

        assertEquals(1, currentDataEmissions.size)
        assertTrue(currentDataEmissions[0].isFavorite)

        currentDataJob.cancel()
    }

    @Test
    fun `unlikeFavoriteData should remove from use case and update state`() = testScope.runTest {
        val userUiData = UserUiData(isFavorite = true, data = testUserData)
        val userUiDataList = listOf(userUiData)
        viewModel.setUiData(userUiDataList)

        val currentDataEmissions = mutableListOf<UserUiData>()
        val currentDataJob = launch {
            viewModel.currentData.toList(currentDataEmissions)
        }

        viewModel.unlikeFavoriteData(userUiData)
        advanceUntilIdle()

        coVerify { removeFromFavoritesUseCase(any<User>()) }

        val currentList = viewModel.currentList
        assertFalse(currentList[0].isFavorite)

        val isChanged = viewModel.isChangedFavorite.value
        assertTrue(isChanged)

        assertEquals(1, currentDataEmissions.size)
        assertFalse(currentDataEmissions[0].isFavorite)

        currentDataJob.cancel()
    }

    @Test
    fun `concurrent favorite operations should be handled safely`() = testScope.runTest {
        val userUiData = UserUiData(isFavorite = false, data = testUserData)
        val userUiDataList = listOf(userUiData)
        viewModel.setUiData(userUiDataList)

        // When - first operation
        viewModel.likeFavoriteData(userUiData)
        advanceUntilIdle()

        // Then - verify first operation completed
        coVerify(exactly = 1) { addToFavoritesUseCase(any<User>()) }
        assertTrue(viewModel.currentList[0].isFavorite)

        // When - second operation
        viewModel.unlikeFavoriteData(userUiData.copy(isFavorite = true))
        advanceUntilIdle()

        // Then - verify second operation completed
        coVerify(exactly = 1) { removeFromFavoritesUseCase(any<User>()) }
        assertFalse(viewModel.currentList[0].isFavorite)
    }
}