package com.sample.android.ui.feature.detail.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sample.android.ui.model.UserUiData

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun DetailImagePager(
    selectedList: List<UserUiData>,
    pagerState: PagerState
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val item = selectedList.getOrNull(page)
        item?.let { userUiData ->
            DetailImageItem(userUiData = userUiData)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun DetailImageItem(userUiData: UserUiData) {
    GlideImage(
        model = userUiData.thumbnail,
        contentDescription = userUiData.title,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}