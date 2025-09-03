package com.sample.android.ui.feature.detail.model

import androidx.compose.runtime.Stable
import com.sample.android.ui.model.UserUiData

@Stable
data class DetailState(
    val selectedList: List<UserUiData> = emptyList(),
    val currentData: UserUiData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)