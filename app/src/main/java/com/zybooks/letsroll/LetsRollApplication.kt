package com.zybooks.letsroll

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zybooks.letsroll.sensor.AccelerometerSensor
import com.zybooks.letsroll.ui.HomeScreen
import com.zybooks.letsroll.ui.GameScreen
import kotlinx.serialization.Serializable


class LetsRollApplication : Application() {
    lateinit var accelerometerSensor: AccelerometerSensor

    override fun onCreate() {
        super.onCreate()
        accelerometerSensor = AccelerometerSensor(this)
    }
}