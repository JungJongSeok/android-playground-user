package com.sample.android.ui.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.android.network.request.UserRequest
import com.sample.android.repository.FavoriteRepository
import com.sample.android.repository.SearchRepository
import com.sample.android.ui.feature.main.model.MainEffect
import com.sample.android.ui.feature.main.model.MainIntent
import com.sample.android.ui.feature.main.model.MainState
import com.sample.android.ui.feature.main.model.SearchTabBorder
import com.sample.android.ui.feature.main.model.SearchTabData
import com.sample.android.ui.feature.main.model.SearchTabUiData
import com.sample.android.ui.feature.main.model.UserUiData
import com.sample.android.ui.feature.main.model.addUiData
import com.sample.android.ui.feature.main.model.like
import com.sample.android.ui.feature.main.model.removeUiData
import com.sample.android.ui.feature.main.model.unlike
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * MVI ViewModel for Main screen
 * Processes Intents, emits States and Effects
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    // State
    private val _state = MutableStateFlow(MainState.initial())
    val state = _state.asStateFlow()

    // Effect
    private val _effect = MutableSharedFlow<MainEffect>()
    val effect = _effect.asSharedFlow()

    // Search job for cancellation
    private var searchJob: Job? = null
    private val searchLock = AtomicBoolean(false)
    private val favoriteLock = AtomicBoolean(false)

    /**
     * Process user intents
     */
    fun processIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.Initialize -> handleInitialize()
            is MainIntent.Restore -> handleRestore()
            is MainIntent.Search -> handleSearch(intent.query)
            is MainIntent.SearchMore -> handleSearchMore(intent.query)
            is MainIntent.AddFavorite -> handleAddFavorite(intent.userUiData)
            is MainIntent.RemoveFavorite -> handleRemoveFavorite(intent.userUiData)
        }
    }

    private fun handleInitialize() {
        viewModelScope.launch {
            try {
                val data = favoriteRepository.get().map { data ->
                    UserUiData(true, data)
                }
                updateState { it.copy(favorites = data) }
            } catch (e: Exception) {
                _effect.emit(MainEffect.ShowError(e))
            }
        }
    }

    private fun handleRestore() {
        viewModelScope.launch {
            try {
                val favoriteList = favoriteRepository.get().map { data ->
                    UserUiData(true, data)
                }
                val favoriteSet = favoriteList.map { it.data }.toSet()
                val searchList = state.value.searches.map { search ->
                    if (search is SearchTabUiData) {
                        SearchTabUiData(
                            UserUiData(
                                favoriteSet.contains(search.data.data),
                                search.data.data
                            )
                        )
                    } else {
                        search
                    }
                }
                updateState {
                    it.copy(
                        favorites = favoriteList,
                        searches = searchList
                    )
                }
            } catch (e: Exception) {
                _effect.emit(MainEffect.ShowError(e))
            }
        }
    }

    private fun handleSearch(query: String) {
        viewModelScope.launch {
            searchJob?.cancelAndJoin()
            searchJob = launch job@{
                updateState { it.copy(isLoading = true) }
                delay(300)
                if (query.isBlank()) {
                    updateState { it.copy(isLoading = false) }
                    searchJob?.cancelAndJoin()
                    return@job
                }
                paging(query, 1)
                updateState { it.copy(isLoading = false) }
            }
        }
    }

    private fun handleSearchMore(query: String) {
        val currentState = state.value
        val searchQuery = query.ifBlank { currentState.query }

        if (currentState.isEnd || currentState.isLoadingMore) {
            return
        }

        searchJob = viewModelScope.launch {
            if (searchQuery.isBlank()) {
                return@launch
            }
            updateState { it.copy(isLoadingMore = true) }
            paging(searchQuery, currentState.currentPage)
            updateState { it.copy(isLoadingMore = false) }
        }
    }

    private suspend fun paging(query: String, currentPosition: Int) {
        withContext(Dispatchers.Unconfined) {
            try {
                if (searchLock.getAndSet(true)) {
                    return@withContext
                }

                val response = searchRepository.searchItem(UserRequest(query, currentPosition))
                val nextPage = currentPosition + 1
                val isEnd = response.users.isEmpty()
                val favoriteSet = state.value.favorites.map { it.data }.toSet()

                val list = response.users.map { data ->
                    UserUiData(favoriteSet.contains(data), data)
                }.map { SearchTabUiData(it) } + if (response.users.isEmpty()) {
                    SearchTabBorder("", true)
                } else {
                    SearchTabBorder(currentPosition.toString(), false)
                }

                updateState { currentState ->
                    if (currentPosition <= 1) {
                        viewModelScope.launch {
                            _effect.emit(MainEffect.ScrollToTop)
                        }
                        currentState.copy(
                            searches = list,
                            query = query,
                            currentPage = nextPage,
                            isEnd = isEnd
                        )
                    } else {
                        currentState.copy(
                            searches = currentState.searches + list,
                            currentPage = nextPage,
                            isEnd = isEnd
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _effect.emit(MainEffect.ShowError(e))
            } finally {
                searchLock.set(false)
            }
        }
    }

    private fun handleAddFavorite(userUiData: UserUiData) {
        viewModelScope.launch {
            if (favoriteLock.getAndSet(true)) {
                return@launch
            }
            try {
                favoriteRepository.add(userUiData.data)

                val searchList = state.value.searches.like(userUiData)
                val favoriteList = state.value.favorites.addUiData(
                    userUiData.copy(isFavorite = true)
                )

                updateState {
                    it.copy(
                        searches = searchList,
                        favorites = favoriteList
                    )
                }
            } catch (e: Exception) {
                _effect.emit(MainEffect.ShowError(e))
            } finally {
                favoriteLock.set(false)
            }
        }
    }

    private fun handleRemoveFavorite(userUiData: UserUiData) {
        viewModelScope.launch {
            if (favoriteLock.getAndSet(true)) {
                return@launch
            }
            try {
                favoriteRepository.remove(userUiData.data)

                val searchList = state.value.searches.unlike(userUiData)
                val favoriteList = state.value.favorites.removeUiData(userUiData)

                updateState {
                    it.copy(
                        searches = searchList,
                        favorites = favoriteList
                    )
                }
            } catch (e: Exception) {
                _effect.emit(MainEffect.ShowError(e))
            } finally {
                favoriteLock.set(false)
            }
        }
    }

    private fun updateState(update: (MainState) -> MainState) {
        _state.value = update(_state.value)
    }
}