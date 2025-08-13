package com.sample.android.ui.feature.detail
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import com.sample.android.ui.BaseComponentActivity
import com.sample.android.ui.extension.getParcelableArrayListExtraSafety
import com.sample.android.ui.model.UserUiData
import com.sample.android.ui.theme.CommonTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailActivity : BaseComponentActivity() {
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

    private val selectedList: List<UserUiData> by lazy {
        return@lazy intent.getParcelableArrayListExtraSafety(EXTRA_UI_LIST) ?: emptyList()
    }

    private val scrollToPosition: Int by lazy {
        return@lazy intent.getIntExtra(EXTRA_SCROLL_TO_POSITION, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CommonTheme {
                DetailRouter(
                    selectedList = selectedList,
                    initialPosition = scrollToPosition,
                    onBackClick = { finish() },
                    onFavoriteChanged = { isChanged ->
                        if (isChanged) {
                            setResult(RESULT_OK)
                        } else {
                            setResult(RESULT_CANCELED)
                        }
                    }
                )
            }
        }
    }
}
