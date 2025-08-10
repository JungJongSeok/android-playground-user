package com.sample.android.ui.feature.main.model

import androidx.annotation.Keep

@Keep
sealed interface MainIntent {

    data object Initialize : MainIntent

    data object Restore : MainIntent

    data class Search(val query: String) : MainIntent

    data class SearchMore(val query: String = "") : MainIntent

    data class AddFavorite(val userUiData: UserUiData) : MainIntent

    data class RemoveFavorite(val userUiData: UserUiData) : MainIntent
}