package com.sample.android.ui.feature.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sample.android.R
import com.sample.android.data.UserMetaData
import com.sample.android.ui.feature.main.model.SearchTabBorder
import com.sample.android.ui.feature.main.model.SearchTabData
import com.sample.android.ui.feature.main.model.SearchTabUiData
import com.sample.android.ui.feature.main.model.UserUiData
import com.sample.android.ui.theme.ColorBlack22
import com.sample.android.ui.theme.ColorBlack88
import com.sample.android.ui.theme.ColorBlackE6
import com.sample.android.ui.theme.ColorBlackF7
import com.sample.android.ui.theme.CommonTheme


@Composable
fun SearchTab(
    query: String,
    searches: List<SearchTabData>,
    listState: LazyListState,
    isLoading: Boolean,
    searchTask: (String) -> Unit,
    onValueChangeTask: (String) -> Unit,
    addFavoriteTask: (UserUiData) -> Unit,
    removeFavoriteTask: (UserUiData) -> Unit,
    startDetailActivity: (List<UserUiData>) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .height(54.dp)
                .background(
                    color = ColorBlackF7,
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.width(20.dp))
                Icon(
                    painter = painterResource(R.drawable.icon_search),
                    contentDescription = "search bar delete",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = query,
                        onValueChange = { text ->
                            onValueChangeTask.invoke(text)
                            searchTask.invoke(text)
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            color = ColorBlack22,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        cursorBrush = SolidColor(ColorBlack22),
                        modifier = Modifier.fillMaxWidth()
                    ) { inner ->
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_hint_text),
                                color = ColorBlack88,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                            )
                        }
                        inner()
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (query.isNotEmpty()) {
                    Icon(
                        painter = painterResource(R.drawable.icon_delete),
                        contentDescription = "search bar delete",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onValueChangeTask.invoke("")
                                    searchTask.invoke("")
                                },
                            ),
                    )
                }
                Spacer(modifier = Modifier.width(18.dp))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                itemsIndexed(searches) { index, item ->
                    when (item) {
                        is SearchTabUiData -> Column {
                            if (index == 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            SearchItemRow(
                                item.data,
                                onFavoriteToggle = {
                                    if (it.isFavorite) {
                                        removeFavoriteTask.invoke(it)
                                    } else {
                                        addFavoriteTask.invoke(it)
                                    }
                                },
                                onClick = {
                                    startDetailActivity.invoke(listOf(it))
                                })
                        }

                        is SearchTabBorder -> Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (!item.isEnd) {
                                    item.text
                                } else {
                                    stringResource(R.string.search_end_text)
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            if (!item.isEnd) {
                                HorizontalDivider(
                                    color = ColorBlackE6,
                                    thickness = 1.dp
                                )
                                if (index == searches.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = ColorBlack22)
                                    }
                                }
                            }
                        }

                        else -> error("Unknown item type: $item")
                    }
                }
            }
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ColorBlack22)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchTabPreview() {
    val data1 = SearchTabUiData(
        UserUiData(
            isFavorite = true,
            data = UserMetaData(
                thumbnail = "thumbnail1",
                title = "title1",
                url = "http://example.com/1",
                datetime = "2025-05-18T12:00:00.000+09:00"
            )
        )
    )
    val data2 = SearchTabUiData(
        UserUiData(
            isFavorite = false,
            data = UserMetaData(
                thumbnail = "thumbnail2",
                title = "title2",
                url = "http://example.com/2",
                datetime = "2025-05-18T13:00:00.000+09:00"
            )
        )
    )
    val data3 = SearchTabUiData(
        UserUiData(
            isFavorite = true,
            data = UserMetaData(
                thumbnail = "thumbnail3",
                title = "title3",
                url = "http://example.com/3",
                datetime = "2025-05-18T14:00:00.000+09:00"
            )
        )
    )
    val border1 = SearchTabBorder(text = "1", isEnd = false)
    val border2 = SearchTabBorder(text = "2", isEnd = true)

    CommonTheme {
        SearchTab(
            query = "",
            searches = listOf(data1, data2, border1, data3, border2),
            listState = LazyListState(),
            isLoading = false,
            searchTask = {},
            onValueChangeTask = {},
            addFavoriteTask = {},
            removeFavoriteTask = {},
            startDetailActivity = {}
        )
    }
}