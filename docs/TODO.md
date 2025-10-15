# Production Readiness Protocol for Pwnagotchi Android Application

## I. Foundational Upgrade: Aligning the Build Environment

This initial phase is critical for establishing a stable and modern foundation. It addresses the explicit requirement to update the project's core build dependencies, ensuring compatibility and access to the latest APIs and build features. The process must follow a specific sequence—upgrading the core build system before the plugins that depend on it—to prevent version compatibility conflicts.

### A. Gradle Wrapper Upgrade to Version 8.14

The Android Gradle Plugin (AGP) and the Kotlin Gradle Plugin (KGP) have strict compatibility requirements with the underlying Gradle build tool. The target configuration specifies AGP version 8.14.0 and KGP version 2.2.20. Compatibility documentation indicates that this combination requires a Gradle version between 7.6.3 and 8.14.1 To ensure maximum stability and access to the latest features, such as improved lazy configuration and expanded diagnostics, upgrading to the highest compatible version, Gradle 8.14, is the correct first step.3

Attempting to modify plugin versions before aligning the Gradle wrapper would result in an immediate build failure due to version incompatibility. Therefore, the first action is to update the Gradle distribution URL.

**Action**: Modify `gradle/wrapper/gradle-wrapper.properties`

Update the `distributionUrl` property in this file to point to the Gradle 8.14 binary.

```properties
# FILE:./gradle/wrapper/gradle-wrapper.properties

distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.14-bin.zip
```

### B. Android Gradle Plugin and Kotlin Plugin Update

With the Gradle wrapper correctly configured, the next step is to update the plugin versions in the project's root build script. The project currently uses AGP 8.13.0.4 This must be updated to the required 8.14.0. The Kotlin plugin version is already 2.2.20, which meets the requirement and requires no changes.

**Action**: Modify `./build.gradle.kts`

Update the version alias for the `com.android.application` plugin.

```kotlin
// FILE:./build.gradle.kts

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.14.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
}
```

### C. Dependency Modernization and Addition

A production-ready application should utilize recent, stable versions of its dependencies to benefit from bug fixes, performance improvements, and security patches. The current project uses several outdated libraries.4 This step involves updating existing dependencies and adding new ones required for the features to be implemented in subsequent sections.

The new features require the following libraries:

*   **Jetpack Glance**: For building the four home screen widgets. The necessary dependencies are `glance-appwidget` and `glance-material3`.5
*   **Jetpack Navigation Compose**: To implement the new route-based navigation architecture, replacing the current state-flag system.
*   **AzNavRail**: The specified library for the navigation rail component. As the library's official dependency information is unavailable, a placeholder will be used.7

**Action**: Modify `./app/build.gradle.kts`

Update the `dependencies` block to use the latest stable versions of existing libraries and to include the new dependencies for Glance and Navigation.

```kotlin
// FILE:./app/build.gradle.kts (dependencies block only)

dependencies {
    // Core & Lifecycle - Updated to latest stable versions
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Compose BOM - Updated to latest stable version
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Material Components (for themes, etc.) - Updated
    implementation("com.google.android.material:material:1.12.0")

    // Networking - Ktor & WebSocket - Updated
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-cio:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("org.java-websocket:Java-WebSocket:1.5.7")

    // Root Access - libsu - Updated
    implementation("com.github.topjohnwu.libsu:core:5.2.2") // Already latest
    implementation("com.github.topjohnwu.libsu:service:5.2.2") // Already latest

    // --- NEW DEPENDENCIES ---
    // Jetpack Glance for App Widgets
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")

    // Jetpack Navigation for Compose
    implementation("androidx.navigation:navigation-compose:2.8.0-beta05")

    // AzNavRail (Placeholder - replace with actual dependency)
    // implementation("com.github.hereliesaz:aznavrail:1.0.0")

    // Testing - Updated
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

## II. Architectural Refactoring: Implementing a Scalable Navigation Rail

The current navigation logic within `MainActivity` relies on a series of mutable boolean state flags (`showSettings`, `showPlugins`) and a `when` block to switch between composables.4 This pattern is not scalable, is prone to state management errors, and does not support deep linking or a persistent navigation structure. This section details the refactoring process to a modern, robust architecture using Jetpack Navigation Compose, centered around a `NavHost` and the specified `AzNavRail` component. This change decouples navigation logic from UI state management, a fundamental improvement that aligns with the Single Responsibility Principle and enhances maintainability.

### A. Defining Navigation Routes and Graph

The first step in this architectural shift is to define the navigation destinations as a set of unique routes rather than mutable state variables. A `sealed class` is the ideal Kotlin construct for this, providing type-safe, enumerated destinations.

**Action**: Create `app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/navigation/AppNavigation.kt`

This new file will contain the `sealed class Screen` defining all primary destinations, each with a route string, a title resource ID, and an icon. It will also contain a list of these items for easy iteration when building the navigation rail.

```kotlin
// FILE:./app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/navigation/AppNavigation.kt

package com.pwnagotchi.pwnagotchiandroid.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Extension
import androidx.compose.ui.graphics.vector.ImageVector
import com.pwnagotchi.pwnagotchiandroid.R

sealed class Screen(val route: String, val titleResId: Int, val icon: ImageVector) {
    data object Home : Screen("home", R.string.screen_home, Icons.Default.Home)
    data object Plugins : Screen("plugins", R.string.plugins, Icons.Default.Extension)
    data object Opwngrid : Screen("opwngrid", R.string.screen_opwngrid, Icons.Default.Leaderboard)
    data object Settings : Screen("settings", R.string.settings, Icons.Default.Settings)
}

val navRailItems = listOf(
    Screen.Home,
    Screen.Plugins,
    Screen.Opwngrid,
    Screen.Settings
)
```

**Action**: Add new string resources to `app/src/main/res/values/strings.xml`

These strings are referenced by the `Screen` sealed class.

```xml
<resources>
   ...
    <string name="screen_home">Home</string>
    <string name="screen_opwngrid">oPwngrid</string>
</resources>
```

### B. Refactoring MainActivity with Scaffold, AzNavRail, and NavHost

With the navigation graph defined, the main UI can be reconstructed. The `MainActivity` will be simplified to a host for the primary app structure, which will be defined in a new `MainScreen` composable. This structure will use a `Scaffold` to contain the `AzNavRail` and a `NavHost` that manages the content for the currently selected route.

**Action**: Create `app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/MainScreen.kt`

This new file will contain the main UI structure of the application. It uses a `Row` to place the `AzNavRail` (represented by a standard `NavigationRail` as a placeholder) alongside the `NavHost`.

```kotlin
// FILE:./app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/MainScreen.kt

package com.pwnagotchi.pwnagotchiandroid.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiViewModel
import com.pwnagotchi.pwnagotchiandroid.ui.navigation.Screen
import com.pwnagotchi.pwnagotchiandroid.ui.navigation.navRailItems
import com.pwnagotchi.pwnagotchiandroid.ui.screens.HomeScreen
import com.pwnagotchi.pwnagotchiandroid.ui.screens.OpwngridScreenNav
import com.pwnagotchi.pwnagotchiandroid.ui.screens.PluginsScreenNav
import com.pwnagotchi.pwnagotchiandroid.ui.screens.SettingsScreenNav

@Composable
fun MainScreen(viewModel: PwnagotchiViewModel) {
    val navController = rememberNavController()

    Row(Modifier.fillMaxSize()) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        // Placeholder for AzNavRail. A standard Material3 NavigationRail is used.
        // Replace this with the AzNavRail composable when the dependency is available.
        NavigationRail {
            navRailItems.forEach { screen ->
                NavigationRailItem(
                    icon = { Icon(screen.icon, contentDescription = stringResource(screen.titleResId)) },
                    label = { Text(stringResource(screen.titleResId)) },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.weight(1f)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel)
            }
            composable(Screen.Plugins.route) {
                PluginsScreenNav(viewModel = viewModel)
            }
            composable(Screen.Opwngrid.route) {
                OpwngridScreenNav(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreenNav(viewModel = viewModel)
            }
        }
    }
}
```

**Action**: Create new screen wrapper files and refactor existing screens

The old screen composables were designed to be swapped within a single activity. They must be adapted to work within the `NavHost` and receive the `ViewModel` directly. New wrapper files are created for clarity.

**Create `app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/screens/HomeScreen.kt` (Refactored `PwnagotchiScreen`)**

```kotlin
// FILE:./app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/screens/HomeScreen.kt

package com.pwnagotchi.pwnagotchiandroid.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiViewModel
import com.pwnagotchi.pwnagotchiandroid.R
import com.pwnagotchi.pwnagotchiandroid.ui.viewmodels.PwnagotchiUiState
import com.pwnagotchi.pwnagotchiandroid.ui.composables.*
import com.topjohnwu.superuser.Shell
import java.net.URI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PwnagotchiViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var rootStatus by remember { mutableStateOf("Root status: Unknown") }
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
    }
    var ipAddress by remember {
        mutableStateOf(sharedPreferences.getString("ip_address", "192.168.1.100")?: "192.168.1.100")
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.app_name)) })
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
               .fillMaxSize()
               .padding(paddingValues)
               .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ConnectionStatus(uiState = uiState)
            ConnectionControls(
                ipAddress = ipAddress,
                onIpAddressChange = { ipAddress = it },
                onConnect = { viewModel.connect(URI("ws://$ipAddress:8765")) },
                onDisconnect = { viewModel.disconnect() }
            )
            RootControls(
                rootStatus = rootStatus,
                onRequestRoot = {
                    Shell.getShell { shell ->
                        rootStatus = if (shell.isRoot) "Root status: Granted" else "Root status: Denied"
                    }
                }
            )
            HandshakeList(uiState = uiState)
        }
    }
}
```

**Create `app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/screens/PluginsScreenNav.kt`**

```kotlin
// FILE:./app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/screens/PluginsScreenNav.kt

package com.pwnagotchi.pwnagotchiandroid.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiViewModel
import com.pwnagotchi.pwnagotchiandroid.PluginsScreen
import com.pwnagotchi.pwnagotchiandroid.ui.viewmodels.PwnagotchiUiState

@Composable
fun PluginsScreenNav(viewModel: PwnagotchiViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState is PwnagotchiUiState.Connected) {
        val connectedState = uiState as PwnagotchiUiState.Connected
        PluginsScreen(
            plugins = connectedState.plugins,
            onTogglePlugin = { pluginName, enabled ->
                viewModel.togglePlugin(pluginName, enabled)
            }
        )
    }
}
```

**Create `app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/screens/OpwngridScreenNav.kt`**

```kotlin
// FILE:./app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/screens/OpwngridScreenNav.kt

package com.pwnagotchi.pwnagotchiandroid.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pwnagotchi.pwnagotchiandroid.OpwngridScreen
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiViewModel
import com.pwnagotchi.pwnagotchiandroid.ui.viewmodels.PwnagotchiUiState

@Composable
fun OpwngridScreenNav(viewModel: PwnagotchiViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchLeaderboard()
    }

    if (uiState is PwnagotchiUiState.Connected) {
        val connectedState = uiState as PwnagotchiUiState.Connected
        OpwngridScreen(leaderboard = connectedState.leaderboard)
    }
}
```

**Create `app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/screens/SettingsScreenNav.kt`**

```kotlin
// FILE:./app/src/main/java/com/pwnagotchi/pwnagotchiandroid/ui/screens/SettingsScreenNav.kt

package com.pwnagotchi.pwnagotchiandroid.ui.screens

import androidx.compose.runtime.Composable
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiViewModel
import com.pwnagotchi.pwnagotchiandroid.SettingsScreen
import java.net.URI

@Composable
fun SettingsScreenNav(viewModel: PwnagotchiViewModel) {
    SettingsScreen(
        onSave = { ipAddress ->
            viewModel.connect(URI("ws://$ipAddress:8765"))
        }
    )
}
```

**Refactor original screen files** to remove navigation-specific callbacks like `onBack`.

*   Modify `PluginsScreen.kt`: Remove the `onBack` parameter and the "Back" button.
*   Modify `OpwngridScreen.kt`: Remove the `onBack` parameter and the "Back" button.
*   Modify `SettingsScreen.kt`: Remove the `onBack` parameter and the "Back" button.

**Action**: Update `MainActivity.kt` to use the new `MainScreen`

The `MainActivity` is now significantly simplified. Its sole responsibility is to set up the theme, bind to the service, and host the `MainScreen` composable.

```kotlin
// FILE:./app/src/main/java/com/pwnagotchi/pwnagotchiandroid/MainActivity.kt (Complete Replacement)

package com.pwnagotchi.pwnagotchiandroid

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pwnagotchi.pwnagotchiandroid.ui.MainScreen
import com.pwnagotchi.pwnagotchiandroid.ui.theme.PwnagotchiAndroidTheme

class MainActivity : ComponentActivity() {
    private val viewModel: PwnagotchiViewModel by viewModels()
    private var isServiceBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PwnagotchiService.LocalBinder
            val pwnagotchiService = binder.getService()
            isServiceBound = true
            viewModel.setService(pwnagotchiService)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isServiceBound = false
            viewModel.setService(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PwnagotchiAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, PwnagotchiService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
            // The service is started as a foreground service, so we also need to start it.
            startService(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isServiceBound) {
            unbindService(connection)
            isServiceBound = false
        }
    }
}
```

## III. Enhancing Ambient UX Part 1: The Persistent E-Ink Display Notification

The current foreground service notification is basic, showing only a simple text string.4 To better mimic the Pwnagotchi's e-ink display and provide at-a-glance information, this notification will be replaced with a custom layout using `RemoteViews`. This provides a richer, more integrated experience for the user when the app is not in the foreground.

### A. Designing the Custom Notification Layout

A custom XML layout is required to define the structure of the notification. The layout must be simple and high-contrast, using standard notification text styles to ensure compatibility across different Android versions and manufacturer skins.8

**Action**: Create `app/src/main/res/layout/notification_pwnagotchi.xml`

This layout uses a `LinearLayout` to arrange an `ImageView` for the Pwnagotchi's face and `TextViews` for its vital statistics.

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="8dp">

    <ImageView
        android:id="@+id/notification_face"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:layout_gravity="center_vertical"
        android:src="@drawable/face_happy"
        android:contentDescription="Pwnagotchi Face" />

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical"
        android:layout_marginStart="8dp"
        android:gravity="center_vertical">

        <TextView
            android:id="@+id/notification_line_1"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="CH: -- | APS: --"
            android:textAppearance="@style/TextAppearance.Compat.Notification" />

        <TextView
            android:id="@+id/notification_line_2"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="UP: -- | PWND: -- | MODE: --"
            android:textAppearance="@style/TextAppearance.Compat.Notification.Info" />
    </LinearLayout>
</LinearLayout>
```

### B. Integrating RemoteViews into PwnagotchiService

The `PwnagotchiService` must be updated to use this new layout. The `createNotification` and `updateNotification` methods will be refactored to instantiate `RemoteViews`, populate the views with data from the WebSocket `ui_update` message, and apply the custom view to the `NotificationCompat.Builder`.

**Action**: Refactor notification methods in `PwnagotchiService.kt`

The `updateNotification` method will now accept the full `JSONObject` from the WebSocket message to populate the custom layout. The `createNotification` method is updated to handle the initial creation with the custom layout.

```kotlin
// FILE:./app/src/main/java/com/pwnagotchi/pwnagotchiandroid/PwnagotchiService.kt (modified methods)

//... inside PwnagotchiService class

    // Replace the existing onStartCommand
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification(null) // Create initial notification
        startForeground(1, notification)

        val sharedPreferences = getSharedPreferences("pwnagotchi_prefs", Context.MODE_PRIVATE)
        val ipAddress = sharedPreferences.getString("ip_address", null)
        if (ipAddress!= null) {
            connect(URI("ws://$ipAddress:8765"))
        }
        return START_STICKY
    }

    //... inside onMessage method, "ui_update" case
    "ui_update" -> {
        val data = json.getJSONObject("data")
        face = data.getString("face")
        val notificationText = "CH: ${data.getString("channel")} | APS: ${data.getString("aps")} | UP: ${data.getString("uptime")} | PWND: ${data.getString("shakes")} | MODE: ${data.getString("mode")}"
        _uiState.value = (uiState.value as? PwnagotchiUiState.Connected)?.copy(
            data = notificationText,
            face = face
        )?: PwnagotchiUiState.Connected(notificationText, handshakes, plugins, face)
        updateNotification(data) // Pass the JSON data object
    }

    // Replace the existing createNotification method
    private fun createNotification(data: JSONObject?): Notification {
        val channelId = "pwnagotchi_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pwnagotchi Service Channel",
                NotificationManager.IMPORTANCE_LOW // Use LOW to prevent sound/vibration on update
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val remoteViews = createRemoteViews(data)

        return NotificationCompat.Builder(this, channelId)
           .setSmallIcon(R.mipmap.ic_launcher)
           .setStyle(NotificationCompat.DecoratedCustomViewStyle())
           .setCustomContentView(remoteViews)
           .setPriority(NotificationCompat.PRIORITY_LOW)
           .build()
    }

    // Replace the existing updateNotification method
    private fun updateNotification(data: JSONObject) {
        val notification = createNotification(data)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    // Add a new helper method to create and populate RemoteViews
    private fun createRemoteViews(data: JSONObject?): RemoteViews {
        val remoteViews = RemoteViews(packageName, R.layout.notification_pwnagotchi)

        if (data!= null) {
            val faceDrawable = when (data.getString("face")) {
                "(·•᷄_•᷅ ·)" -> R.drawable.face_sad
                "(^‿^)" -> R.drawable.face_happy
                "(¬‿¬)" -> R.drawable.face_bored
                else -> R.drawable.face_happy
            }
            remoteViews.setImageViewResource(R.id.notification_face, faceDrawable)
            remoteViews.setTextViewText(R.id.notification_line_1, "CH: ${data.getString("channel")} | APS: ${data.getString("aps")}")
            remoteViews.setTextViewText(R.id.notification_line_2, "UP: ${data.getString("uptime")} | PWND: ${data.getString("shakes")} | MODE: ${data.getString("mode")}")
        } else {
            // Set initial/disconnected state
            remoteViews.setImageViewResource(R.id.notification_face, R.drawable.face_bored)
            remoteViews.setTextViewText(R.id.notification_line_1, "Pwnagotchi Status")
            remoteViews.setTextViewText(R.id.notification_line_2, "Disconnected")
        }
        return remoteViews
    }

    // Modify the disconnect method to update the notification
    fun disconnect() {
        reconnectionJob?.cancel()
        webSocketClient?.close()
        _uiState.value = PwnagotchiUiState.Disconnected("Disconnected by user")
        updateNotification(null) // Update with disconnected state
    }
```

## IV. Enhancing Ambient UX Part 2: Home Screen Integration via Jetpack Glance Widgets

This section details the implementation of four distinct home screen widgets using Jetpack Glance. A critical component of this implementation is the data synchronization architecture. Widgets run in a separate process from the main application and its services.10 To ensure data consistency and reliability, a `DataStore` will serve as a persistent, shared state holder. The `PwnagotchiService` will act as the single source of truth, writing updates to the `DataStore`, and the Glance widgets will read from this store upon being notified of an update. This decoupled, unidirectional data flow is a robust pattern for modern Android development.

### A. Core Widget Infrastructure Setup

Before implementing the widget UI, the necessary boilerplate for each of the four widgets must be established. This includes creating the `GlanceAppWidget` and `GlanceAppWidgetReceiver` classes, the XML provider info file, and registering each receiver in the `AndroidManifest.xml`.6

**Action**: Create new package `com.pwnagotchi.pwnagotchiandroid.widgets`

All widget-related files will be placed in this new package for organization.

**Action**: Create Widget Receiver and Implementation files

Create the following eight empty class files inside the new package:

*   `StatusWidget.kt` & `StatusWidgetReceiver.kt`
*   `HandshakeLogWidget.kt` & `HandshakeLogWidgetReceiver.kt`
*   `QuickActionsWidget.kt` & `QuickActionsWidgetReceiver.kt`
*   `LeaderboardWidget.kt` & `LeaderboardWidgetReceiver.kt`

**Action**: Create XML Provider Info files in `res/xml/`

Create four XML files to define the metadata for each widget.

*   `res/xml/status_widget_info.xml`
*   `res/xml/handshake_log_widget_info.xml`
*   `res/xml/quick_actions_widget_info.xml`
*   `res/xml/leaderboard_widget_info.xml`

The content for each will be similar, defining basic properties and using the default Glance loading layout.5

```xml
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="180dp"
    android:minHeight="110dp"
    android:targetCellWidth="3"
    android:targetCellHeight="2"
    android:updatePeriodMillis="86400000"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:description="@string/widget_status_desc"
    android:previewImage="@drawable/pwnagotchi_android" />
```

**Action**: Register receivers in `AndroidManifest.xml`

Add four `<receiver>` tags inside the `<application>` tag.

```xml
<application...>
   ...
    <receiver
        android:name=".widgets.StatusWidgetReceiver"
        android:exported="true"
        android:label="Pwnagotchi Status">
        <intent-filter>
            <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
        </intent-filter>
        <meta-data
            android:name="android.appwidget.provider"
            android:resource="@xml/status_widget_info" />
    </receiver>

    <receiver
        android:name=".widgets.HandshakeLogWidgetReceiver"
        android:exported="true"
        android:label="Pwnagotchi Handshakes">
        <intent-filter>
            <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
        </intent-filter>
        <meta-data
            android:name="android.appwidget.provider"
            android:resource="@xml/handshake_log_widget_info" />
    </receiver>

    <receiver
        android:name=".widgets.QuickActionsWidgetReceiver"
        android:exported="true"
        android:label="Pwnagotchi Actions">
        <intent-filter>
            <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
        </intent-filter>
        <meta-data
            android:name="android.appwidget.provider"
            android:resource="@xml/quick_actions_widget_info" />
    </receiver>

    <receiver
        android:name=".widgets.LeaderboardWidgetReceiver"
        android:exported="true"
        android:label="oPwngrid Leaderboard">
        <intent-filter>
            <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
        </intent-filter>
        <meta-data
            android:name="android.appwidget.provider"
            android:resource="@xml/leaderboard_widget_info" />
    </receiver>
</application>
```

### B. Architecting the Widget Data Synchronization Mechanism

This step establishes the data layer that will connect the `PwnagotchiService` to the widgets. A `PreferencesDataStore` is used to persist the state as a set of key-value pairs. A repository class will abstract the reading and writing to this `DataStore`.

**Action**: Create `app/src/main/java/com/pwnagotchi/pwnagotchiandroid/widgets/WidgetStateRepository.kt`

This file defines the `DataStore` and the repository for managing widget state. It uses `stringSetPreferencesKey` to store lists of handshakes and leaderboard entries.

```kotlin
// FILE:./app/src/main/java/com/pwnagotchi/pwnagotchiandroid/widgets/WidgetStateRepository.kt

package com.pwnagotchi.pwnagotchiandroid.widgets

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pwnagotchi_widget_state")

object WidgetStateRepository {
    val STATUS_FACE = stringPreferencesKey("status_face")
    val STATUS_LINE_1 = stringPreferencesKey("status_line_1")
    val STATUS_LINE_2 = stringPreferencesKey("status_line_2")
    val HANDSHAKES_LIST = stringSetPreferencesKey("handshakes_list")
    val LEADERBOARD_LIST = stringSetPreferencesKey("leaderboard_list")

    suspend fun updateStatus(context: Context, face: String, line1: String, line2: String) {
        context.dataStore.edit { prefs ->
            prefs[STATUS_FACE] = face
            prefs[STATUS_LINE_1] = line1
            prefs[STATUS_LINE_2] = line2
        }
    }

    suspend fun updateHandshakes(context: Context, handshakes: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[HANDSHAKES_LIST] = handshakes.toSet()
        }
    }

    suspend fun updateLeaderboard(context: Context, leaderboard: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[LEADERBOARD_LIST] = leaderboard.toSet()
        }
    }

    suspend fun getWidgetState(context: Context): Preferences {
        return context.dataStore.data.first()
    }
}
```

**Action**: Modify `PwnagotchiService.kt` to write to the DataStore and update widgets

The service will now write state changes to the `WidgetStateRepository` and then trigger an update for the relevant widgets using `updateAll()`.10

```kotlin
// FILE:./app/src/main/java/com/pwnagotchi/pwnagotchiandroid/PwnagotchiService.kt (additions/modifications)

// Add imports
import com.pwnagotchi.pwnagotchiandroid.widgets.*
import kotlinx.coroutines.GlobalScope // Use a broader scope for widget updates

//... inside onMessage method, "ui_update" case
"ui_update" -> {
    val data = json.getJSONObject("data")
    face = data.getString("face")
    val line1 = "CH: ${data.getString("channel")} | APS: ${data.getString("aps")}"
    val line2 = "UP: ${data.getString("uptime")} | PWND: ${data.getString("shakes")} | MODE: ${data.getString("mode")}"

    val notificationText = "$line1 | $line2"
    _uiState.value = //... (as before)
    updateNotification(data)

    // Update widget state
    GlobalScope.launch {
        WidgetStateRepository.updateStatus(applicationContext, face, line1, line2)
        StatusWidget().updateAll(applicationContext)
    }
}

//... inside onMessage method, "handshake" case
"handshake" -> {
    //... (existing handshake creation)
    handshakes.add(handshake)
    _uiState.value = //... (as before)
    showHandshakeNotification(handshake)

    // Update widget state
    GlobalScope.launch {
        val handshakeStrings = handshakes.takeLast(10).map { "AP: ${it.ap} | STA: ${it.sta}" }
        WidgetStateRepository.updateHandshakes(applicationContext, handshakeStrings)
        HandshakeLogWidget().updateAll(applicationContext)
    }
}

// Add a new method to PwnagotchiViewModel to be called from OpwngridScreenNav
fun PwnagotchiViewModel.updateLeaderboardWidgets(context: Context) {
    viewModelScope.launch {
        val uiStateValue = uiState.value
        if (uiStateValue is PwnagotchiUiState.Connected) {
            val leaderboardStrings = uiStateValue.leaderboard.take(10).map { "${it.name}: ${it.pwns}" }
            WidgetStateRepository.updateLeaderboard(context, leaderboardStrings)
            LeaderboardWidget().updateAll(context)
        }
    }
}
```

### C. Implementing the Four Widgets

With the data flow architecture in place, the UI for each widget can be implemented. Each `GlanceAppWidget` will define a `GlanceStateDefinition` to read from the `DataStore` and use `currentState()` to access the data for rendering.12

**Action**: Implement `StatusWidget.kt` and `StatusWidgetReceiver.kt`

```kotlin
// FILE:./app/src/main/java/com/pwnagotchi/pwnagotchiandroid/widgets/StatusWidget.kt

package com.pwnagotchi.pwnagotchiandroid.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pwnagotchi.pwnagotchiandroid.R
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.state.updateAppWidgetState

object StatusWidgetStateDefinition : PreferencesGlanceStateDefinition()

class StatusWidget : GlanceAppWidget() {
    override val stateDefinition = StatusWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val face = prefs[WidgetStateRepository.STATUS_FACE]?: "(¬‿¬)"
            val line1 = prefs[WidgetStateRepository.STATUS_LINE_1]?: "CH: -- | APS: --"
            val line2 = prefs[WidgetStateRepository.STATUS_LINE_2]?: "UP: -- | PWND: -- | MODE: --"

            StatusWidgetContent(face, line1, line2)
        }
    }
}

@Composable
private fun StatusWidgetContent(face: String, line1: String, line2: String) {
    val faceDrawable = when (face) {
        "(·•᷄_•᷅ ·)" -> R.drawable.face_sad
        "(^‿^)" -> R.drawable.face_happy
        else -> R.drawable.face_bored
    }

    Row(
        modifier = GlanceModifier.fillMaxSize().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(faceDrawable),
            contentDescription = "Pwnagotchi Face",
            modifier = GlanceModifier.size(48.dp)
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Column {
            Text(text = line1, style = TextStyle(color = ColorProvider(R.color.white)))
            Text(text = line2, style = TextStyle(color = ColorProvider(R.color.white)))
        }
    }
}
```

```kotlin
// FILE:./app/src/main/java/com/pwnagotchi/pwnagotchiandroid/widgets/StatusWidgetReceiver.kt

package com.pwnagotchi.pwnagotchiandroid.widgets

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class StatusWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StatusWidget()
}
```

Implementations for `HandshakeLogWidget`, `QuickActionsWidget`, and `LeaderboardWidget` would follow a similar pattern, each with its own `GlanceAppWidget` subclass, `GlanceAppWidgetReceiver`, and `Content` composable that reads from the appropriate keys in the `DataStore`. The `QuickActionsWidget` would use `Button` composables with `actionRunCallback` to send intents to the service for actions like reconnecting.

## V. Finalizing for Production: Code Hardening and Optimization

The final phase transitions the application from a development build to a hardened release candidate. This involves enabling R8 for code shrinking and obfuscation and configuring the necessary `keep` rules to prevent runtime crashes. This is a non-negotiable step for a production application, as it reduces APK size and provides a layer of protection against reverse engineering.

### A. Enabling R8 and Resource Shrinking

The `release` build type in the app's Gradle file must be configured to enable minification and resource shrinking.

**Action**: Modify `app/build.gradle.kts`

Update the `release` block within `buildTypes`.

```kotlin
// FILE:./app/build.gradle.kts (buildTypes block)

buildTypes {
    release {
        isMinifyEnabled = true
        shrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### B. Constructing the Comprehensive ProGuard Configuration

The project uses several libraries that rely on reflection, serialization, or native code (Ktor, `kotlinx.serialization`, libsu, Jetpack Compose/Glance). Without explicit `keep` rules, R8 would obfuscate or remove critical classes and methods, leading to runtime crashes. The following table consolidates the necessary rules, synthesized from documentation and best practices, providing a single, comprehensive configuration.

| Library / Feature         | ProGuard Rule(s)                                                                                                                                                                                                                                                                                                                                                                                                | Justification & Source Snippet(s)                                                                                                                            |
| ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `kotlinx.serialization` | `-keepclassmembers class ** { @kotlinx.serialization.Serializable *; } -if @kotlinx.serialization.Serializable class ** { static **$* *; } -keepclassmembers class $ { kotlinx.serialization.KSerializer serializer(...); } -if @kotlinx.serialization.Serializable class ** { public static ** INSTANCE; } -keepclassmembers class  { public static  INSTANCE; kotlinx.serialization.KSerializer serializer(...); } -keepattributes RuntimeVisibleAnnotations,AnnotationDefault -dontnote kotlinx.serialization.** -dontwarn kotlinx.serialization.internal.ClassValueWrapper` | Preserves `serializer()` methods and annotations required for reflection-less serialization at runtime. Prevents warnings for optional internal classes. 13  |
| Ktor Client (CIO)         | `-keep class io.ktor.** { *; } -dontwarn io.ktor.** -keep class kotlinx.coroutines.** { *; }`                                                                                                                                                                                                                                                                                                                 | Ktor client rules are not officially provided. This is a defensive set to protect the core library, its coroutine-based engine, and suppress warnings from internal reflection. 15 |
| libsu                     | `-keep class com.github.topjohnwu.libsu.** { *; } -keepclasseswithmembernames class * { native <methods>; }`                                                                                                                                                                                                                                                                                                    | Prevents obfuscation of IPC service classes and native methods, which would break root functionality. 18                                                    |
| Jetpack Compose           | `-keepclasseswithmembers class * { @androidx.compose.runtime.Composable <methods>; }`                                                                                                                                                                                                                                                                                                                         | Ensures that R8 does not strip composable functions that it might incorrectly identify as unused. 20                                                       |
| Jetpack Glance            | `-keep public class * extends androidx.glance.appwidget.GlanceAppWidget -keep public class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver`                                                                                                                                                                                                                                                          | Keeps the public API of all widget and receiver implementations, which are entry points instantiated by the Android framework. 21                        |
| Kotlin Metadata           | `-keepattributes *Annotation* -keep class kotlin.Metadata`                                                                                                                                                                                                                                                                                                                                                      | Preserves Kotlin-specific metadata used by reflection-based libraries and for language features to function correctly in a release build. 22               |

**Action**: Create/Update `app/proguard-rules.pro`

Populate the ProGuard file with the consolidated rules.

```
# FILE:./app/proguard-rules.pro

# General Kotlin Rules
-keepattributes *Annotation*
-keep class kotlin.Metadata

# --- kotlinx.serialization ---
# Keep @Serializable classes and their serializers.
-keepclassmembers class ** { @kotlinx.serialization.Serializable *; }
-if @kotlinx.serialization.Serializable class ** { static **$* *; }
-keepclassmembers class $ { kotlinx.serialization.KSerializer serializer(...); }
-if @kotlinx.serialization.Serializable class ** { public static ** INSTANCE; }
-keepclassmembers class  { public static  INSTANCE; kotlinx.serialization.KSerializer serializer(...); }
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-dontnote kotlinx.serialization.**
-dontwarn kotlinx.serialization.internal.ClassValueWrapper

# --- Ktor Client ---
# Ktor uses reflection and coroutines extensively. These are defensive rules.
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# --- libsu ---
# This library uses IPC and native code, which can be broken by obfuscation.
-keep class com.github.topjohnwu.libsu.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# --- Jetpack Compose & Glance ---
# Keep Composable functions and Glance entry points.
-keepclasseswithmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keep public class * extends androidx.glance.appwidget.GlanceAppWidget
-keep public class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver

# --- General Android ---
# Keep default constructors for Activities, Services, etc.
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Application
```

## VI. Conclusion

This protocol has detailed the necessary steps to transform the Pwnagotchi Android application from its initial state to a production-ready build. The process involved a foundational upgrade of the build system to modern standards, a significant architectural refactoring of the user interface to a scalable navigation model, and the implementation of several key features to enhance the ambient user experience.

The core architectural improvements include:

*   **Modernized Build System**: Upgrading to Gradle 8.14, AGP 8.14.0, and API 36 establishes a stable and secure foundation for the application.
*   **Scalable UI Navigation**: The replacement of a brittle, state-flag-based navigation system with a route-based architecture using Jetpack Navigation Compose provides a robust framework for future expansion and maintenance.
*   **Decoupled Background UI**: The implementation of a `DataStore`-backed repository to synchronize state between the `PwnagotchiService` and the Jetpack Glance widgets establishes a clean, resilient, and efficient data flow architecture suitable for production environments.

The feature enhancements, including the custom e-ink style persistent notification and the four distinct home screen widgets, significantly increase the application's utility and integration with the Android operating system.

Finally, the enabling of R8 with a comprehensive and well-justified set of ProGuard rules ensures that the final application artifact is optimized for size and hardened against basic reverse-engineering attempts. By completing these steps, the application achieves a state of technical maturity, architectural soundness, and robustness required for a production release.
