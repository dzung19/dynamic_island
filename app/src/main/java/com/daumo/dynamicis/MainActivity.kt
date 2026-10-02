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
import androidx.core.content.edit


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

		settingsPreferences = getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)

		WindowCompat.setDecorFitsSystemWindows(window, false)

		// Invert theme in app
		settingsPreferences.edit { putBoolean(THEME_INVERTED, true) }
		sendBroadcast(Intent(SETTINGS_THEME_INVERTED).setPackage(packageName))

		setContent {
			// Setup plugins
			ExportedPlugins.Companion.setupPlugins(LocalContext.current)

			// Init
			Theme.Companion.instance.Init()
			IslandSettings.Companion.instance.loadSettings(this)

			val disclosureAccepted by remember { mutableStateOf(settingsPreferences.getBoolean(
				DISCLOSURE_ACCEPTED, false)
			) }

			if (!disclosureAccepted) {
				startActivity(Intent(this, DisclosureActivity::class.java))
				finish()
			}

			DynamicIslandTheme(
				darkTheme = Theme.Companion.instance.isDarkTheme,
			) {
				// A surface container using the 'background' color from the theme
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					// Navigation
					val settingsRoutes =
						settings.map { (it as IslandDestination).route }

					val navController = rememberNavController()
					val currentBackStack by navController.currentBackStackEntryAsState()
					val currentDestination = currentBackStack?.destination
					val currentScreen: IslandDestination =
						bottomDestinations.find { it.route == currentDestination?.route }
							?:
							// If current destination is contained in settings
							(settings.find { (it as IslandDestination).route == currentDestination?.route }
								?: if (currentDestination?.route == IslandPluginSettings.routeWithArgs) IslandPluginSettings else IslandHome
									) as IslandDestination

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
										|| currentDestination?.route == IslandPluginSettings.routeWithArgs
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
								for (destination in bottomDestinations) {
									NavigationBarItem(
										icon = { Icon(destination.icon, contentDescription = null) },
										label = { Text(destination.title) },
										selected = currentScreen == destination
												|| (destination == IslandSettings && settings.contains(
											currentScreen))
												|| (destination == IslandPlugins && currentScreen == IslandPluginSettings),
										onClick = {
											navController.navigateSingleTopTo(destination.route)
										}
									)
								}
							}
						},
					) {
						IslandNavHost(
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
		settingsPreferences.edit { putBoolean(THEME_INVERTED, false) }
		sendBroadcast(Intent(SETTINGS_THEME_INVERTED).setPackage(packageName))
	}

	override fun onStop() {
		super.onStop()
		// Un-invert theme in app
		settingsPreferences.edit { putBoolean(THEME_INVERTED, false) }
		sendBroadcast(Intent(SETTINGS_THEME_INVERTED).setPackage(packageName))
	}

	override fun onPause() {
		super.onPause()
		// Un-invert theme in app
		settingsPreferences.edit { putBoolean(THEME_INVERTED, false) }
		sendBroadcast(Intent(SETTINGS_THEME_INVERTED).setPackage(packageName))
	}

	override fun onResume() {
		super.onResume()
		// Invert theme in app
		settingsPreferences.edit { putBoolean(THEME_INVERTED, true) }
		sendBroadcast(Intent(SETTINGS_THEME_INVERTED).setPackage(packageName))
	}
}