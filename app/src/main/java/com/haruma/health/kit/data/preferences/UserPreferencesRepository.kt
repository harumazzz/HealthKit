package com.haruma.health.kit.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.haruma.health.kit.data.model.UserGoals
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val keyStepsGoal = longPreferencesKey("steps_goal")
    private val keyCaloriesGoal = doublePreferencesKey("calories_goal")
    private val keyWaterGoal = intPreferencesKey("water_goal")
    private val keySleepGoal = doublePreferencesKey("sleep_goal")

    val userGoalsFlow: Flow<UserGoals> = context.dataStore.data.map { preferences ->
        UserGoals(
            stepsGoal = preferences[keyStepsGoal] ?: 10000L,
            caloriesGoal = preferences[keyCaloriesGoal] ?: 500.0,
            waterGoalMilliliters = preferences[keyWaterGoal] ?: 2000,
            sleepGoalHours = preferences[keySleepGoal] ?: 8.0
        )
    }

    suspend fun updateStepsGoal(steps: Long) {
        context.dataStore.edit { preferences ->
            preferences[keyStepsGoal] = steps
        }
    }

    suspend fun updateCaloriesGoal(calories: Double) {
        context.dataStore.edit { preferences ->
            preferences[keyCaloriesGoal] = calories
        }
    }

    suspend fun updateWaterGoal(milliliters: Int) {
        context.dataStore.edit { preferences ->
            preferences[keyWaterGoal] = milliliters
        }
    }

    suspend fun updateSleepGoal(hours: Double) {
        context.dataStore.edit { preferences ->
            preferences[keySleepGoal] = hours
        }
    }
}
