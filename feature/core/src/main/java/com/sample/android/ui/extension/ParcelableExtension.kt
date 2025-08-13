package com.sample.android.ui.extension

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable

@Suppress("DEPRECATION")
inline fun <reified P : Parcelable> Intent.getParcelableExtraSafety(key: String): P? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, P::class.java)
    } else {
        getParcelableExtra(key)
    }
}

@Suppress("DEPRECATION")
inline fun <reified P : Parcelable> Intent.getParcelableArrayListExtraSafety(key: String): List<P>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(key, P::class.java)
    } else {
        getParcelableArrayListExtra(key)
    }
}

@Suppress("DEPRECATION")
inline fun <reified P : Parcelable> Bundle.getParcelableSafety(key: String): P? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, P::class.java)
    } else {
        getParcelable(key)
    }
}

@Suppress("DEPRECATION")
inline fun <reified P : Parcelable> Bundle.getParcelableArrayListExtraSafety(key: String): List<P>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayList(key, P::class.java)
    } else {
        getParcelableArrayList(key)
    }
}
