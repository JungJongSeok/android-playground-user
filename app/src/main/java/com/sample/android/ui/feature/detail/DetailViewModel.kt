package com.sample.android.ui.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.android.domain.usecase.AddToFavoritesUseCase
import com.sample.android.domain.usecase.RemoveFromFavoritesUseCase
import com.sample.android.mapper.toUser
import com.sample.android.ui.feature.main.model.UserUiData
import com.sample.android.ui.feature.main.model.like
import com.sample.android.ui.feature.main.model.unlike
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase
) : ViewModel() {
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
            try {
                val user = userUiData.data.toUser()
                removeFromFavoritesUseCase(user)

                val list = _currentList.unlike(userUiData)
                _currentList.clear()
                _currentList.addAll(list)

                val unlikedData = userUiData.copy(isFavorite = false)
                _currentData.emit(unlikedData)

                _isChangedFavorite.emit(true)
            } finally {
                favoriteLock.set(false)
            }
        }
    }

    fun likeFavoriteData(userUiData: UserUiData) {
        viewModelScope.launch {
            if (favoriteLock.getAndSet(true)) {
                return@launch
            }
            try {
                val user = userUiData.data.toUser()
                addToFavoritesUseCase(user)

                val list = _currentList.like(userUiData)
                _currentList.clear()
                _currentList.addAll(list)

                val likedData = userUiData.copy(isFavorite = true)
                _currentData.emit(likedData)

                _isChangedFavorite.emit(true)
            } finally {
                favoriteLock.set(false)
            }
        }
    }
}