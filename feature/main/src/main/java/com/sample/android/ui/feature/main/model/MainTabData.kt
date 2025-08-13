package com.sample.android.ui.feature.main.model

import androidx.annotation.StringRes
import com.sample.android.feature.main.R

enum class MainTab(val index: Int, @StringRes val titleRes: Int) {
    SEARCH(0, R.string.main_tab_search),
    FAVORITE(1, R.string.main_tab_favorite)
}