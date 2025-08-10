package com.sample.android.domain.usecase

import com.sample.android.domain.entity.User
import com.sample.android.domain.repository.FavoriteRepository
import javax.inject.Inject

/**
 * Use case for removing user from favorites
 * Encapsulates the business logic for removing favorites
 */
class RemoveFromFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    /**
     * Execute remove from favorites operation
     * @param user User to remove from favorites
     */
    suspend operator fun invoke(user: User) {
        favoriteRepository.removeFromFavorites(user)
    }
}