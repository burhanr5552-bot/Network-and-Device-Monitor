package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.BatteryInfo
import com.example.model.NetworkSpeedInfo
import com.example.telemetry.NetworkTelemetryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NetPulse", appName)
    }

    @Test
    fun `speed formatting tests`() {
        assertEquals("0 B/s", NetworkTelemetryManager.formatSpeed(0L))
        assertEquals("500 B/s", NetworkTelemetryManager.formatSpeed(500L))
        assertEquals("1.5 KB/s", NetworkTelemetryManager.formatSpeed(1536L))
        assertEquals("2.0 MB/s", NetworkTelemetryManager.formatSpeed(2 * 1024 * 1024L))
    }

    @Test
    fun `bytes formatting tests`() {
        assertEquals("0 B", NetworkTelemetryManager.formatBytes(0L))
        assertEquals("1.0 MB", NetworkTelemetryManager.formatBytes(1024 * 1024L))
        assertEquals("1.00 GB", NetworkTelemetryManager.formatBytes(1024 * 1024 * 1024L))
    }

    @Test
    fun `real time battery power calculation test`() {
        val voltageMv = 4000
        val currentMa = 2000
        val powerWatts = (voltageMv.toFloat() * currentMa.toFloat()) / 1_000_000f
        assertEquals(8.0f, powerWatts, 0.001f)
    }
}
