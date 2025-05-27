package com.sample.android.ui.detail

import com.sample.android.data.UserMetaData
import com.sample.android.repository.FavoriteRepository
import com.sample.android.ui.data.UserUiData
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @MockK(relaxed = true)
    lateinit var favoriteRepository: FavoriteRepository

    private lateinit var viewModel: DetailViewModel

    private val data1 = UserMetaData(
        thumbnail = "thumbnail1",
        title     = "title1",
        url       = "http://example.com/1",
        datetime  = "2025-05-18T13:00:00.000+09:00"
    )
    private val data2 = UserMetaData(
        thumbnail = "thumbnail2",
        title     = "title2",
        url       = "http://example.com/2",
        datetime  = "2025-05-18T14:00:00.000+09:00"
    )

    private val uiData1 = UserUiData(isFavorite = false, data = data1)
    private val uiData2 = UserUiData(isFavorite = false, data = data2)

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        Dispatchers.setMain(testDispatcher)
        viewModel = DetailViewModel(favoriteRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setUiData updates currentList`() {
        val list = listOf(uiData1, uiData2)
        viewModel.setUiData(list)
        assertEquals(list, viewModel.currentList)
    }

    @Test
    fun `setCurrentData emits the correct item`() = runTest {
        viewModel.setUiData(listOf(uiData1, uiData2))
        var currentData: UserUiData? = null
        val job = launch(testDispatcher) {
            viewModel.currentData.collect {
                currentData = it
            }
        }

        viewModel.setCurrentData(1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(uiData2, currentData)
        job.cancel()
    }

    @Test
    fun `likeFavoriteData calls repository add, updates list and isChangedFavorite`() = runTest {
        viewModel.setUiData(listOf(uiData1))
        coEvery { favoriteRepository.add(data1) } returns Unit

        viewModel.likeFavoriteData(uiData1)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { favoriteRepository.add(data1) }
        val isFavoriteData =
            viewModel.currentList.find { it.data == data1 }?.isFavorite ?: false
        assertTrue(isFavoriteData)
        assertTrue(viewModel.isChangedFavorite.value)
    }

    @Test
    fun `unlikeFavoriteData calls repository remove, updates list and isChangedFavorite`() = runTest {
        val liked = uiData1.copy(isFavorite = true)
        viewModel.setUiData(listOf(liked))
        coEvery { favoriteRepository.remove(data1) } returns Unit

        viewModel.unlikeFavoriteData(liked)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { favoriteRepository.remove(data1) }
        val isFavoriteData =
            viewModel.currentList.find { it.data == data1 }?.isFavorite ?: false
        assertFalse(isFavoriteData)
        assertTrue(viewModel.isChangedFavorite.value)
    }
}