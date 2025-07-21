package com.sample.android.ui.feature.main.model

import androidx.annotation.Keep

/**
 * Represents all possible side effects in the Main screen
 */
@Keep
sealed interface MainEffect {

    /**
     * Show error message to user
     */
    data class ShowError(val exception: Exception) : MainEffect

    /**
     * Scroll to top of search list
     */
    data object ScrollToTop : MainEffect
}