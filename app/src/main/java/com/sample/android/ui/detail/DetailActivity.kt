package com.sample.android.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.sample.android.databinding.ActivityDetailBinding
import com.sample.android.repository.FavoriteRepositoryImpl
import com.sample.android.ui.BaseAppCompatActivity
import com.sample.android.ui.data.UserUiData
import com.sample.android.ui.extension.getParcelableArrayListExtraSafety
import com.sample.android.utils.PreferencesModuleImpl
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DetailActivity : BaseAppCompatActivity() {
    companion object {
        private const val EXTRA_UI_LIST = "extra_ui_list"
        private const val EXTRA_SCROLL_TO_POSITION = "extra_scroll_to_position"

        @JvmStatic
        fun intent(context: Context, list: List<UserUiData>, position: Int = 0): Intent {
            return Intent(context, DetailActivity::class.java).apply {
                putExtra(EXTRA_UI_LIST, ArrayList(list))
                putExtra(EXTRA_SCROLL_TO_POSITION, position)
            }
        }

        @JvmStatic
        fun intent(context: Context, data: UserUiData): Intent {
            return intent(context, listOf(data))
        }
    }

    private val binding: ActivityDetailBinding by lazy {
        ActivityDetailBinding.inflate(layoutInflater)
    }

    private val viewModel: DetailViewModel by viewModels {
        viewModelFactory {
            initializer {
                DetailViewModel(
                    FavoriteRepositoryImpl(
                        preferencesModule = PreferencesModuleImpl(
                            this@DetailActivity
                        )
                    )
                )
            }
        }
    }

    private val selectedList: List<UserUiData> by lazy {
        return@lazy intent.getParcelableArrayListExtraSafety(EXTRA_UI_LIST) ?: emptyList()
    }

    private val scrollToPosition: Int by lazy {
        return@lazy intent.getIntExtra(EXTRA_SCROLL_TO_POSITION, 0)
    }

    private val adapter by lazy {
        DetailAdapter(object : DetailProperty {
            override val requestManager: RequestManager
                get() = Glide.with(this@DetailActivity)
        })
    }

    private val layoutManager by lazy {
        LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
    }

    private val snapHelper = PagerSnapHelper()

    private var currentPosition = RecyclerView.NO_POSITION
    private val findPositionScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val verticalScrollExtent = recyclerView.computeHorizontalScrollExtent()
            if (verticalScrollExtent == 0) {
                return
            }
            val position = (recyclerView.computeHorizontalScrollOffset()
                .toDouble() / verticalScrollExtent).roundToInt()
            if (position != currentPosition) {
                currentPosition = position
                viewModel.setCurrentData(currentPosition)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        snapHelper.attachToRecyclerView(binding.recyclerView)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.addOnScrollListener(findPositionScrollListener)

        viewModel.setUiData(selectedList)
        adapter.submitList(selectedList) {
            layoutManager.scrollToPosition(scrollToPosition)
        }

        binding.favoriteButton.setOnClickListener {
            val data = viewModel.currentList.getOrNull(currentPosition) ?: return@setOnClickListener
            if (data.isFavorite) {
                viewModel.unlikeFavoriteData(data)
            } else {
                viewModel.likeFavoriteData(data)
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentData.collect {
                    binding.title.text = it.data.title ?: ""
                    binding.favoriteButton.isSelected = it.isFavorite
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isChangedFavorite.collect { isChanged ->
                    if (isChanged) {
                        setResult(RESULT_OK)
                    } else {
                        setResult(RESULT_CANCELED)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        intent.putExtra(EXTRA_UI_LIST, ArrayList(viewModel.currentList))
        intent.putExtra(EXTRA_SCROLL_TO_POSITION, currentPosition)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.recyclerView.removeOnScrollListener(findPositionScrollListener)
    }
}