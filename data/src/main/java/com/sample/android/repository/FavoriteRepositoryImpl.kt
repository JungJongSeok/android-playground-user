package com.sample.android.repository

import com.sample.android.domain.entity.User
import com.sample.android.domain.repository.FavoriteRepository
import com.sample.android.mapper.toUser
import com.sample.android.mapper.toUserMetaData
import com.sample.android.utils.PreferencesModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val preferencesModule: PreferencesModule,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : FavoriteRepository {

    override suspend fun getFavorites(): List<User> {
        return withContext(dispatcher) {
            preferencesModule.favorites.map { it.toUser() }
        }
    }

    override suspend fun addToFavorites(user: User) {
        withContext(dispatcher) {
            val userMetaData = user.toUserMetaData()
            preferencesModule.favorites += listOf(userMetaData)
        }
    }

    override suspend fun removeFromFavorites(user: User) {
        withContext(dispatcher) {
            preferencesModule.favorites = preferencesModule.favorites.filter { data ->
                data.title != user.login
            }
        }
    }
}