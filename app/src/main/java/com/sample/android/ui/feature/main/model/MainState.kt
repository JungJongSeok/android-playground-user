package com.sample.android.ui.feature.main.model

import androidx.annotation.Keep

/**
 * Represents the complete UI state for the Main screen
 */
@Keep
data class MainState(
    val searches: List<SearchTabData> = emptyList(),
    val favorites: List<UserUiData> = emptyList(),
    val query: String = "",
    val currentPage: Int = 1,
    val isEnd: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false
) {
    companion object {
        fun initial() = MainState()
    }
}