package com.sample.android.di

import com.sample.android.domain.repository.FavoriteRepository
import com.sample.android.domain.repository.SearchRepository
import com.sample.android.repository.FavoriteRepositoryImpl
import com.sample.android.repository.SearchRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides repository implementations
 * Binds data layer implementations to domain interfaces
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds SearchRepositoryImpl to SearchRepository interface
     */
    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository

    /**
     * Binds FavoriteRepositoryImpl to FavoriteRepository interface
     */
    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        favoriteRepositoryImpl: FavoriteRepositoryImpl
    ): FavoriteRepository
}