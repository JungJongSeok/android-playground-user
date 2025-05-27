package com.sample.android.network.extension

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

internal inline fun <reified T> String.fromJson(): T? {
    return try {
        GsonBuilder()
            .serializeNulls().create().fromJson(
                this,
                object : TypeToken<T>() {}.type
            )
    } catch (e: Exception) {
        null
    }
}

internal inline fun <reified T> Map<String, Any?>.fromJson(): T? {
    return try {
        GsonBuilder()
            .serializeNulls().create().fromJson(
                JSONObject(this).toString(),
                object : TypeToken<T>() {}.type
            )
    } catch (e: Exception) {
        null
    }
}

internal fun String?.toMap(): Map<String, Any?> {
    return try {
        this?.let { payload ->
            val jsonObject = JSONObject(payload)
            val map = mutableMapOf<String, Any?>()

            jsonObject.keys().forEach { key ->
                map[key] = jsonObject.opt(key)
            }
            map
        } ?: emptyMap()
    } catch (e: Exception) {
        emptyMap()
    }
}

internal fun<T> T.toMap(): Map<String, Any?> {
    return try {
        return Gson().toJson(this).toMap()
    } catch (e: Exception) {
        emptyMap()
    }
}
