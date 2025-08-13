package com.sample.android.ui.feature.detail

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.sample.android.R
import com.sample.android.ui.feature.detail.component.DetailAppBar
import com.sample.android.ui.feature.detail.component.DetailImagePager
import com.sample.android.ui.feature.detail.model.DetailEffect
import com.sample.android.ui.feature.detail.model.DetailIntent
import com.sample.android.ui.feature.main.model.UserUiData

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailRouter(
    selectedList: List<UserUiData>,
    initialPosition: Int,
    onBackClick: () -> Unit,
    onFavoriteChanged: (Boolean) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val effect by viewModel.effect.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = initialPosition,
        pageCount = { selectedList.size }
    )

    LaunchedEffect(selectedList) {
        viewModel.handleIntent(DetailIntent.Initialize(selectedList))
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.handleIntent(DetailIntent.SetCurrentPosition(pagerState.currentPage))
    }

    LaunchedEffect(effect) {
        when (effect) {
            is DetailEffect.FavoriteChanged -> {
                onFavoriteChanged(true)
                viewModel.clearEffect()
            }

            is DetailEffect.ShowError -> {
                Toast.makeText(
                    context,
                    (effect as DetailEffect.ShowError).message
                        ?: context.getString(R.string.common_network_error),
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.clearEffect()
            }

            null -> {
                /* No effect */
            }
        }
    }

    DetailScreen(
        selectedList = state.selectedList,
        pagerState = pagerState,
        currentData = state.currentData,
        isLoading = state.isLoading,
        onBackClick = onBackClick,
        onFavoriteClick = { data ->
            viewModel.handleIntent(DetailIntent.ToggleFavorite(data))
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    selectedList: List<UserUiData>,
    pagerState: PagerState,
    currentData: UserUiData?,
    isLoading: Boolean = false,
    onBackClick: () -> Unit,
    onFavoriteClick: (UserUiData) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        DetailAppBar(
            currentData = currentData,
            onBackClick = onBackClick,
            onFavoriteClick = onFavoriteClick
        )

        DetailImagePager(
            selectedList = selectedList,
            pagerState = pagerState
        )
    }
}