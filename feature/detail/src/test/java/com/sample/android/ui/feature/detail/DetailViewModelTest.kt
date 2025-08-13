package com.sample.android.ui.feature.detail

import com.sample.android.data.model.UserMetaData
import com.sample.android.domain.entity.User
import com.sample.android.domain.usecase.AddToFavoritesUseCase
import com.sample.android.domain.usecase.RemoveFromFavoritesUseCase
import com.sample.android.ui.feature.detail.model.DetailEffect
import com.sample.android.ui.feature.detail.model.DetailIntent
import com.sample.android.ui.feature.detail.model.DetailState
import com.sample.android.ui.mapper.toUiData
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
import org.junit.Assert.assertNull
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

    private val testUserMetaData = UserMetaData(
        title = "Test User",
        thumbnail = "https://example.com/image.jpg",
        url = "test@example.com",
        datetime = "2023-01-01T12:00:00.000Z"
    )

    private val testUserUiData = testUserMetaData.toUiData(isFavorite = false)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { addToFavoritesUseCase(any()) } returns Unit
        coEvery { removeFromFavoritesUseCase(any()) } returns Unit

        viewModel = DetailViewModel(addToFavoritesUseCase, removeFromFavoritesUseCase)
    }

    @Test
    fun `initialize intent should set selected list and current data`() = testScope.runTest {
        // Given
        val selectedList = listOf(
            testUserUiData,
            testUserMetaData.copy(title = "Second User").toUiData(isFavorite = false)
        )

        // When
        viewModel.handleIntent(DetailIntent.Initialize(selectedList))

        // Then
        val state = viewModel.state.value
        assertEquals(selectedList, state.selectedList)
        assertEquals(selectedList.first(), state.currentData)
    }

    @Test
    fun `setCurrentPosition intent should update current data`() = testScope.runTest {
        // Given
        val selectedList = listOf(
            testUserUiData,
            testUserMetaData.copy(title = "Second User").toUiData(isFavorite = false)
        )
        viewModel.handleIntent(DetailIntent.Initialize(selectedList))

        // When
        viewModel.handleIntent(DetailIntent.SetCurrentPosition(1))

        // Then
        val state = viewModel.state.value
        assertEquals(selectedList[1], state.currentData)
    }

    @Test
    fun `setCurrentPosition with invalid position should handle gracefully`() = testScope.runTest {
        // Given
        val selectedList = listOf(testUserUiData)
        viewModel.handleIntent(DetailIntent.Initialize(selectedList))

        // When
        viewModel.handleIntent(DetailIntent.SetCurrentPosition(10))

        // Then
        val state = viewModel.state.value
        assertNull(state.currentData)
    }

    @Test
    fun `toggle favorite for non-favorite user should add to favorites`() = testScope.runTest {
        // Given
        val selectedList = listOf(testUserUiData)
        viewModel.handleIntent(DetailIntent.Initialize(selectedList))

        val stateEmissions = mutableListOf<DetailState>()
        val effectEmissions = mutableListOf<DetailEffect?>()

        val stateJob = launch { viewModel.state.toList(stateEmissions) }
        val effectJob = launch { viewModel.effect.toList(effectEmissions) }

        // When
        viewModel.handleIntent(DetailIntent.ToggleFavorite(testUserUiData))
        advanceUntilIdle()

        // Then
        coVerify { addToFavoritesUseCase(any<User>()) }

        // Check final state
        val finalState = viewModel.state.value
        assertFalse(finalState.isLoading)
        assertTrue(finalState.selectedList.first().isFavorite)
        assertTrue(finalState.currentData!!.isFavorite)

        // Check effect was emitted
        val favoriteChangedEffect = effectEmissions.find { it is DetailEffect.FavoriteChanged }
        assertTrue(favoriteChangedEffect is DetailEffect.FavoriteChanged)

        stateJob.cancel()
        effectJob.cancel()
    }

    @Test
    fun `toggle favorite for favorite user should remove from favorites`() = testScope.runTest {
        // Given
        val favoriteUserUiData = testUserMetaData.toUiData(isFavorite = true)
        val selectedList = listOf(favoriteUserUiData)
        viewModel.handleIntent(DetailIntent.Initialize(selectedList))

        val stateEmissions = mutableListOf<DetailState>()
        val effectEmissions = mutableListOf<DetailEffect?>()

        val stateJob = launch { viewModel.state.toList(stateEmissions) }
        val effectJob = launch { viewModel.effect.toList(effectEmissions) }

        // When
        viewModel.handleIntent(DetailIntent.ToggleFavorite(favoriteUserUiData))
        advanceUntilIdle()

        // Then
        coVerify { removeFromFavoritesUseCase(any<User>()) }

        // Check final state
        val finalState = viewModel.state.value
        assertFalse(finalState.isLoading)
        assertFalse(finalState.selectedList.first().isFavorite)
        assertFalse(finalState.currentData!!.isFavorite)

        // Check effect was emitted
        val favoriteChangedEffect = effectEmissions.find { it is DetailEffect.FavoriteChanged }
        assertTrue(favoriteChangedEffect is DetailEffect.FavoriteChanged)

        stateJob.cancel()
        effectJob.cancel()
    }

    @Test
    fun `favorite operation error should update error state and emit error effect`() =
        testScope.runTest {
            // Given
            val errorMessage = "Network error"
            coEvery { addToFavoritesUseCase(any()) } throws Exception(errorMessage)

            val selectedList = listOf(testUserUiData)
            viewModel.handleIntent(DetailIntent.Initialize(selectedList))

            val effectEmissions = mutableListOf<DetailEffect?>()
            val effectJob = launch { viewModel.effect.toList(effectEmissions) }

        // When
        viewModel.handleIntent(DetailIntent.ToggleFavorite(testUserUiData))
        advanceUntilIdle()

        // Then
        val finalState = viewModel.state.value
        assertFalse(finalState.isLoading)
        assertEquals(errorMessage, finalState.error)

        // Check error effect was emitted
        val errorEffect = effectEmissions.find { it is DetailEffect.ShowError }
        assertTrue(errorEffect is DetailEffect.ShowError)
        assertEquals(errorMessage, (errorEffect as DetailEffect.ShowError).message)

        effectJob.cancel()
    }

    @Test
    fun `concurrent favorite operations should be prevented by lock`() = testScope.runTest {
        // Given
        val selectedList = listOf(testUserUiData)
        viewModel.handleIntent(DetailIntent.Initialize(selectedList))

        // Make the use case slow to simulate concurrent calls
        coEvery { addToFavoritesUseCase(any()) } coAnswers {
            kotlinx.coroutines.delay(100)
        }

        // When - fire multiple concurrent requests
        viewModel.handleIntent(DetailIntent.ToggleFavorite(testUserUiData))
        viewModel.handleIntent(DetailIntent.ToggleFavorite(testUserUiData))
        viewModel.handleIntent(DetailIntent.ToggleFavorite(testUserUiData))
        advanceUntilIdle()

        // Then - use case should only be called once due to lock
        coVerify(exactly = 1) { addToFavoritesUseCase(any<User>()) }
    }

    @Test
    fun `clearEffect should set effect to null`() = testScope.runTest {
        // Given - set up some effect first
        val selectedList = listOf(testUserUiData)
        viewModel.handleIntent(DetailIntent.Initialize(selectedList))
        viewModel.handleIntent(DetailIntent.ToggleFavorite(testUserUiData))
        advanceUntilIdle()

        // When
        viewModel.clearEffect()

        // Then
        assertNull(viewModel.effect.value)
    }

    @Test
    fun `initial state should have empty values`() {
        // Given - fresh viewModel
        val initialState = viewModel.state.value

        // Then
        assertTrue(initialState.selectedList.isEmpty())
        assertNull(initialState.currentData)
        assertFalse(initialState.isLoading)
        assertNull(initialState.error)
    }
}