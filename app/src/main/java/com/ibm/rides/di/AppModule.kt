package com.ibm.rides.di

import android.content.Context
import com.ibm.rides.utils.ResourceManager
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
    fun provideResourceManager(
        @ApplicationContext context: Context
    ): ResourceManager {
        return ResourceManager(context)
    }
}