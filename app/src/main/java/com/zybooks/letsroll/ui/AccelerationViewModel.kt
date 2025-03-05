package com.zybooks.letsroll.ui

import androidx.collection.mutableFloatListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zybooks.letsroll.LetsRollApplication
import com.zybooks.letsroll.sensor.AccelerometerSensor

class AccelerationViewModel(
    private val accelerometerSensor: AccelerometerSensor,
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as LetsRollApplication)
                AccelerationViewModel(
                    accelerometerSensor = application.accelerometerSensor
                )
            }
        }
    }

    var accelValues = mutableStateListOf(0.0f, 0.0f, 0.0f)
    var canAccelerate by mutableStateOf(true)

    var oCenterX by mutableStateOf(0f)
    var oCenterY by mutableStateOf(0f)

    fun startListening() {
        accelerometerSensor.startListening { values ->
            accelValues.clear()
            accelValues.addAll(values)
        }
    }

    fun stopListening() {
        accelerometerSensor.stopListening()
    }
}