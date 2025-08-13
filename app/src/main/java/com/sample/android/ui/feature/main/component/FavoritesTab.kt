package com.sample.android.ui.feature.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sample.android.data.UserMetaData
import com.sample.android.ui.feature.main.model.UserUiData
import com.sample.android.ui.theme.CommonTheme


@Composable
fun FavoritesTab(
    favorites: List<UserUiData>,
    gridState: LazyGridState,
    removeFavoriteTask: (UserUiData) -> Unit,
    startDetailActivity: (List<UserUiData>, Int) -> Unit
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 30.dp, vertical = 40.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        itemsIndexed(favorites) { _, item ->
            FavoriteItemRow(
                item = item,
                onFavoriteToggle = { data ->
                    if (data.isFavorite) {
                        removeFavoriteTask.invoke(data)
                    }
                },
                onClick = { data ->
                    val position = maxOf(favorites.indexOfFirst { it == data }, 0)
                    startDetailActivity.invoke(favorites, position)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoriteTabPreview() {
    val data1 = UserUiData(
        isFavorite = true,
        data = UserMetaData(
            thumbnail = "thumbnail1",
            title = "title1",
            url = "http://example.com/1",
            datetime = "2025-05-18T12:00:00.000+09:00"
        )
    )
    val data2 = UserUiData(
        isFavorite = false,
        data = UserMetaData(
            thumbnail = "thumbnail2",
            title = "title2",
            url = "http://example.com/2",
            datetime = "2025-05-18T13:00:00.000+09:00"
        )
    )
    val data3 = UserUiData(
        isFavorite = true,
        data = UserMetaData(
            thumbnail = "thumbnail3",
            title = "title3",
            url = "http://example.com/3",
            datetime = "2025-05-18T14:00:00.000+09:00"
        )
    )

    CommonTheme {
        FavoritesTab(
            gridState = LazyGridState(),
            favorites = listOf(data1, data2, data3),
            removeFavoriteTask = {},
            startDetailActivity = { _, _ -> }
        )
    }
}