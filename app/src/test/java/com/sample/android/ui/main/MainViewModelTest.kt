package com.sample.android.ui.main

import com.sample.android.data.UserMetaData
import com.sample.android.data.UserMetaDataList
import com.sample.android.network.request.UserRequest
import com.sample.android.repository.FavoriteRepository
import com.sample.android.repository.SearchRepository
import com.sample.android.ui.data.SearchTabMetaData
import com.sample.android.ui.data.UserUiData
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException


@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @MockK(relaxed = true)
    lateinit var searchRepository: SearchRepository

    @MockK(relaxed = true)
    lateinit var favoriteRepository: FavoriteRepository

    private lateinit var viewModel: MainViewModel

    private val userRequest1 = UserRequest("q", 1)
    private val userRequest2 = UserRequest("q2", 2)

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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this, relaxUnitFun = true)
        viewModel = MainViewModel(searchRepository, favoriteRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initialize emits favorites or error`() = runTest {
        coEvery {
            favoriteRepository.get()
        } returns listOf(data1, data2)

        viewModel.initialize()
        testDispatcher.scheduler.advanceUntilIdle()

        val favorites = viewModel.favorites.value
        assertEquals(2, favorites.size)
        assertTrue(favorites.all { it.isFavorite })
    }

    @Test
    fun `restore updates searches and favorites`() = runTest {
        coEvery {
            favoriteRepository.get()
        } returns listOf(data1)
        coEvery {
            searchRepository.searchItem(userRequest1)
        } returns UserMetaDataList(listOf(data1, data2))

        viewModel.search(userRequest1.seed)
        advanceTimeBy(300)
        viewModel.restore()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(UserUiData(true, data1), viewModel.favorites.value.first())
        val isFavoriteData =
            viewModel.searches.value
                .mapNotNull { it as? SearchTabMetaData }
                .find { it.data.data == data1 }?.data?.isFavorite ?: false
        assertTrue(isFavoriteData)
    }

    @Test
    fun `search emits tabs and scrollToTop, loading for first page`() = runTest {
        coEvery {
            searchRepository.searchItem(userRequest1)
        } returns UserMetaDataList(listOf(data1, data2))

        var isScrollToTop = false
        val scollJob = launch(testDispatcher) {
            viewModel.scrollToTop.collect {
                isScrollToTop = it
            }
        }

        var isLoading = false
        val loadingJob = launch(testDispatcher) {
            viewModel.loading.collect {
                isLoading = it
            }
        }

        viewModel.search(userRequest1.seed)
        advanceTimeBy(300)
        assertTrue(isLoading)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(isScrollToTop)
        assertFalse(isLoading)
        assertEquals(3, viewModel.searches.value.size)
        scollJob.cancel()
        loadingJob.cancel()
    }

    @Test
    fun `searchMore appends additional pages`() = runTest {
        coEvery {
            searchRepository.searchItem(userRequest1)
        } returns UserMetaDataList(listOf(data1, data2))
        coEvery {
            searchRepository.searchItem(userRequest2)
        } returns UserMetaDataList(listOf(data1, data2))
        coEvery { favoriteRepository.add(data1) } returns Unit

        viewModel.search(userRequest1.seed)
        advanceTimeBy(300)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(3, viewModel.searches.value.size)

        viewModel.searchMore(userRequest2.seed)
        advanceTimeBy(300)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(6, viewModel.searches.value.size)
    }

    @Test
    fun `searchCancel prevents paging`() = runTest {
        coEvery { searchRepository.searchItem(userRequest1) } coAnswers {
            throw CancellationException()
        }
        var exception: Exception? = null
        val job = launch(testDispatcher) {
            viewModel.error.collect {
                exception = it
            }
        }

        viewModel.search(userRequest1.seed)
        viewModel.searchCancel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(exception != null)
        job.cancel()
    }

    @Test
    fun `exception when searching`() = runTest {
        coEvery { searchRepository.searchItem(userRequest1) } coAnswers {
            throw UnknownHostException()
        }
        var exception: Exception? = null
        val job = launch(testDispatcher) {
            viewModel.error.collect {
                exception = it
            }
        }

        viewModel.search(userRequest1.seed)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(exception != null)
        job.cancel()
    }

    @Test
    fun `addFavoriteData updates searches and favorites`() = runTest {
        coEvery {
            searchRepository.searchItem(userRequest1)
        } returns UserMetaDataList(listOf(data1, data2))
        coEvery { favoriteRepository.add(data1) } returns Unit

        viewModel.search(userRequest1.seed)
        advanceTimeBy(300)

        val ui = UserUiData(false, data1)
        viewModel.addFavoriteData(ui)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { favoriteRepository.add(data1) }
        val isFavoriteData =
            viewModel.searches.value
                .mapNotNull { it as? SearchTabMetaData }
                .find { it.data.data == data1 }?.data?.isFavorite ?: false
        assertTrue(isFavoriteData)
        assertEquals(1, viewModel.favorites.value.size)
    }

    @Test
    fun `removeFavoriteData updates searches and favorites`() = runTest {
        coEvery {
            searchRepository.searchItem(userRequest1)
        } returns UserMetaDataList(listOf(data1, data2))
        coEvery { favoriteRepository.add(data1) } returns Unit
        coEvery { favoriteRepository.remove(data1) } returns Unit

        viewModel.search(userRequest1.seed)
        advanceTimeBy(300)

        val ui = UserUiData(true, data1)
        viewModel.removeFavoriteData(ui)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { favoriteRepository.remove(data1) }
        val isFavoriteData =
            viewModel.searches.value
                .mapNotNull { it as? SearchTabMetaData }
                .find { it.data.data == data1 }?.data?.isFavorite ?: false
        assertFalse(isFavoriteData)
        assertTrue(viewModel.favorites.value.isEmpty())
    }
}