package com.sample.android.domain.repository

import com.sample.android.domain.entity.User

interface FavoriteRepository {
    suspend fun getFavorites(): List<User>

    suspend fun addToFavorites(user: User)

    suspend fun removeFromFavorites(user: User)
}