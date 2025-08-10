package com.sample.android.domain.usecase

import com.sample.android.domain.entity.User
import com.sample.android.domain.repository.FavoriteRepository
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(): List<User> {
        return favoriteRepository.getFavorites()
    }
}