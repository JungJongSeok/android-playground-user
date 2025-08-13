package com.sample.android.ui.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.android.data.mapper.toUser
import com.sample.android.data.mapper.toUserMetaData
import com.sample.android.data.model.UserMetaData
import com.sample.android.domain.usecase.AddToFavoritesUseCase
import com.sample.android.domain.usecase.GetFavoritesUseCase
import com.sample.android.domain.usecase.RemoveFromFavoritesUseCase
import com.sample.android.domain.usecase.SearchUsersUseCase
import com.sample.android.ui.feature.main.model.MainEffect
import com.sample.android.ui.feature.main.model.MainIntent
import com.sample.android.ui.feature.main.model.MainState
import com.sample.android.ui.feature.main.model.SearchTabBorder
import com.sample.android.ui.feature.main.model.SearchTabData
import com.sample.android.ui.feature.main.model.SearchTabUiData
import com.sample.android.ui.feature.main.model.like
import com.sample.android.ui.feature.main.model.unlike
import com.sample.android.ui.mapper.toUiData
import com.sample.android.ui.model.UserUiData
import com.sample.android.ui.model.addUiData
import com.sample.android.ui.model.removeUiData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainState.initial())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<MainEffect>()
    val effect = _effect.asSharedFlow()

    private var searchJob: Job? = null
    private val searchLock = AtomicBoolean(false)
    private val favoriteLock = AtomicBoolean(false)

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
                val favorites = getFavoritesUseCase()
                val data = favorites.map { user ->
                    user.toUserMetaData().toUiData(isFavorite = true)
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
                val favorites = getFavoritesUseCase()
                val favoriteList = favorites.map { user ->
                    user.toUserMetaData().toUiData(isFavorite = true)
                }
                val favoriteSet = favoriteList.map {
                    UserMetaData(
                        title = it.title,
                        thumbnail = it.thumbnail,
                        url = it.url,
                        datetime = it.datetime
                    )
                }.toSet()
                val searchList = state.value.searches.map { search ->
                    if (search is SearchTabUiData) {
                        val searchUserMetaData = UserMetaData(
                            title = search.data.title,
                            thumbnail = search.data.thumbnail,
                            url = search.data.url,
                            datetime = search.data.datetime
                        )
                        SearchTabUiData(
                            search.data.copy(
                                isFavorite = favoriteSet.contains(searchUserMetaData)
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

                val searchResult = searchUsersUseCase(query, currentPosition)
                val nextPage = currentPosition + 1
                val isEnd = searchResult.users.isEmpty()
                val favorites = getFavoritesUseCase()
                val favoriteSet = favorites.toSet()

                val searchItems = searchResult.users.map { user ->
                    val metaData = user.toUserMetaData()
                    metaData.toUiData(isFavorite = favoriteSet.contains(user))
                }.map { SearchTabUiData(it) }

                val borderItem = if (searchResult.users.isEmpty()) {
                    SearchTabBorder("", true)
                } else {
                    SearchTabBorder(currentPosition.toString(), false)
                }

                val list: List<SearchTabData> = searchItems + listOf(borderItem)

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
                val userMetaData = UserMetaData(
                    title = userUiData.title,
                    thumbnail = userUiData.thumbnail,
                    url = userUiData.url,
                    datetime = userUiData.datetime
                )
                val user = userMetaData.toUser()
                addToFavoritesUseCase(user)

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
                val userMetaData = UserMetaData(
                    title = userUiData.title,
                    thumbnail = userUiData.thumbnail,
                    url = userUiData.url,
                    datetime = userUiData.datetime
                )
                val user = userMetaData.toUser()
                removeFromFavoritesUseCase(user)

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