package com.example.telemetry

import android.content.Context
import android.os.Build
import android.telephony.CellSignalStrength
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import com.example.model.SimSignalInfo

class SimTelemetryManager(private val context: Context) {

    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    private var cachedSignalLevel = 0
    private var cachedSignalDbm = -999

    fun captureSimInfo(): SimSignalInfo {
        if (telephonyManager == null) {
            return SimSignalInfo(
                simState = "Unavailable",
                carrierName = "No Telephony Service",
                networkType = "None",
                signalLevel = 0,
                signalDbm = -999
            )
        }

        val simStateString = when (telephonyManager.simState) {
            TelephonyManager.SIM_STATE_READY -> "Ready"
            TelephonyManager.SIM_STATE_ABSENT -> "No SIM"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network Locked"
            TelephonyManager.SIM_STATE_NOT_READY -> "Not Ready"
            TelephonyManager.SIM_STATE_PERM_DISABLED -> "Permanently Disabled"
            else -> "Unknown"
        }

        var carrier = telephonyManager.networkOperatorName
        if (carrier.isNullOrBlank()) {
            carrier = telephonyManager.simOperatorName
        }
        if (carrier.isNullOrBlank()) {
            carrier = if (telephonyManager.simState == TelephonyManager.SIM_STATE_READY) "Cellular Carrier" else "No Carrier"
        }

        val networkTypeString = resolveNetworkType(telephonyManager)

        // Read signal strength
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val signalStrength: SignalStrength? = telephonyManager.signalStrength
                if (signalStrength != null) {
                    cachedSignalLevel = signalStrength.level // 0 to 4
                    val cellStrengths = signalStrength.cellSignalStrengths
                    if (cellStrengths.isNotEmpty()) {
                        val firstValidDbm = cellStrengths.firstOrNull { it.dbm != Int.MAX_VALUE && it.dbm < 0 }
                        if (firstValidDbm != null) {
                            cachedSignalDbm = firstValidDbm.dbm
                        }
                    }
                }
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            }
        }

        val isRoaming = telephonyManager.isNetworkRoaming

        val dataActivity = when (telephonyManager.dataActivity) {
            TelephonyManager.DATA_ACTIVITY_IN -> "Downloading"
            TelephonyManager.DATA_ACTIVITY_OUT -> "Uploading"
            TelephonyManager.DATA_ACTIVITY_INOUT -> "Bi-directional"
            TelephonyManager.DATA_ACTIVITY_DORMANT -> "Dormant"
            else -> "Idle"
        }

        val slotsCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                telephonyManager.activeModemCount.coerceAtLeast(1)
            } catch (_: Exception) {
                1
            }
        } else {
            1
        }

        return SimSignalInfo(
            simState = simStateString,
            carrierName = carrier,
            networkType = networkTypeString,
            signalLevel = cachedSignalLevel.coerceIn(0, 4),
            signalDbm = cachedSignalDbm,
            isDataRoaming = isRoaming,
            dataActivity = dataActivity,
            simSlotsCount = slotsCount
        )
    }

    private fun resolveNetworkType(tm: TelephonyManager): String {
        return try {
            @Suppress("DEPRECATION")
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    tm.dataNetworkType
                } catch (_: SecurityException) {
                    tm.networkType
                }
            } else {
                tm.networkType
            }

            when (type) {
                TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA -> "3.5G HSPA+"
                TelephonyManager.NETWORK_TYPE_UMTS -> "3G UMTS"
                TelephonyManager.NETWORK_TYPE_EDGE -> "2G EDGE"
                TelephonyManager.NETWORK_TYPE_GPRS -> "2G GPRS"
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> "Cellular Network"
                else -> "Cellular"
            }
        } catch (_: SecurityException) {
            "Cellular (Permission limited)"
        } catch (_: Exception) {
            "Cellular"
        }
    }
}
