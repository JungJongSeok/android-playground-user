package com.sample.android.domain.usecase

import com.sample.android.domain.entity.User
import com.sample.android.domain.repository.FavoriteRepository
import javax.inject.Inject

/**
 * Use case for adding user to favorites
 * Encapsulates the business logic for favorite operations
 */
class AddToFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    /**
     * Execute add to favorites operation
     * @param user User to add to favorites
     */
    suspend operator fun invoke(user: User) {
        favoriteRepository.addToFavorites(user)
    }
}