package com.example.nutrismart.di

import android.content.Context
import com.example.nutrismart.data.SessionManager
import com.example.nutrismart.data.remote.NutriSmartApi
import com.example.nutrismart.data.remote.RetrofitClient
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
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideNutriSmartApi(): NutriSmartApi {
        return RetrofitClient.api
    }
}
