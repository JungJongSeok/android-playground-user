package com.sample.android.ui.feature.main.model

import androidx.annotation.Keep

/**
 * Represents all possible user intents/actions in the Main screen
 */
@Keep
sealed interface MainIntent {

    /**
     * Initialize the screen by loading favorites
     */
    data object Initialize : MainIntent

    /**
     * Restore state after returning from detail screen
     */
    data object Restore : MainIntent

    /**
     * Perform search with given query
     */
    data class Search(val query: String) : MainIntent

    /**
     * Load more search results (pagination)
     */
    data class SearchMore(val query: String = "") : MainIntent

    /**
     * Add user to favorites
     */
    data class AddFavorite(val userUiData: UserUiData) : MainIntent

    /**
     * Remove user from favorites
     */
    data class RemoveFavorite(val userUiData: UserUiData) : MainIntent
}