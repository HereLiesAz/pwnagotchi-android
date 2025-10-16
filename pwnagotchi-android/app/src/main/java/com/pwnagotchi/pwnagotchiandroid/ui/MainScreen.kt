package com.pwnagotchi.pwnagotchiandroid.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiScreen
import com.pwnagotchi.pwnagotchiandroid.PwnagotchiViewModel
import com.pwnagotchi.pwnagotchiandroid.PluginsScreen
import com.pwnagotchi.pwnagotchiandroid.SettingsScreen
import com.pwnagotchi.pwnagotchiandroid.ui.navigation.Screen
import com.pwnagotchi.pwnagotchiandroid.ui.theme.PwnagotchiAndroidTheme
import com.pwnagotchi.pwnagotchiandroid.OpwngridScreen
import com.pwnagotchi.pwnagotchiandroid.viewmodels.OpwngridViewModel
import com.pwnagotchi.pwnagotchiandroid.viewmodels.PluginsViewModel
import com.pwnagotchi.pwnagotchiandroid.viewmodels.SettingsViewModel
import com.pwnagotchi.pwnagotchiandroid.viewmodels.PwnagotchiViewModel as PwnagotchiScreenViewModel
import com.az.aznavrail.AzNavRail
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pwnagotchi.pwnagotchiandroid.OpwngridViewModelFactory

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
                    val pwnagotchiScreenViewModel: PwnagotchiScreenViewModel = viewModel()
                    PwnagotchiScreen(pwnagotchiScreenViewModel)
                }
                composable(Screen.Plugins.route) {
                    val pluginsViewModel: PluginsViewModel = viewModel()
                    PluginsScreen(pluginsViewModel)
                }
                composable(Screen.Opwngrid.route) {
                    val opwngridViewModel: OpwngridViewModel = viewModel(factory = OpwngridViewModelFactory(viewModel.getApplication() as android.app.Application))
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