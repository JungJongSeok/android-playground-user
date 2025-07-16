package com.sample.android.ui.feature.main

import android.app.Activity.RESULT_OK
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sample.android.network.NetworkCommonException
import com.sample.android.ui.feature.detail.DetailActivity
import com.sample.android.ui.feature.detail.model.DetailExtraData
import com.sample.android.ui.feature.main.coponent.FavoritesTab
import com.sample.android.ui.feature.main.coponent.SearchTab
import com.sample.android.ui.feature.main.model.MainTab
import com.sample.android.ui.feature.main.model.SearchTabData
import com.sample.android.ui.feature.main.model.UserUiData
import com.sample.android.ui.theme.ColorBlack22
import com.sample.android.ui.theme.ColorBlack88
import com.sample.android.ui.theme.ColorBlackDD
import com.sample.android.ui.theme.CommonTheme

@Composable
fun MainRoute(viewModel: MainViewModel = hiltViewModel()) {
    val searches by viewModel.searches.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val searchListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }
    val favoriteGridState = rememberSaveable(saver = LazyGridState.Saver) {
        LazyGridState()
    }
    val tabs = listOf(MainTab.SEARCH, MainTab.FAVORITE)
    var selectedTab by rememberSaveable { mutableIntStateOf(MainTab.SEARCH.index) }
    var query by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.error
            .collect {
                if (it is NetworkCommonException) {
                    val message = it.message ?: it.code.toString()
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    val message = it.message ?: return@collect
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    LaunchedEffect(searchListState) {
        snapshotFlow {
            val lastVisible = searchListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val totalItems = searchListState.layoutInfo.totalItemsCount
            lastVisible to totalItems
        }.collect { (lastVisible, total) ->
            if (lastVisible != null && lastVisible >= total - 1) {
                viewModel.searchMore()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.scrollToTop
            .collect {
                searchListState.scrollToItem(0, 0)
            }
    }

    LaunchedEffect(Unit) {
        viewModel.loading
            .collect {
                isLoading = it
            }
    }

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        MainScreen(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            query = query,
            onQueryChange = { query = it },
            searches = searches,
            favorites = favorites,
            searchListState = searchListState,
            favoriteGridState = favoriteGridState,
            isLoading = isLoading,
            onSearch = { viewModel.search(it) },
            onAddFavorite = { viewModel.addFavoriteData(it) },
            onRemoveFavorite = { viewModel.removeFavoriteData(it) },
            tabs = tabs,
            startDetailActivity = { list, position ->
                val intent = DetailActivity.intent(context, list, position)
                val activity = context as? ComponentActivity ?: return@MainScreen
                activity.activityResultRegistry
                    .register(
                        DetailExtraData.KEY_DETAIL_ACTIVITY_RESULT,
                        ActivityResultContracts.StartActivityForResult()
                    ) {
                        if (it.resultCode == RESULT_OK) {
                            viewModel.restore()
                        }
                    }.launch(intent)
            }
        )
    }
}

@Composable
fun MainScreen(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    searches: List<SearchTabData>,
    favorites: List<UserUiData>,
    searchListState: LazyListState,
    favoriteGridState: LazyGridState,
    isLoading: Boolean,
    onSearch: (String) -> Unit,
    onAddFavorite: (UserUiData) -> Unit,
    onRemoveFavorite: (UserUiData) -> Unit,
    tabs: List<MainTab>,
    startDetailActivity: (List<UserUiData>, Int) -> Unit
) {
    Column {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            divider = {
                HorizontalDivider(color = ColorBlackDD, thickness = 0.5.dp)
            },
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .height(2.dp),
                    color = ColorBlack22
                )
            },
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    modifier = Modifier.height(70.dp),
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White,
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(11.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                text = stringResource(tab.titleRes),
                                fontSize = 16.sp,
                                fontWeight = if (index == selectedTab) FontWeight.Bold else FontWeight.Normal,
                                color = if (index == selectedTab) ColorBlack22 else ColorBlack88,
                            )
                        }
                    }
                )
            }
        }
        when (selectedTab) {
            MainTab.SEARCH.index -> SearchTab(
                query = query,
                searches = searches,
                listState = searchListState,
                isLoading = isLoading,
                searchTask = onSearch,
                onValueChangeTask = onQueryChange,
                addFavoriteTask = onAddFavorite,
                removeFavoriteTask = onRemoveFavorite,
                startDetailActivity = { list ->
                    startDetailActivity(list, 0)
                },
            )

            MainTab.FAVORITE.index -> FavoritesTab(
                favorites = favorites,
                gridState = favoriteGridState,
                removeFavoriteTask = onRemoveFavorite,
                startDetailActivity = { list, position ->
                    startDetailActivity(list, position)
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    CommonTheme {
        MainScreen(
            selectedTab = 0,
            onTabSelected = {},
            query = "",
            onQueryChange = {},
            searches = emptyList<SearchTabData>(),
            favorites = emptyList<UserUiData>(),
            searchListState = LazyListState(),
            favoriteGridState = LazyGridState(),
            isLoading = false,
            onSearch = {},
            onAddFavorite = {},
            onRemoveFavorite = {},
            tabs = listOf(MainTab.SEARCH, MainTab.FAVORITE),
            startDetailActivity = { _, _ -> }
        )
    }
}
