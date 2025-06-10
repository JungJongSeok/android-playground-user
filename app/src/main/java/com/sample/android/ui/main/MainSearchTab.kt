package com.sample.android.ui.main

import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.sample.android.R
import com.sample.android.network.UserServiceImpl
import com.sample.android.repository.FavoriteRepositoryImpl
import com.sample.android.repository.SearchRepositoryImpl
import com.sample.android.ui.data.SearchTabBorder
import com.sample.android.ui.data.SearchTabData
import com.sample.android.ui.data.SearchTabMetaData
import com.sample.android.ui.data.UserUiData
import com.sample.android.ui.extension.setTimeText
import com.sample.android.ui.theme.ColorBlack22
import com.sample.android.ui.theme.ColorBlack44
import com.sample.android.ui.theme.ColorBlack88
import com.sample.android.ui.theme.ColorBlackE6
import com.sample.android.ui.theme.ColorBlackF7
import com.sample.android.ui.theme.CommonTheme
import com.sample.android.utils.PreferencesModuleImpl


@Composable
fun SearchTab(
    viewModel: MainViewModel,
    searches: List<SearchTabData>,
    listState: LazyListState,
    query: String,
    onValueChange: (String) -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loading
            .collect {
                isLoading = it
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(20.dp))
        SearchBar(
            value = query,
            onValueChange = { text ->
                onValueChange.invoke(text)
                viewModel.search(text)
            },
            hintText = context.getString(R.string.search_hint_text)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                itemsIndexed(searches) { index, item ->
                    when (item) {
                        is SearchTabMetaData -> Column {
                            if (index == 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            SearchListItem(item.data,
                                onFavoriteToggle = {
                                    if (it.isFavorite) {
                                        viewModel.removeFavoriteData(it)
                                    } else {
                                        viewModel.addFavoriteData(it)
                                    }
                                },
                                onClick = {
                                    viewModel.startDetailActivity(listOf(it))
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
                                    context.getString(R.string.search_end_text)
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
                                        modifier = Modifier.fillMaxWidth().height(100.dp),
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
                    modifier = Modifier.fillMaxWidth().height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ColorBlack22)
                }
            }
        }
    }
}

@Composable
fun SearchListItem(
    item: UserUiData,
    onFavoriteToggle: (UserUiData) -> Unit,
    onClick: (UserUiData) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick(item) })
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.CenterVertically)
                .border(
                    border = BorderStroke(width = 1.dp, color = ColorBlackE6),
                    shape = RoundedCornerShape(14.0.dp)
                )
                .clip(RoundedCornerShape(14.0.dp))
        ) {
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { imageView ->
                    Glide.with(imageView.context)
                        .load(item.data.thumbnail ?: return@AndroidView)
                        .placeholder(ColorDrawable(ColorBlackE6.toArgb()))
                        .into(imageView)
                }
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onFavoriteToggle(item) },
                    )
            ) {
                Icon(
                    painter = painterResource(
                        id = if (item.isFavorite) {
                            R.drawable.icon_like_on
                        } else {
                            R.drawable.icon_like_off
                        }
                    ),
                    contentDescription = "favorite like button",
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .size(22.dp),
                    tint = Color.Unspecified
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = item.data.title ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.data.url ?: "",
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = ColorBlack44,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.data.timestamp.setTimeText(context, R.string.pattern_datetime_full),
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = ColorBlack88,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    hintText: String = ""
) {
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
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = ColorBlack22,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    cursorBrush = SolidColor(ColorBlack22),
                    modifier = Modifier.fillMaxWidth()
                ) { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = hintText,
                            color = ColorBlack88,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                    inner()
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (value.isNotEmpty()) {
                Icon(
                    painter = painterResource(R.drawable.icon_delete),
                    contentDescription = "search bar delete",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onValueChange("") },
                        ),
                )
            }
            Spacer(modifier = Modifier.width(18.dp))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainSearchTabPreview() {
    CommonTheme {
        SearchTab(
            MainViewModel(
                SearchRepositoryImpl(userService = UserServiceImpl()),
                FavoriteRepositoryImpl(preferencesModule = PreferencesModuleImpl(LocalContext.current))
            ),
            listState = LazyListState(),
            query = "",
            searches = emptyList(),
            onValueChange = {}
        )
    }
}