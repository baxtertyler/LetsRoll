package tbax.gamedev.letsroll.letsroll

import android.app.Application
import tbax.gamedev.letsroll.letsroll.sensor.AccelerometerSensor

class LetsRollApplication : Application() {
    lateinit var accelerometerSensor: AccelerometerSensor

    override fun onCreate() {
        super.onCreate()
        accelerometerSensor = AccelerometerSensor(this)
    }
}