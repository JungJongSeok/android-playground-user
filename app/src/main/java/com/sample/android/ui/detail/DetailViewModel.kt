package com.sample.android.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.android.repository.FavoriteRepository
import com.sample.android.ui.data.UserUiData
import com.sample.android.ui.data.like
import com.sample.android.ui.data.unlike
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class DetailViewModel(private val favoriteRepository: FavoriteRepository) : ViewModel() {
    private val _currentList = mutableListOf<UserUiData>()
    val currentList
        get() = _currentList.toList()
    private val _currentData = MutableSharedFlow<UserUiData>()
    val currentData = _currentData.asSharedFlow()

    private val _isChangedFavorite = MutableStateFlow(false)
    val isChangedFavorite = _isChangedFavorite.asStateFlow()

    fun setUiData(list: List<UserUiData>) {
        _currentList.clear()
        _currentList.addAll(list)
    }

    fun setCurrentData(position: Int) {
        viewModelScope.launch {
            val data = _currentList.getOrNull(position) ?: return@launch
            _currentData.emit(data)
        }
    }

    private val favoriteLock = AtomicBoolean(false)
    fun unlikeFavoriteData(userUiData: UserUiData) {
        viewModelScope.launch {
            if (favoriteLock.getAndSet(true)) {
                return@launch
            }
            favoriteRepository.remove(userUiData.data)

            val list = _currentList.unlike(userUiData)
            _currentList.clear()
            _currentList.addAll(list)

            val unlikedData = userUiData.copy(isFavorite = false)
            _currentData.emit(unlikedData)

            _isChangedFavorite.emit(true)
            favoriteLock.set(false)
        }
    }

    fun likeFavoriteData(userUiData: UserUiData) {
        viewModelScope.launch {
            if (favoriteLock.getAndSet(true)) {
                return@launch
            }
            favoriteRepository.add(userUiData.data)

            val list = _currentList.like(userUiData)
            _currentList.clear()
            _currentList.addAll(list)

            val likedData = userUiData.copy(isFavorite = true)
            _currentData.emit(likedData)

            _isChangedFavorite.emit(true)
            favoriteLock.set(false)
        }
    }
}