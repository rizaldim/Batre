package com.tokopedia.batre

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity: AppCompatActivity() {

    val UNKNOWN_TEMP = -9999

    var batteryStatusIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        batteryStatusIntent = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            this.registerReceiver(null, ifilter)
        }
        getBatteryStatus()
        getBatteryTemperature()
        getBatteryLevel()
        getTimeToFullyCharged()
    }

    private fun getTimeToFullyCharged() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val timeToFullInMilliseconds = batteryManager.computeChargeTimeRemaining()
            remaining_time_to_full_textview.text = "Will be full in: ${timeToFullInMilliseconds / 1000} seconds"
        }
    }

    private fun getBatteryLevel() {
        val batteryPct: Float? = batteryStatusIntent?.let { intent ->
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            level * 100 / scale.toFloat()
        }
        battery_level_textview?.text = "Percentage: $batteryPct"
    }

    private fun getBatteryStatus() {
        val statusInInt =  batteryStatusIntent?.getIntExtra(
            BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN
        ) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        val batteryStatus = when (statusInInt) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            else -> "Unknown"
        }

        battery_status_textview?.text = "Status: $batteryStatus"
    }

    private fun getBatteryTemperature() {
        val temperatureInTenthDegreeOfC = batteryStatusIntent?.getIntExtra(
            BatteryManager.EXTRA_TEMPERATURE, UNKNOWN_TEMP
        ) ?: UNKNOWN_TEMP

        temperature_textview?.text = if (temperatureInTenthDegreeOfC == UNKNOWN_TEMP) "Unknown" else "${temperatureInTenthDegreeOfC / 10.0} °C"
    }

}
