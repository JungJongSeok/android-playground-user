package com.sample.android.repository

import com.sample.android.data.UserMetaData
import com.sample.android.utils.PreferencesModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface FavoriteRepository {
    suspend fun get(): List<UserMetaData>
    suspend fun add(userMetaData: UserMetaData)
    suspend fun remove(userMetaData: UserMetaData)
}

class FavoriteRepositoryImpl(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val preferencesModule: PreferencesModule
) : FavoriteRepository {

    override suspend fun get(): List<UserMetaData> {
        return withContext(dispatcher) {
            preferencesModule.favorites
        }
    }

    override suspend fun add(userMetaData: UserMetaData) {
        withContext(dispatcher) {
            preferencesModule.favorites += listOf(userMetaData)
        }
    }

    override suspend fun remove(userMetaData: UserMetaData) {
        withContext(dispatcher) {
            preferencesModule.favorites = preferencesModule.favorites.mapNotNull { data ->
                if (data == userMetaData) {
                    null
                } else {
                    data
                }
            }
        }
    }
}