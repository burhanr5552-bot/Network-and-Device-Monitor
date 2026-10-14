package com.example.data

import com.example.model.BatteryCurrentSample
import com.example.model.BatteryInfo
import com.example.model.NetworkSpeedInfo
import com.example.model.SimSignalInfo
import com.example.model.SpeedSample
import com.example.model.TempSample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TelemetryRepository {

    private val _networkInfo = MutableStateFlow(NetworkSpeedInfo())
    val networkInfo: StateFlow<NetworkSpeedInfo> = _networkInfo.asStateFlow()

    private val _simInfo = MutableStateFlow(SimSignalInfo())
    val simInfo: StateFlow<SimSignalInfo> = _simInfo.asStateFlow()

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    private val _isServiceActive = MutableStateFlow(false)
    val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

    private val _speedHistory = MutableStateFlow<List<SpeedSample>>(emptyList())
    val speedHistory: StateFlow<List<SpeedSample>> = _speedHistory.asStateFlow()

    private val _tempHistory = MutableStateFlow<List<TempSample>>(emptyList())
    val tempHistory: StateFlow<List<TempSample>> = _tempHistory.asStateFlow()

    private val _currentHistory = MutableStateFlow<List<BatteryCurrentSample>>(emptyList())
    val currentHistory: StateFlow<List<BatteryCurrentSample>> = _currentHistory.asStateFlow()

    fun updateTelemetry(
        network: NetworkSpeedInfo,
        sim: SimSignalInfo,
        battery: BatteryInfo
    ) {
        _networkInfo.value = network
        _simInfo.value = sim
        _batteryInfo.value = battery

        val now = System.currentTimeMillis()
        val currentSpeedHistory = _speedHistory.value.toMutableList()
        currentSpeedHistory.add(SpeedSample(now, network.downSpeedBps, network.upSpeedBps))
        if (currentSpeedHistory.size > 40) {
            currentSpeedHistory.removeAt(0)
        }
        _speedHistory.value = currentSpeedHistory

        val currentTempHistory = _tempHistory.value.toMutableList()
        currentTempHistory.add(TempSample(now, battery.temperatureC))
        if (currentTempHistory.size > 40) {
            currentTempHistory.removeAt(0)
        }
        _tempHistory.value = currentTempHistory

        val currentBatHistory = _currentHistory.value.toMutableList()
        currentBatHistory.add(BatteryCurrentSample(now, battery.currentNowMa, battery.powerWatts))
        if (currentBatHistory.size > 40) {
            currentBatHistory.removeAt(0)
        }
        _currentHistory.value = currentBatHistory
    }

    fun setServiceActive(active: Boolean) {
        _isServiceActive.value = active
    }
}
