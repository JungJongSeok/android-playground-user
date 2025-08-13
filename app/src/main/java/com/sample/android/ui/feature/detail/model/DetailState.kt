package com.sample.android.ui.feature.detail.model

import com.sample.android.ui.feature.main.model.UserUiData

data class DetailState(
    val selectedList: List<UserUiData> = emptyList(),
    val currentData: UserUiData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)