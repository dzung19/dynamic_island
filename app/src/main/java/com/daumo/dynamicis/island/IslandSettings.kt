package com.daumo.dynamicis.island

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.daumo.dynamicis.model.*

class IslandSettings {

	companion object {
		val instance = IslandSettings()
	}

	var positionX by mutableIntStateOf(0)
	var positionY by mutableIntStateOf(0)
	var width by mutableIntStateOf(150)
	var height by mutableIntStateOf(200)
	var cornerRadius by mutableIntStateOf(60)
	var gravity by mutableStateOf(IslandGravity.Center)

	var enabledApps = mutableStateListOf<String>()

	var showOnLockScreen by mutableStateOf(false)
	var showInLandscape by mutableStateOf(false)
	var showBorders by mutableStateOf(false)

	var autoHideOpenedAfter by mutableFloatStateOf(5000f)

	fun applySettings(context: Context) {
		val settings = context.getSharedPreferences(_root_ide_package_.com.daumo.dynamicis.model.SETTINGS_KEY, Context.MODE_PRIVATE)
		settings.edit()
			.putInt(_root_ide_package_.com.daumo.dynamicis.model.POSITION_X, positionX)
			.putInt(_root_ide_package_.com.daumo.dynamicis.model.POSITION_Y, positionY)
			.putInt(_root_ide_package_.com.daumo.dynamicis.model.SIZE_X, width)
			.putInt(_root_ide_package_.com.daumo.dynamicis.model.SIZE_Y, height)
			.putInt(_root_ide_package_.com.daumo.dynamicis.model.CORNER_RADIUS, cornerRadius)
			.putStringSet(_root_ide_package_.com.daumo.dynamicis.model.ENABLED_APPS, enabledApps.toSet())
			.putBoolean(_root_ide_package_.com.daumo.dynamicis.model.SHOW_ON_LOCK_SCREEN, showOnLockScreen)
			.putBoolean(_root_ide_package_.com.daumo.dynamicis.model.SHOW_IN_LANDSCAPE, showInLandscape)
			.putFloat(_root_ide_package_.com.daumo.dynamicis.model.AUTO_HIDE_OPENED_AFTER, autoHideOpenedAfter)
			.putBoolean(_root_ide_package_.com.daumo.dynamicis.model.SHOW_BORDER, showBorders)
			.putString(_root_ide_package_.com.daumo.dynamicis.model.GRAVITY, gravity.name)
			.apply()
	}

	fun loadSettings(context: Context) {
		val settings = context.getSharedPreferences(_root_ide_package_.com.daumo.dynamicis.model.SETTINGS_KEY, Context.MODE_PRIVATE)
		positionX = settings.getInt(_root_ide_package_.com.daumo.dynamicis.model.POSITION_X, 0)
		positionY = settings.getInt(_root_ide_package_.com.daumo.dynamicis.model.POSITION_Y, 5)
		width = settings.getInt(_root_ide_package_.com.daumo.dynamicis.model.SIZE_X, 150)
		height = settings.getInt(_root_ide_package_.com.daumo.dynamicis.model.SIZE_Y, 200)
		cornerRadius = settings.getInt(_root_ide_package_.com.daumo.dynamicis.model.CORNER_RADIUS, 60)
		enabledApps.clear()
		enabledApps.addAll(settings.getStringSet(_root_ide_package_.com.daumo.dynamicis.model.ENABLED_APPS, setOf()) ?: setOf())
		showOnLockScreen = settings.getBoolean(_root_ide_package_.com.daumo.dynamicis.model.SHOW_ON_LOCK_SCREEN, false)
		showInLandscape = settings.getBoolean(_root_ide_package_.com.daumo.dynamicis.model.SHOW_IN_LANDSCAPE, false)
		autoHideOpenedAfter = settings.getFloat(_root_ide_package_.com.daumo.dynamicis.model.AUTO_HIDE_OPENED_AFTER, 5000f)
		showBorders = settings.getBoolean(_root_ide_package_.com.daumo.dynamicis.model.SHOW_BORDER, false)
		gravity = IslandGravity.valueOf(settings.getString(_root_ide_package_.com.daumo.dynamicis.model.GRAVITY, IslandGravity.Center.name) ?: IslandGravity.Center.name)
	}
}

enum class IslandGravity {
	Left,
	Right,
	Center
}