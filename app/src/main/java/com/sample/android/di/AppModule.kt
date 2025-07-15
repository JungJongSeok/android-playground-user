package com.sample.android.di

import android.content.Context
import com.sample.android.network.UserService
import com.sample.android.network.UserServiceImpl
import com.sample.android.repository.FavoriteRepository
import com.sample.android.repository.FavoriteRepositoryImpl
import com.sample.android.repository.SearchRepository
import com.sample.android.repository.SearchRepositoryImpl
import com.sample.android.utils.PreferencesModule
import com.sample.android.utils.PreferencesModuleImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserService(): UserService {
        return UserServiceImpl()
    }

    @Provides
    @Singleton
    fun providePreferencesModule(@ApplicationContext context: Context): PreferencesModule {
        return PreferencesModuleImpl(context)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(userService: UserService): SearchRepository {
        return SearchRepositoryImpl(userService)
    }

    @Provides
    @Singleton
    fun provideFavoriteRepository(preferencesModule: PreferencesModule): FavoriteRepository {
        return FavoriteRepositoryImpl(preferencesModule = preferencesModule)
    }
}