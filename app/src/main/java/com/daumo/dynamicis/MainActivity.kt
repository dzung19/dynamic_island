package com.daumo.dynamicis

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.daumo.dynamicis.navigation.navigateSingleTopTo
import com.daumo.dynamicis.island.IslandSettings
import com.daumo.dynamicis.model.DISCLOSURE_ACCEPTED
import com.daumo.dynamicis.model.SETTINGS_KEY
import com.daumo.dynamicis.model.SETTINGS_THEME_INVERTED
import com.daumo.dynamicis.model.THEME_INVERTED
import com.daumo.dynamicis.navigation.IslandDestination
import com.daumo.dynamicis.navigation.IslandHome
import com.daumo.dynamicis.navigation.IslandNavHost
import com.daumo.dynamicis.navigation.IslandPluginSettings
import com.daumo.dynamicis.navigation.IslandPlugins
import com.daumo.dynamicis.navigation.bottomDestinations
import com.daumo.dynamicis.navigation.navigateSingleTopTo
import com.daumo.dynamicis.plugins.ExportedPlugins
import com.daumo.dynamicis.ui.settings.settings
import com.daumo.dynamicis.ui.theme.DynamicIslandTheme
import com.daumo.dynamicis.ui.theme.Theme


class MainActivity : ComponentActivity() {

	private lateinit var settingsPreferences: SharedPreferences

	companion object {
		lateinit var instance: MainActivity
	}

	var actions = mutableStateListOf<@Composable () -> Unit>()

	@RequiresApi(Build.VERSION_CODES.S)
    @OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		instance = this

		settingsPreferences = getSharedPreferences(_root_ide_package_.com.daumo.dynamicis.model.SETTINGS_KEY, Context.MODE_PRIVATE)

		WindowCompat.setDecorFitsSystemWindows(window, false)

		// Invert theme in app
		settingsPreferences.edit().putBoolean(_root_ide_package_.com.daumo.dynamicis.model.THEME_INVERTED, true).apply()
		sendBroadcast(Intent(_root_ide_package_.com.daumo.dynamicis.model.SETTINGS_THEME_INVERTED))

		setContent {
			// Setup plugins
			_root_ide_package_.com.daumo.dynamicis.plugins.ExportedPlugins.Companion.setupPlugins(LocalContext.current)

			// Init
			_root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.Init()
			_root_ide_package_.com.daumo.dynamicis.island.IslandSettings.Companion.instance.loadSettings(this)

			val disclosureAccepted by remember { mutableStateOf(settingsPreferences.getBoolean(
				_root_ide_package_.com.daumo.dynamicis.model.DISCLOSURE_ACCEPTED, false)
			) }

			if (!disclosureAccepted) {
				startActivity(Intent(this, DisclosureActivity::class.java))
				finish()
			}

			_root_ide_package_.com.daumo.dynamicis.ui.theme.DynamicIslandTheme(
				darkTheme = _root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.isDarkTheme,
			) {
				// A surface container using the 'background' color from the theme
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					// Navigation
					val settingsRoutes =
						_root_ide_package_.com.daumo.dynamicis.ui.settings.settings.map { (it as com.daumo.dynamicis.navigation.IslandDestination).route }

					val navController = rememberNavController()
					val currentBackStack by navController.currentBackStackEntryAsState()
					val currentDestination = currentBackStack?.destination
					val currentScreen: com.daumo.dynamicis.navigation.IslandDestination =
						_root_ide_package_.com.daumo.dynamicis.navigation.bottomDestinations.find { it.route == currentDestination?.route }
							?:
							// If current destination is contained in settings
							(_root_ide_package_.com.daumo.dynamicis.ui.settings.settings.find { (it as com.daumo.dynamicis.navigation.IslandDestination).route == currentDestination?.route }
								?: if (currentDestination?.route == _root_ide_package_.com.daumo.dynamicis.navigation.IslandPluginSettings.routeWithArgs) _root_ide_package_.com.daumo.dynamicis.navigation.IslandPluginSettings else _root_ide_package_.com.daumo.dynamicis.navigation.IslandHome
									) as com.daumo.dynamicis.navigation.IslandDestination

					// Top app bar
					val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

					LaunchedEffect(currentScreen) {
						actions.clear()
					}

					Scaffold(
						modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
						topBar = {
							CenterAlignedTopAppBar(
								title = {
									Crossfade(
										targetState = currentScreen,
									) { screen ->
										Text(
											text = if (screen == IslandHome) {
												stringResource(
													id = R.string.app_name)
											} else {
												screen.title
											},
											textAlign = TextAlign.Center,
											modifier = Modifier
												.fillMaxWidth()
										)
									}
								},
								navigationIcon = {
									if (
										currentDestination?.route in settingsRoutes
										|| currentDestination?.route == _root_ide_package_.com.daumo.dynamicis.navigation.IslandPluginSettings.routeWithArgs
									) {
										IconButton(onClick = { navController.popBackStack() }) {
											Icon(
												imageVector = Icons.Default.ArrowBack,
												contentDescription = "Back"
											)
										}
									}
								},
								actions = {
									actions.forEach { it() }
								},
								scrollBehavior = scrollBehavior
							)
						},
						bottomBar = {
							NavigationBar {
								for (destination in _root_ide_package_.com.daumo.dynamicis.navigation.bottomDestinations) {
									NavigationBarItem(
										icon = { Icon(destination.icon, contentDescription = null) },
										label = { Text(destination.title) },
										selected = currentScreen == destination
												|| (destination == _root_ide_package_.com.daumo.dynamicis.navigation.IslandSettings && _root_ide_package_.com.daumo.dynamicis.ui.settings.settings.contains(
											currentScreen))
												|| (destination == _root_ide_package_.com.daumo.dynamicis.navigation.IslandPlugins && currentScreen == _root_ide_package_.com.daumo.dynamicis.navigation.IslandPluginSettings),
										onClick = {
											navController.navigateSingleTopTo(destination.route)
										}
									)
								}
							}
						},
					) {
						_root_ide_package_.com.daumo.dynamicis.navigation.IslandNavHost(
							modifier = Modifier
								.padding(it)
								.fillMaxSize(),
							navController = navController
						)
					}
				}
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		// Un-invert theme in app
		settingsPreferences.edit().putBoolean(_root_ide_package_.com.daumo.dynamicis.model.THEME_INVERTED, false).apply()
		sendBroadcast(Intent(_root_ide_package_.com.daumo.dynamicis.model.SETTINGS_THEME_INVERTED))
	}

	override fun onStop() {
		super.onStop()
		// Un-invert theme in app
		settingsPreferences.edit().putBoolean(_root_ide_package_.com.daumo.dynamicis.model.THEME_INVERTED, false).apply()
		sendBroadcast(Intent(_root_ide_package_.com.daumo.dynamicis.model.SETTINGS_THEME_INVERTED))
	}

	override fun onPause() {
		super.onPause()
		// Un-invert theme in app
		settingsPreferences.edit().putBoolean(_root_ide_package_.com.daumo.dynamicis.model.THEME_INVERTED, false).apply()
		sendBroadcast(Intent(_root_ide_package_.com.daumo.dynamicis.model.SETTINGS_THEME_INVERTED))
	}

	override fun onResume() {
		super.onResume()
		// Invert theme in app
		settingsPreferences.edit().putBoolean(_root_ide_package_.com.daumo.dynamicis.model.THEME_INVERTED, true).apply()
		sendBroadcast(Intent(_root_ide_package_.com.daumo.dynamicis.model.SETTINGS_THEME_INVERTED))
	}
}