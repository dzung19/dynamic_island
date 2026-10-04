package com.daumo.dynamicis.plugins.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.daumo.dynamicis.R
import com.daumo.dynamicis.island.IslandViewState
import com.daumo.dynamicis.model.BATTERY_SHOW_PERCENTAGE
import com.daumo.dynamicis.model.service.IslandOverlayService
import com.daumo.dynamicis.plugins.BasePlugin
import com.daumo.dynamicis.plugins.PluginSettingsItem
import com.daumo.dynamicis.ui.animation.WavesLoadingIndicator
import com.daumo.dynamicis.ui.theme.BatteryEmpty
import com.daumo.dynamicis.ui.theme.BatteryFull

class BatteryPlugin(
	override val id: String = "BatteryPlugin",
	override val name: String = "Battery",
	override val description: String = "Show the current battery level when charging",
	override val permissions: ArrayList<String> = arrayListOf(),
	override var enabled: MutableState<Boolean> = mutableStateOf(false),
	override var pluginSettings: MutableMap<String, PluginSettingsItem> = mutableMapOf(
		BATTERY_SHOW_PERCENTAGE to PluginSettingsItem.SwitchSettingsItem(
			title = "Show percentage",
			description = "Show the battery percentage",
			id = BATTERY_SHOW_PERCENTAGE,
			value = mutableStateOf(true),
		),
	),
) : BasePlugin() {

	private lateinit var context: IslandOverlayService
	var batteryPercent by mutableIntStateOf(0)

	private fun updateBatteryStatus(intent: Intent) {
		val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
		val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
			status == BatteryManager.BATTERY_STATUS_FULL

		if (isCharging) {
			this@BatteryPlugin.context.addPlugin(this@BatteryPlugin)
			val batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
			val maxBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
			if (maxBatteryLevel > 0) {
				batteryPercent = (batteryLevel * 100 / maxBatteryLevel)
			}
		} else {
			this@BatteryPlugin.context.removePlugin(this@BatteryPlugin)
		}
	}

	private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			updateBatteryStatus(intent)
		}
	}

	override fun canExpand(): Boolean { return true }

	override fun onCreate(context: IslandOverlayService?) {
		this.context = context ?: return
		val stickyIntent = context.registerReceiver(mBroadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
		stickyIntent?.let { updateBatteryStatus(it) }

		// Check for plugin internal settings
		pluginSettings.values.forEach {
			if (it is PluginSettingsItem.SwitchSettingsItem) {
				it.value.value = it.isSettingEnabled(context, it.id)
			}
		}
	}

	@Composable
	override fun BackgroundComposable() {
		val batteryFraction = (batteryPercent.toFloat() / 100f).coerceIn(0.15f, 1f)
		val animatedProgress by animateFloatAsState(
			targetValue = batteryFraction,
			animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
			label = "battery_bg_progress"
		)
		val batteryColor = pointBetweenColors(BatteryEmpty, BatteryFull, (batteryPercent.toFloat() / 100f).coerceIn(0f, 1f))

		WavesLoadingIndicator(
			modifier = Modifier
				.fillMaxSize()
				.alpha(0.55f),
			color = batteryColor,
			progress = animatedProgress
		)
	}

	@Composable
	override fun Composable() {
		BatteryView(batteryPercent)
	}

	@Composable
	private fun BatteryView(
		batteryPercent: Int
	) {
		val batteryFraction = (batteryPercent.toFloat() / 100f).coerceIn(0f, 1f)
		val batteryColor = pointBetweenColors(BatteryEmpty, BatteryFull, batteryFraction)

		Row(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 20.dp, vertical = 12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(12.dp)
			) {
				Box(
					modifier = Modifier
						.size(44.dp)
						.clip(CircleShape)
						.background(batteryColor.copy(alpha = 0.25f)),
					contentAlignment = Alignment.Center
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_charging_full),
						contentDescription = null,
						tint = batteryColor,
						modifier = Modifier.size(26.dp)
					)
				}
				Column(
					verticalArrangement = Arrangement.Center
				) {
					Text(
						text = if (batteryPercent >= 100) "Fully Charged" else "Charging",
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onSurface
					)
					Text(
						text = if (batteryPercent >= 100) "Unplug charger" else "Battery level",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
					)
				}
			}

			Text(
				text = "$batteryPercent%",
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
	}

	@Composable
	override fun LeftOpenedComposable() {
		val batteryFraction = (batteryPercent.toFloat() / 100f).coerceIn(0f, 1f)
		val batteryColor = pointBetweenColors(BatteryEmpty, BatteryFull, batteryFraction)

		Icon(
			painter = painterResource(id = R.drawable.ic_charging_full),
			contentDescription = "Battery level: $batteryPercent%",
			tint = batteryColor,
			modifier = Modifier
				.fillMaxHeight()
				.padding(start = 8.dp)
				.size(20.dp)
		)
	}

	override fun onClick() {
		if (context.islandState is IslandViewState.Expanded) {
			context.shrink()
		} else {
			try {
				val intent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
					flags = Intent.FLAG_ACTIVITY_NEW_TASK
				}
				context.startActivity(intent)
			} catch (_: Exception) {}
		}
	}

	override fun onLeftSwipe() {}
	override fun onRightSwipe() {}

	@Composable
	override fun RightOpenedComposable() {
		val showPercentage = (pluginSettings[BATTERY_SHOW_PERCENTAGE] as? PluginSettingsItem.SwitchSettingsItem)?.value?.value ?: true
		if (showPercentage) {
			Text(
				text = "$batteryPercent%",
				modifier = Modifier.padding(end = 8.dp),
				style = MaterialTheme.typography.labelLarge,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
	}

	override fun onDestroy() {
		if (!::context.isInitialized) return
		try {
			context.unregisterReceiver(mBroadcastReceiver)
		} catch (_: Exception) {} // Ignore exception if receiver is not registered
	}

	@Composable
	override fun PermissionsRequired() {

	}
}

fun pointBetweenColors(from: Float, to: Float, percent: Float): Float =
	from + percent * (to - from)

fun pointBetweenColors(from: Color, to: Color, percent: Float) =
	Color(
		red = pointBetweenColors(from.red, to.red, percent),
		green = pointBetweenColors(from.green, to.green, percent),
		blue = pointBetweenColors(from.blue, to.blue, percent),
		alpha = pointBetweenColors(from.alpha, to.alpha, percent),
	)