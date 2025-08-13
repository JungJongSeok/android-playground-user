package com.sample.android.ui.feature.detail.model

import com.sample.android.ui.feature.main.model.UserUiData

sealed interface DetailIntent {
    data class Initialize(val selectedList: List<UserUiData>) : DetailIntent
    data class SetCurrentPosition(val position: Int) : DetailIntent
    data class ToggleFavorite(val userUiData: UserUiData) : DetailIntent
}