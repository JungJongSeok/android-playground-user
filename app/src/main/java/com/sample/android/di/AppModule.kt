package com.sample.android.di

import android.content.Context
import com.sample.android.domain.repository.FavoriteRepository
import com.sample.android.domain.repository.SearchRepository
import com.sample.android.network.UserService
import com.sample.android.network.UserServiceImpl
import com.sample.android.repository.FavoriteRepositoryImpl
import com.sample.android.repository.SearchRepositoryImpl
import com.sample.android.utils.PreferencesModule
import com.sample.android.utils.PreferencesModuleImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideUserService(): UserService {
        return UserServiceImpl()
    }

    @Provides
    fun providePreferencesModule(@ApplicationContext context: Context): PreferencesModule {
        return PreferencesModuleImpl(context)
    }

    @Provides
    fun provideCoroutineDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }
}