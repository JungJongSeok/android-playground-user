package com.sample.android.ui.feature.detail.model

sealed interface DetailEffect {
    data object FavoriteChanged : DetailEffect
    data class ShowError(val message: String?) : DetailEffect
}