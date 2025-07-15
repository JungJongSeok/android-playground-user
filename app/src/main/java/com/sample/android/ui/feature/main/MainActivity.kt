package com.sample.android.ui.feature.main


import android.os.Bundle
import androidx.activity.compose.setContent
import com.sample.android.ui.BaseComponentActivity
import com.sample.android.ui.theme.CommonTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CommonTheme {
                MainRoute()
            }
        }
    }
}
