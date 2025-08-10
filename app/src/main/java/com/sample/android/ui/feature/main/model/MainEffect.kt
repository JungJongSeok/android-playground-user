package com.sample.android.ui.feature.main.model

import androidx.annotation.Keep

@Keep
sealed interface MainEffect {
    data class ShowError(val exception: Exception) : MainEffect

    data object ScrollToTop : MainEffect
}