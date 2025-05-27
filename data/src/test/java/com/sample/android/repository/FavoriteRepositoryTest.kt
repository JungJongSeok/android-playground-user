package com.sample.android.repository

import com.sample.android.data.UserMetaData
import com.sample.android.utils.PreferencesModule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class FavoriteRepositoryTest {
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

    private lateinit var dispatcher: CoroutineDispatcher
    private lateinit var fakePrefs: PreferencesModule
    private lateinit var repository: FavoriteRepositoryImpl

    private class FakePreferencesModule : PreferencesModule {
        private val _backing = mutableListOf<UserMetaData>()
        override var favorites: List<UserMetaData>
            get() = _backing.toList()
            set(value) {
                _backing.clear()
                _backing.addAll(value)
            }
    }

    @Before
    fun setUp() {
        dispatcher = Dispatchers.Unconfined
        fakePrefs = FakePreferencesModule()
        repository = FavoriteRepositoryImpl(
            dispatcher = dispatcher,
            preferencesModule = fakePrefs
        )
    }

    @Test
    fun `get returns empty list initially`() = runBlocking {
        val result = repository.get()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `add and get returns the added item`() = runBlocking {
        repository.add(data1)
        val result = repository.get()

        assertEquals(1, result.size)
        assertEquals(data1, result[0])
    }

    @Test
    fun `remove deletes only matching item`() = runBlocking {
        fakePrefs.favorites = listOf(data1, data2)

        repository.remove(data1)
        val result = repository.get()

        assertEquals(1, result.size)
        assertEquals(data2, result[0])
    }

    @Test
    fun `remove non-existing item leaves list unchanged`() = runBlocking {
        fakePrefs.favorites = listOf(data1)
        repository.remove(data2)
        val result = repository.get()

        assertEquals(1, result.size)
        assertEquals(data1, result[0])
    }

    @Test
    fun `add multiple and then remove one`() = runBlocking {
        repository.add(data1)
        repository.add(data2)
        var result = repository.get()
        assertEquals(2, result.size)

        repository.remove(data1)
        result = repository.get()
        assertEquals(1, result.size)
        assertEquals(data2, result[0])
    }
}