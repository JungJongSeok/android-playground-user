package com.sample.android.ui.data

import androidx.annotation.Keep

@Keep
data class SearchTabMetaData(
    val data: UserUiData
) : SearchTabData

@Keep
data class SearchTabBorder(
    val text: String,
    val isEnd: Boolean
) : SearchTabData

sealed interface SearchTabData

fun List<SearchTabData>.like(userUiData: UserUiData): List<SearchTabData> {
    return changeFavoriteStatus(userUiData, true)
}

fun List<SearchTabData>.unlike(userUiData: UserUiData): List<SearchTabData> {
    return changeFavoriteStatus(userUiData, false)
}

private fun List<SearchTabData>.changeFavoriteStatus(userUiData: UserUiData, isFavorite: Boolean): List<SearchTabData> {
    return this.map { search ->
        if (search is SearchTabMetaData && search.data == userUiData) {
            search.copy(data = search.data.copy(isFavorite = isFavorite))
        } else {
            search
        }
    }
}