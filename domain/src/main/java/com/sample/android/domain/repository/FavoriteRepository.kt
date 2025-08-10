package com.sample.android.domain.repository

import com.sample.android.domain.entity.User

/**
 * Domain repository interface for favorite operations
 * Defines the contract for favorite functionality without implementation details
 */
interface FavoriteRepository {
    /**
     * Get all favorite users
     * @return List of favorite users
     */
    suspend fun getFavorites(): List<User>

    /**
     * Add user to favorites
     * @param user User to add to favorites
     */
    suspend fun addToFavorites(user: User)

    /**
     * Remove user from favorites
     * @param user User to remove from favorites
     */
    suspend fun removeFromFavorites(user: User)
}