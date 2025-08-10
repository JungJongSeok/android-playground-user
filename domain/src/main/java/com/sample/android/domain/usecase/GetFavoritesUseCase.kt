package com.sample.android.domain.usecase

import com.sample.android.domain.entity.User
import com.sample.android.domain.repository.FavoriteRepository
import javax.inject.Inject

/**
 * Use case for getting favorite users
 * Encapsulates the business logic for retrieving favorite users
 */
class GetFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    /**
     * Execute get favorites operation
     * @return List of favorite users
     */
    suspend operator fun invoke(): List<User> {
        return favoriteRepository.getFavorites()
    }
}