package com.zybooks.letsroll.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

sealed class Routes {
    @Serializable
    data object HomeScreen

    @Serializable
    data object GameScreen
}

data class AppPreferences (
    val highScore: Int = 0,
)

class AppStorage(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by
        preferencesDataStore("app_storage")

        private object PreferenceKeys {
            val HIGH_SCORE = intPreferencesKey("highScore")
        }
    }

    val appPreferencesFlow: Flow<AppPreferences> =
        context.dataStore.data.map { prefs ->
            val highScore = prefs[PreferenceKeys.HIGH_SCORE] ?: 0
            AppPreferences(highScore)
        }


    suspend fun saveHighScore(score: Int) {
        context.dataStore.edit { prefs ->
            if (score > (prefs[PreferenceKeys.HIGH_SCORE] ?: 0)) {
                prefs[PreferenceKeys.HIGH_SCORE] = score
            }
        }
    }
}

@Composable
fun LetsRollApp() {

    val navController = rememberNavController()
    val store = AppStorage(LocalContext.current)
    val appPrefs = store.appPreferencesFlow.collectAsStateWithLifecycle(AppPreferences())
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Routes.HomeScreen
    ) {
        composable<Routes.HomeScreen> {
            HomeScreen(startGame = {navController.navigate(Routes.GameScreen)}, appPreferences = appPrefs)
        }
        composable<Routes.GameScreen> {
            GameScreen(complete = {navController.navigate(Routes.HomeScreen)}, store = store, appPreferences = appPrefs, coroutineScope = coroutineScope)
        }
    }
}