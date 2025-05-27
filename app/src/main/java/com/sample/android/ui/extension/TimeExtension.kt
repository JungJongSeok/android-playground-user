package com.sample.android.ui.extension

import android.content.Context
import androidx.annotation.StringRes
import java.text.SimpleDateFormat
import java.util.Locale

internal fun Long.setTimeText(context: Context, @StringRes resId: Int): String {
    if (this <= 0) {
        return ""
    }
    val timeFormatter = SimpleDateFormat(context.getString(resId), Locale.getDefault())
    return timeFormatter.format(this)
}