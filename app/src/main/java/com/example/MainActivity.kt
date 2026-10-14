package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.screens.BatteryThermalScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NetworkSimScreen
import com.example.ui.screens.NotificationSettingsScreen
import com.example.ui.theme.CoralNeon
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldNeon
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current

    val networkInfo by viewModel.networkInfo.collectAsStateWithLifecycle()
    val simInfo by viewModel.simInfo.collectAsStateWithLifecycle()
    val batteryInfo by viewModel.batteryInfo.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isServiceActive by viewModel.isServiceActive.collectAsStateWithLifecycle()
    val speedHistory by viewModel.speedHistory.collectAsStateWithLifecycle()
    val tempHistory by viewModel.tempHistory.collectAsStateWithLifecycle()
    val currentHistory by viewModel.currentHistory.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val isBandwidthTesting by viewModel.isBandwidthTesting.collectAsStateWithLifecycle()
    val bandwidthResult by viewModel.bandwidthResult.collectAsStateWithLifecycle()

    // Permission launcher for Notification and Phone State
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshTelemetryOnce()
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Handle back button on secondary tabs to navigate back to Dashboard
    BackHandler(enabled = activeTab != AppTab.DASHBOARD) {
        viewModel.selectTab(AppTab.DASHBOARD)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "NetPulse",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = CyanNeon
                        )

                        // Status pill
                        Surface(
                            shape = CircleShape,
                            color = if (isServiceActive) EmeraldNeon.copy(alpha = 0.15f) else DarkBorder.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isServiceActive) EmeraldNeon.copy(alpha = 0.5f) else DarkBorder
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(6.dp),
                                    shape = CircleShape,
                                    color = if (isServiceActive) EmeraldNeon else TextSecondary
                                ) {}
                                Text(
                                    text = if (isServiceActive) "LIVE" else "IDLE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = if (isServiceActive) EmeraldNeon else TextSecondary
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleMonitoringService(context) },
                        modifier = Modifier.testTag("appbar_toggle_service")
                    ) {
                        Icon(
                            imageVector = if (isServiceActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isServiceActive) "Stop Service" else "Start Service",
                            tint = if (isServiceActive) CoralNeon else EmeraldNeon
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == AppTab.DASHBOARD,
                    onClick = { viewModel.selectTab(AppTab.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        indicatorColor = CyanNeon,
                        selectedTextColor = CyanNeon,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                NavigationBarItem(
                    selected = activeTab == AppTab.NETWORK_SIM,
                    onClick = { viewModel.selectTab(AppTab.NETWORK_SIM) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Network & SIM"
                        )
                    },
                    label = { Text("Net & SIM") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        indicatorColor = CyanNeon,
                        selectedTextColor = CyanNeon,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_network")
                )

                NavigationBarItem(
                    selected = activeTab == AppTab.BATTERY_THERMAL,
                    onClick = { viewModel.selectTab(AppTab.BATTERY_THERMAL) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery & Thermal"
                        )
                    },
                    label = { Text("Battery") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        indicatorColor = EmeraldNeon,
                        selectedTextColor = EmeraldNeon,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_battery")
                )

                NavigationBarItem(
                    selected = activeTab == AppTab.NOTIFICATIONS,
                    onClick = { viewModel.selectTab(AppTab.NOTIFICATIONS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Alerts & Config"
                        )
                    },
                    label = { Text("Alerts") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        indicatorColor = CoralNeon,
                        selectedTextColor = CoralNeon,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_alerts")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tabTransition"
            ) { tab ->
                when (tab) {
                    AppTab.DASHBOARD -> DashboardScreen(
                        networkInfo = networkInfo,
                        simInfo = simInfo,
                        batteryInfo = batteryInfo,
                        settings = settings,
                        isServiceActive = isServiceActive,
                        speedHistory = speedHistory,
                        onToggleService = { viewModel.toggleMonitoringService(context) },
                        onRefresh = { viewModel.refreshTelemetryOnce() },
                        onNavigateToNetwork = { viewModel.selectTab(AppTab.NETWORK_SIM) },
                        onNavigateToBattery = { viewModel.selectTab(AppTab.BATTERY_THERMAL) },
                        onNavigateToNotifications = { viewModel.selectTab(AppTab.NOTIFICATIONS) }
                    )

                    AppTab.NETWORK_SIM -> NetworkSimScreen(
                        networkInfo = networkInfo,
                        simInfo = simInfo,
                        isBandwidthTesting = isBandwidthTesting,
                        bandwidthResult = bandwidthResult,
                        onRunBandwidthTest = { viewModel.runBandwidthTest() }
                    )

                    AppTab.BATTERY_THERMAL -> BatteryThermalScreen(
                        batteryInfo = batteryInfo,
                        settings = settings,
                        tempHistory = tempHistory,
                        currentHistory = currentHistory,
                        onUpdateSettings = { transform -> viewModel.updateSettings(transform) }
                    )

                    AppTab.NOTIFICATIONS -> NotificationSettingsScreen(
                        settings = settings,
                        isServiceActive = isServiceActive,
                        onToggleService = { viewModel.toggleMonitoringService(context) },
                        onUpdateSettings = { transform -> viewModel.updateSettings(transform) }
                    )
                }
            }
        }
    }
}
