package com.sample.android.ui.main


import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sample.android.R
import com.sample.android.network.NetworkCommonException
import com.sample.android.network.UserServiceImpl
import com.sample.android.repository.FavoriteRepositoryImpl
import com.sample.android.repository.SearchRepositoryImpl
import com.sample.android.ui.BaseComponentActivity
import com.sample.android.ui.data.UserUiData
import com.sample.android.ui.detail.DetailActivity
import com.sample.android.ui.theme.ColorBlack22
import com.sample.android.ui.theme.ColorBlack88
import com.sample.android.ui.theme.ColorBlackDD
import com.sample.android.ui.theme.CommonTheme
import com.sample.android.utils.PreferencesModuleImpl
import kotlinx.coroutines.launch

class MainActivity : BaseComponentActivity() {
    companion object {
        private const val KEY_DETAIL_ACTIVITY_RESULT = "key_detail_activity_result"
    }

    private val viewModel: MainViewModel by viewModels {
        viewModelFactory {
            initializer {
                MainViewModel(
                    SearchRepositoryImpl(userService = UserServiceImpl()),
                    FavoriteRepositoryImpl(preferencesModule = PreferencesModuleImpl(this@MainActivity))
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CommonTheme {
                SearchTabs(viewModel)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.detailActivity.collect { (list, position) ->
                    startDetailActivity(this@MainActivity, list, position)
                }
            }
        }

        viewModel.initialize()
    }

    private fun startDetailActivity(context: Context, list: List<UserUiData>, position: Int = 0) {
        val intent = DetailActivity.intent(context, list, position)
        activityResultRegistry
            .register(
                KEY_DETAIL_ACTIVITY_RESULT,
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == RESULT_OK) {
                    viewModel.restore()
                }
            }.launch(intent)
    }
}

@Composable
fun SearchTabs(viewModel: MainViewModel) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    val searches by viewModel.searches.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val searchListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }
    val favoriteGridState = rememberSaveable(saver = LazyGridState.Saver) {
        LazyGridState()
    }
    val tabs = listOf(
        context.getString(R.string.main_tab_search),
        context.getString(R.string.main_tab_favorite)
    )
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
                viewModel.searchMore(query)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.scrollToTop
            .collect {
                searchListState.scrollToItem(0, 0)
            }
    }

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
            tabs.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier.height(70.dp),
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White,
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(11.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Text(
                                text = title,
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
            0 -> SearchTab(
                viewModel,
                searches,
                searchListState,
                query,
                onValueChange = { query = it })

            1 -> FavoritesTab(viewModel, favorites, favoriteGridState)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    CommonTheme {
        SearchTabs(
            MainViewModel(
                SearchRepositoryImpl(userService = UserServiceImpl()),
                FavoriteRepositoryImpl(preferencesModule = PreferencesModuleImpl(LocalContext.current))
            )
        )
    }
}
