package com.pwnagotchi.pwnagotchiandroid.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.az.aznavrail.AzNavRail
import com.pwnagotchi.pwnagotchiandroid.*
import com.pwnagotchi.pwnagotchiandroid.ui.navigation.Screen
import com.pwnagotchi.pwnagotchiandroid.ui.theme.PwnagotchiAndroidTheme
import com.pwnagotchi.pwnagotchiandroid.viewmodels.OpwngridViewModel
import com.pwnagotchi.pwnagotchiandroid.viewmodels.PluginsViewModel
import com.pwnagotchi.pwnagotchiandroid.viewmodels.SettingsViewModel

@Composable
fun MainScreen(viewModel: PwnagotchiViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navRailItems = listOf(
        Screen.Home,
        Screen.Plugins,
        Screen.Opwngrid,
        Screen.Settings
    )

    PwnagotchiAndroidTheme {
        Row(Modifier.fillMaxSize()) {
            AzNavRail(
                items = navRailItems,
                selectedItem = navRailItems.find { it.route == currentDestination?.route },
                onItemClick = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            NavHost(navController = navController, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) {
                    HomeScreen(viewModel)
                }
                composable(Screen.Plugins.route) {
                    val pluginsViewModel: PluginsViewModel = viewModel()
                    PluginsScreen(pluginsViewModel)
                }
                composable(Screen.Opwngrid.route) {
                    val opwngridViewModel: OpwngridViewModel = viewModel(factory = OpwngridViewModelFactory(viewModel.getApplication()))
                    OpwngridScreen(opwngridViewModel)
                }
                composable(Screen.Settings.route) {
                    val settingsViewModel: SettingsViewModel = viewModel()
                    SettingsScreen(settingsViewModel)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: PwnagotchiViewModel) {
    val appMode by viewModel.appMode.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ModeSelector(
            currentMode = appMode,
            onModeChange = { viewModel.onModeChange(it) }
        )

        when (appMode) {
            AppMode.REMOTE -> {
                val pwnagotchiScreenViewModel: com.pwnagotchi.pwnagotchiandroid.viewmodels.PwnagotchiViewModel = viewModel()
                PwnagotchiScreen(pwnagotchiScreenViewModel)
            }
            AppMode.LOCAL -> {
                RootControls(
                    onStartAgent = { viewModel.startLocalAgent() },
                    onStopAgent = { viewModel.stopLocalAgent() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelector(
    currentMode: AppMode,
    onModeChange: (AppMode) -> Unit
) {
    SegmentedButtonRow {
        SegmentedButton(
            selected = currentMode == AppMode.REMOTE,
            onClick = { onModeChange(AppMode.REMOTE) },
            shape = SegmentedButtonDefaults.shape(position = 0, count = 2)
        ) {
            Text("Remote")
        }
        SegmentedButton(
            selected = currentMode == AppMode.LOCAL,
            onClick = { onModeChange(AppMode.LOCAL) },
            shape = SegmentedButtonDefaults.shape(position = 1, count = 2)
        ) {
            Text("Local")
        }
    }
}

@Composable
fun RootControls(
    onStartAgent: () -> Unit,
    onStopAgent: () -> Unit
) {
    Column {
        Button(onClick = onStartAgent) {
            Text("Start Local Agent")
        }
        Button(onClick = onStopAgent) {
            Text("Stop Local Agent")
        }
    }
}