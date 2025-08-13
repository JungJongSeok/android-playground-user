package com.sample.android.ui.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.android.domain.usecase.AddToFavoritesUseCase
import com.sample.android.domain.usecase.RemoveFromFavoritesUseCase
import com.sample.android.data.mapper.toUser
import com.sample.android.ui.feature.detail.model.DetailEffect
import com.sample.android.ui.feature.detail.model.DetailIntent
import com.sample.android.ui.feature.detail.model.DetailState
import com.sample.android.ui.model.UserUiData
import com.sample.android.ui.model.like
import com.sample.android.ui.model.unlike
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state = _state.asStateFlow()

    private val _effect = MutableStateFlow<DetailEffect?>(null)
    val effect = _effect.asStateFlow()

    private val favoriteLock = AtomicBoolean(false)

    fun handleIntent(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.Initialize -> initializeData(intent.selectedList)
            is DetailIntent.SetCurrentPosition -> setCurrentPosition(intent.position)
            is DetailIntent.ToggleFavorite -> toggleFavorite(intent.userUiData)
        }
    }

    private fun initializeData(selectedList: List<UserUiData>) {
        _state.value = _state.value.copy(
            selectedList = selectedList,
            currentData = selectedList.firstOrNull()
        )
    }

    private fun setCurrentPosition(position: Int) {
        val currentData = _state.value.selectedList.getOrNull(position)
        _state.value = _state.value.copy(currentData = currentData)
    }

    private fun toggleFavorite(userUiData: UserUiData) {
        if (userUiData.isFavorite) {
            unlikeFavorite(userUiData)
        } else {
            likeFavorite(userUiData)
        }
    }

    private fun unlikeFavorite(userUiData: UserUiData) {
        viewModelScope.launch {
            if (favoriteLock.getAndSet(true)) {
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true)

            try {
                val user = userUiData.data.toUser()
                removeFromFavoritesUseCase(user)

                val updatedList = _state.value.selectedList.unlike(userUiData)
                val updatedCurrentData = userUiData.copy(isFavorite = false)

                _state.value = _state.value.copy(
                    selectedList = updatedList,
                    currentData = updatedCurrentData,
                    isLoading = false
                )

                _effect.value = DetailEffect.FavoriteChanged
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
                _effect.value = DetailEffect.ShowError(e.message)
            } finally {
                favoriteLock.set(false)
            }
        }
    }

    private fun likeFavorite(userUiData: UserUiData) {
        viewModelScope.launch {
            if (favoriteLock.getAndSet(true)) {
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true)

            try {
                val user = userUiData.data.toUser()
                addToFavoritesUseCase(user)

                val updatedList = _state.value.selectedList.like(userUiData)
                val updatedCurrentData = userUiData.copy(isFavorite = true)

                _state.value = _state.value.copy(
                    selectedList = updatedList,
                    currentData = updatedCurrentData,
                    isLoading = false
                )

                _effect.value = DetailEffect.FavoriteChanged
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
                _effect.value = DetailEffect.ShowError(e.message)
            } finally {
                favoriteLock.set(false)
            }
        }
    }

    fun clearEffect() {
        _effect.value = null
    }
}