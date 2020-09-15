package com.tokopedia.batre

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity: AppCompatActivity() {

    val UNKNOWN_TEMP = -9999

    var batteryStatusIntent: Intent? = null
    var batteryManager: BatteryManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        }

        update_button.setOnClickListener {
            updateInfo()
        }
    }

    override fun onResume() {
        super.onResume()
        updateInfo()
    }

    private fun updateInfo() {
        batteryStatusIntent = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
            this.registerReceiver(null, intentFilter)
        }
        getBatteryStatus()
        getBatteryTemperature()
        getBatteryLevel()
        getPluggedStatus()
        getTimeToFullyCharged()
        updateLastUpdated()
        getBatteryHealth()
        getBatteryCapacity()
    }

    @SuppressLint("NewApi")
    private fun getBatteryCapacity() {
        batteryManager?.let {
            val capacityInMicroAmpereHours = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            val averageBatteryCurrentInMicroAmpere = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
            val instantaneousCurrentInMicroAmpere = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            val remainingEnergyInNanoWattHours = it.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)

            battery_capacity_textview.text =
                """Battery capacity: ${capacityInMicroAmpereHours / 1000.0} mAh
                |Average battery current: ${averageBatteryCurrentInMicroAmpere / 1000.0} mA
                |Instantaneous battery current: ${instantaneousCurrentInMicroAmpere / 1000.0} mA
                |Battery remaining energy: ${remainingEnergyInNanoWattHours / 1_000_000_000.0} Wh"""
            .trimMargin()
        }
    }

    private fun getBatteryHealth() {
        val healthInt = batteryStatusIntent?.getIntExtra(
            BatteryManager.EXTRA_HEALTH, -1
        ) ?: -1
        val text = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified failure"
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> "Unknown"
            else -> "Unknown"
        }
        battery_health_textview.text = "Battery Health: $text"
    }

    private fun getPluggedStatus() {
        val pluggedInt = batteryStatusIntent?.getIntExtra(
            BatteryManager.EXTRA_PLUGGED, -1
        ) ?: -1
        val sourceText = when (pluggedInt) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Unknown"
        }
        plugged_textview.text = "Source: $sourceText"
    }

    private fun updateLastUpdated() {
        last_updated_textview.text = "Last updated: ${DateFormat.format("yyyy-MM-dd hh:mm:ss a", Date())}"
    }

    @SuppressLint("NewApi")
    private fun getTimeToFullyCharged() {

        batteryManager?.let {
            val timeToFullInMilliseconds = it.computeChargeTimeRemaining()
            if (timeToFullInMilliseconds == -1L) {
                remaining_time_to_full_textview.text = "Failed to compute charge time remaining"
            } else {
                val timeToFullInSeconds = timeToFullInMilliseconds / 1000.0
                remaining_time_to_full_textview.text = if (timeToFullInSeconds < 60) {
                    "Will be full in < 1 min"
                } else if (timeToFullInSeconds < 3600) {
                    "Will be full in ${(timeToFullInSeconds / 60).toInt()} minutes"
                } else {
                    "Will be full in more than 1 hour"
                }
            }
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
        val statusInInt = batteryStatusIntent?.getIntExtra(
            BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN
        ) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        val batteryStatus = when (statusInInt) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            else -> "Unknown"
        }

        val isCharging = statusInInt == BatteryManager.BATTERY_STATUS_CHARGING
        remaining_time_to_full_textview.visibility = if (isCharging) View.VISIBLE else View.GONE
        plugged_textview.visibility = if (isCharging) View.VISIBLE else View.GONE

        battery_status_textview?.text = "Status: $batteryStatus"
    }

    private fun getBatteryTemperature() {
        val temperatureInTenthDegreeOfC = batteryStatusIntent?.getIntExtra(
            BatteryManager.EXTRA_TEMPERATURE, UNKNOWN_TEMP
        ) ?: UNKNOWN_TEMP

        val temperatureText = if (temperatureInTenthDegreeOfC == UNKNOWN_TEMP) "Unknown" else "${temperatureInTenthDegreeOfC / 10.0} °C"
        temperature_textview?.text = "Temperature: $temperatureText"
    }

}
