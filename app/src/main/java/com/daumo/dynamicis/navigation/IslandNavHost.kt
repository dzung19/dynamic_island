package com.daumo.dynamicis.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.daumo.dynamicis.plugins.ExportedPlugins
import com.daumo.dynamicis.plugins.PluginSettingsScreen
import com.daumo.dynamicis.ui.home.HomeScreen
import com.daumo.dynamicis.ui.plugins.PluginScreen
import com.daumo.dynamicis.ui.settings.*
import com.daumo.dynamicis.ui.settings.pages.*

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun IslandNavHost(
	modifier: Modifier = Modifier,
	navController: NavHostController,
) {
	NavHost(
		navController = navController,
		startDestination = bottomDestinations.first().route,
		modifier = modifier,
	) {
		// Main destinations
		composable(IslandHome.route) {
            _root_ide_package_.com.daumo.dynamicis.ui.home.HomeScreen(
                onGetStartedClick = {
                    navController.navigateSingleTopTo(IslandPlugins.route)
                },
                onShowDisclosureClick = {
                    navController.navigateSingleTopTo(
                        _root_ide_package_.com.daumo.dynamicis.ui.settings.AboutSetting.route)
                },
            )
		}
		composable(IslandPlugins.route) {
            _root_ide_package_.com.daumo.dynamicis.ui.plugins.PluginScreen(
                onPluginClicked = { plugin ->
                    navController.navigateToPluginSettings(plugin.id)
                }
            )
		}
		composable(IslandSettings.route) {
            _root_ide_package_.com.daumo.dynamicis.ui.settings.SettingsScreen(
                onSettingClicked = { setting ->
                    navController.navigate(setting.route)
                }
            )
		}
		// Settings screens
		composable(_root_ide_package_.com.daumo.dynamicis.ui.settings.ThemeSetting.route) {
            _root_ide_package_.com.daumo.dynamicis.ui.settings.pages.ThemeSettingsScreen()
		}
		composable(_root_ide_package_.com.daumo.dynamicis.ui.settings.BehaviorSetting.route) {
            _root_ide_package_.com.daumo.dynamicis.ui.settings.pages.BehaviorSettingsScreen()
		}
		composable(_root_ide_package_.com.daumo.dynamicis.ui.settings.PositionSizeSetting.route) {
            _root_ide_package_.com.daumo.dynamicis.ui.settings.pages.PositionSizeSettingsScreen()
		}
		composable(_root_ide_package_.com.daumo.dynamicis.ui.settings.EnabledAppsSetting.route) {
            _root_ide_package_.com.daumo.dynamicis.ui.settings.pages.EnabledAppsSettingsScreen()
		}
		composable(_root_ide_package_.com.daumo.dynamicis.ui.settings.AboutSetting.route) {
            _root_ide_package_.com.daumo.dynamicis.ui.settings.pages.AboutSettingsScreen()
		}

		// Plugin settings
		composable(
			route = IslandPluginSettings.routeWithArgs,
			arguments = IslandPluginSettings.arguments,
			deepLinks = IslandPluginSettings.deepLinks,
		) { backStackEntry ->
			val pluginId = backStackEntry.arguments?.getString(IslandPluginSettings.pluginArg)

			if (pluginId != null) {
				PluginSettingsScreen(
					plugin = _root_ide_package_.com.daumo.dynamicis.plugins.ExportedPlugins.Companion.getPlugin(pluginId)
				)
			}
		}
	}
}

fun NavHostController.navigateSingleTopTo(route: String) =
	this.navigate(route) {
		// Pop up to the start destination of the graph to
		// avoid building up a large stack of destinations
		// on the back stack as users select items
		popUpTo(
			this@navigateSingleTopTo.graph.findStartDestination().id
		) {
			saveState = true
		}
		// Avoid multiple copies of the same destination when
		// reselecting the same item
		launchSingleTop = true
		// Restore state when reselecting a previously selected item
		restoreState = true
	}

fun NavHostController.navigateToPluginSettings(pluginId: String) {
	this.navigate(route = "${IslandPluginSettings.route}/$pluginId") {
		popUpTo(IslandSettings.route) {
			saveState = true
		}
		// Avoid multiple copies of the same destination when
		// reselecting the same item
		launchSingleTop = true
	}
}