package com.haruma.health.kit.di

import androidx.health.connect.client.HealthConnectClient
import com.haruma.health.kit.data.health.HealthConnectManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HealthModule {

    @Provides
    @Singleton
    fun provideHealthConnectClient(healthConnectManager: HealthConnectManager): HealthConnectClient? {
        return healthConnectManager.healthConnectClient
    }
}
