package com.sample.android.utils

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.sample.android.data.UserMetaData

interface PreferencesModule {
    var favorites: List<UserMetaData>
}

class PreferencesModuleImpl(private val context: Context) : PreferencesModule {
    companion object {
        private const val KEY_FAVORITES = "key_favorites"
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences(
            "Preference",
            Context.MODE_PRIVATE
        )
    }

    override var favorites: List<UserMetaData>
        get() {
            val gson = GsonBuilder().create()
            val json = sharedPreferences?.getString(KEY_FAVORITES, null)
            val typeToken: TypeToken<MutableList<UserMetaData>> =
                object : TypeToken<MutableList<UserMetaData>>() {}
            return gson.fromJson(json, typeToken.type) ?: emptyList()
        }
        set(value) {
            val gson = GsonBuilder().create()
            sharedPreferences?.edit()?.let {
                val typeToken: TypeToken<MutableList<UserMetaData>> =
                    object : TypeToken<MutableList<UserMetaData>>() {}
                it.putString(KEY_FAVORITES, gson.toJson(value, typeToken.type))
                it.commit()
            }
        }
}