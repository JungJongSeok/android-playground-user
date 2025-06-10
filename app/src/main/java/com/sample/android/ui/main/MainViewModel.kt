package com.sample.android.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.android.network.request.UserRequest
import com.sample.android.repository.FavoriteRepository
import com.sample.android.repository.SearchRepository
import com.sample.android.ui.data.SearchTabBorder
import com.sample.android.ui.data.SearchTabData
import com.sample.android.ui.data.SearchTabMetaData
import com.sample.android.ui.data.UserUiData
import com.sample.android.ui.data.addUiData
import com.sample.android.ui.data.like
import com.sample.android.ui.data.removeUiData
import com.sample.android.ui.data.unlike
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel(
    private val searchRepository: SearchRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    private val _searches = MutableStateFlow<List<SearchTabData>>(emptyList())
    val searches = _searches.asStateFlow()

    private val _favorites = MutableStateFlow<List<UserUiData>>(emptyList())
    val favorites = _favorites.asStateFlow()

    private val _loading = MutableSharedFlow<Boolean>()
    val loading = _loading.asSharedFlow()

    private val _scrollToTop = MutableSharedFlow<Boolean>()
    val scrollToTop = _scrollToTop.asSharedFlow()

    private val _error = MutableSharedFlow<Exception>()
    val error = _error.asSharedFlow()

    private val _detailActivity = MutableSharedFlow<Pair<List<UserUiData>, Int>>()
    val detailActivity = _detailActivity.asSharedFlow()

    private var _query = ""
    private var _currentPage = 1
    private var _isEnd = false

    fun initialize() {
        viewModelScope.launch {
            try {
                val data = favoriteRepository.get().map { data ->
                    UserUiData(true, data)
                }
                _favorites.emit(data)
            } catch (e: Exception) {
                _error.emit(e)
            }
        }
    }

    fun restore() {
        viewModelScope.launch {
            try {
                val favoriteList = favoriteRepository.get().map { data ->
                    UserUiData(true, data)
                }
                _favorites.emit(favoriteList)

                val favoriteSet = favoriteList.map { it.data }.toSet()
                val searchList = searches.value.map { search ->
                    if (search is SearchTabMetaData) {
                        SearchTabMetaData(
                            UserUiData(
                                favoriteSet.contains(search.data.data),
                                search.data.data
                            )
                        )
                    } else {
                        search
                    }
                }
                _searches.emit(searchList)
            } catch (e: Exception) {
                _error.emit(e)
            }
        }
    }

    private var searchJob: Job? = null
    fun search(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _loading.emit(true)
            delay(300)
            if (query.isBlank()) {
                _loading.emit(false)
                searchJob?.cancel()
                return@launch
            }
            paging(query, 1)
            _loading.emit(false)
        }
    }

    fun searchMore(query: String = _query) {
        if (_isEnd) {
            return
        }
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                return@launch
            }
            paging(query, _currentPage)
        }
    }

    fun searchCancel() {
        searchJob?.cancel()
    }

    private val searchLock = AtomicBoolean(false)
    private suspend fun paging(query: String, currentPosition: Int) {
        withContext(Dispatchers.Unconfined) {
            try {
                if (searchLock.getAndSet(true)) {
                    return@withContext
                }
                _query = query
                val response = searchRepository.searchItem(UserRequest(query, currentPosition))
                _currentPage = currentPosition + 1
                _isEnd = response.users.isEmpty()
                val favoriteSet = favorites.value.map { it.data }.toSet()
                val list = response.users.map { data ->
                    UserUiData(favoriteSet.contains(data), data)
                }.map { SearchTabMetaData(it) } + if (response.users.isEmpty()) {
                    SearchTabBorder("", true)
                } else {
                    SearchTabBorder(currentPosition.toString(), false)
                }
                if (currentPosition <= 1) {
                    _searches.emit(list)
                    _scrollToTop.emit(true)
                } else {
                    _searches.emit(_searches.value + list)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.emit(e)
            } finally {
                searchLock.set(false)
            }
        }
    }

    private val favoriteLock = AtomicBoolean(false)
    fun addFavoriteData(userUiData: UserUiData) {
        viewModelScope.launch {
            if (favoriteLock.getAndSet(true)) {
                return@launch
            }
            favoriteRepository.add(userUiData.data)

            val searchList = searches.value.like(userUiData)
            _searches.emit(searchList)

            val favoriteList = favorites.value.addUiData(
                userUiData.copy(isFavorite = true)
            )
            _favorites.emit(favoriteList)
            favoriteLock.set(false)
        }
    }

    fun removeFavoriteData(userUiData: UserUiData) {
        viewModelScope.launch {
            if (favoriteLock.getAndSet(true)) {
                return@launch
            }
            favoriteRepository.remove(userUiData.data)

            val searchList = searches.value.unlike(userUiData)
            _searches.emit(searchList)

            val favoriteList = favorites.value.removeUiData(userUiData)
            _favorites.emit(favoriteList)
            favoriteLock.set(false)
        }
    }

    fun startDetailActivity(list: List<UserUiData>, position: Int = 0) {
        viewModelScope.launch {
            _detailActivity.emit(Pair(list, position))
        }
    }
}