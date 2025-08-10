package com.sample.android.domain.usecase

import com.sample.android.domain.entity.User
import com.sample.android.domain.repository.FavoriteRepository
import javax.inject.Inject

class RemoveFromFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(user: User) {
        favoriteRepository.removeFromFavorites(user)
    }
}