package com.daumo.dynamicis.ui.settings.pages

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.daumo.dynamicis.model.SETTINGS_KEY
import com.daumo.dynamicis.model.STYLE
import com.daumo.dynamicis.model.THEME
import com.daumo.dynamicis.island.IslandSettings
import com.daumo.dynamicis.ui.settings.SettingsDivider
import com.daumo.dynamicis.ui.settings.radioOptions
import com.daumo.dynamicis.ui.theme.Theme

@Composable
fun ThemeSettingsScreen() {

	val context = LocalContext.current

	val isSystemInDarkTheme = isSystemInDarkTheme()

	// Shared Preferences
	val settingsPreferences = context.getSharedPreferences(_root_ide_package_.com.daumo.dynamicis.model.SETTINGS_KEY, Context.MODE_PRIVATE)

	val (themeSelectedOption, onThemeOptionSelected) = remember { mutableStateOf(settingsPreferences.getString(
        _root_ide_package_.com.daumo.dynamicis.model.THEME, "System")) }
	val (styleSelectedOption, onStyleOptionSelected) = remember { mutableStateOf(
        _root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.themeStyle) }


	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		SwitchSettingsItem(
			title = "Show borders",
			description = "Show borders around the island",
			checked = _root_ide_package_.com.daumo.dynamicis.island.IslandSettings.Companion.instance.showBorders
		) {
			_root_ide_package_.com.daumo.dynamicis.island.IslandSettings.Companion.instance.showBorders = it
			_root_ide_package_.com.daumo.dynamicis.island.IslandSettings.Companion.instance.applySettings(context)
		}
		OutlinedCard(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp)
				.height(IntrinsicSize.Min),
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp),
			) {
				Text(
					text = "Theme preference",
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier
						.fillMaxWidth(),
					textAlign = TextAlign.Center,
				)
                _root_ide_package_.com.daumo.dynamicis.ui.settings.SettingsDivider(modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 16.dp))
				Column(Modifier.selectableGroup()) {
					_root_ide_package_.com.daumo.dynamicis.ui.settings.radioOptions.forEach { text ->
						Row(
							Modifier
								.fillMaxWidth()
								.height(56.dp)
								.clip(MaterialTheme.shapes.medium)
								.selectable(
									selected = (text == themeSelectedOption),
									onClick = {
										onThemeOptionSelected(text)
										settingsPreferences
											.edit()
											.putString(_root_ide_package_.com.daumo.dynamicis.model.THEME, text)
											.apply()
										_root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.isDarkTheme = when (text) {
											"System" -> {
												isSystemInDarkTheme
											}
											"Dark" -> {
												true
											}
											"Light" -> {
												false
											}
											else -> {
												isSystemInDarkTheme
											}
										}
									},
									role = Role.RadioButton
								)
								.padding(horizontal = 16.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							RadioButton(
								selected = (text == themeSelectedOption),
								onClick = null // null recommended for accessibility with screenreaders
							)
							Text(
								text = text,
								style = MaterialTheme.typography.bodyLarge,
								modifier = Modifier.padding(start = 16.dp)
							)
						}
					}
				}
			}
		}
		OutlinedCard(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp)
				.height(IntrinsicSize.Min),
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp),
			) {
				Text(
					text = "Style preference",
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier
						.fillMaxWidth(),
					textAlign = TextAlign.Center,
				)
                _root_ide_package_.com.daumo.dynamicis.ui.settings.SettingsDivider(modifier = Modifier
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 16.dp))
				Column(Modifier.selectableGroup()) {
					_root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.ThemeStyle.entries.forEach { themeStyle ->
						Row(
							Modifier
								.fillMaxWidth()
								.height(56.dp)
								.clip(MaterialTheme.shapes.medium)
								.selectable(
									selected = (themeStyle == styleSelectedOption),
									onClick = {
										onStyleOptionSelected(themeStyle)
										_root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.themeStyle = themeStyle
										settingsPreferences
											.edit()
											.putString(_root_ide_package_.com.daumo.dynamicis.model.STYLE, themeStyle.name)
											.apply()
									},
									role = Role.RadioButton
								)
								.padding(horizontal = 16.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							val stylePreviewColor =
								if (_root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.themeStyle.name != _root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.ThemeStyle.MaterialYou.name) {
									if (_root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.isDarkTheme) {
										if (_root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.themeStyle.darkScheme != null) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                themeStyle.previewColorDark ?: dynamicDarkColorScheme(context).primary
                                            } else {
												themeStyle.previewColorDark ?: MaterialTheme.colorScheme.primary
											}
                                        } else {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                themeStyle.previewColorLight ?: dynamicLightColorScheme(context).primary
                                            } else {
												themeStyle.previewColorLight ?: MaterialTheme.colorScheme.primary
											}
                                        }
									} else {
										if (_root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.themeStyle.lightScheme != null) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                themeStyle.previewColorLight ?: dynamicLightColorScheme(context).primary
                                            } else {
												themeStyle.previewColorLight ?: MaterialTheme.colorScheme.primary
											}
                                        } else {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                themeStyle.previewColorDark ?: dynamicDarkColorScheme(context).primary
                                            } else {
												themeStyle.previewColorDark ?: MaterialTheme.colorScheme.primary
											}
                                        }
									}
								} else {
									if (_root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.isDarkTheme) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            themeStyle.previewColorDark ?: dynamicDarkColorScheme(context).primary
                                        } else {
											themeStyle.previewColorDark ?: MaterialTheme.colorScheme.primary
										}
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            themeStyle.previewColorLight ?: dynamicLightColorScheme(context).primary
                                        } else {
											themeStyle.previewColorLight ?: MaterialTheme.colorScheme.primary
										}
                                    }
								}

							RadioButton(
								selected = (themeStyle == styleSelectedOption),
								onClick = null, // null recommended for accessibility with screenreaders
								colors = RadioButtonDefaults.colors(
									selectedColor = stylePreviewColor,
									unselectedColor = stylePreviewColor,
								)
							)
							Text(
								text = themeStyle.styleName,
								style = MaterialTheme.typography.bodyLarge,
								modifier = Modifier.padding(start = 16.dp).weight(1f)
							)
							Box(
								modifier = Modifier
									.size(24.dp)
									.aspectRatio(1f)
									.background(
										color = stylePreviewColor,
										shape = CircleShape
									)
							)
						}
					}
				}
			}
		}
	}
}