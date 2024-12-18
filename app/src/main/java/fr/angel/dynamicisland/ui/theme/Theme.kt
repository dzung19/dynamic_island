package fr.angel.dynamicisland.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import fr.angel.dynamicisland.model.SETTINGS_KEY
import fr.angel.dynamicisland.model.STYLE
import fr.angel.dynamicisland.model.THEME
import fr.angel.dynamicisland.ui.theme.themes.BlackTheme
import fr.angel.dynamicisland.ui.theme.themes.QuinacridoneMagentaThemeDarkColors
import fr.angel.dynamicisland.ui.theme.themes.QuinacridoneMagentaThemeLightColors

class Theme {

	enum class ThemeStyle(
		val lightScheme: ColorScheme? = null,
		val darkScheme: ColorScheme? = null,
		val styleName: String,
		val previewColorLight: Color?,
		val previewColorDark: Color?
	) {
		MaterialYou(
			styleName = "Material You",
			previewColorLight = null,
			previewColorDark = null
		),
		Black(
			darkScheme = BlackTheme,
			styleName = "Black & White",
			previewColorLight = Color.Black,
			previewColorDark = Color.White
		),
		QuinacridoneMagenta(
			lightScheme = QuinacridoneMagentaThemeLightColors,
			darkScheme = QuinacridoneMagentaThemeDarkColors,
			styleName = "Quinacridone Magenta",
			previewColorLight = QuinacridoneMagentaThemeLightColors.primary,
			previewColorDark = QuinacridoneMagentaThemeDarkColors.primary
		),
	}

	companion object {
		val instance = Theme()
	}

	var isDarkTheme by mutableStateOf(false)
	var themeStyle by mutableStateOf(ThemeStyle.MaterialYou)

	@Composable
	fun Init(
		isSystemInDarkTheme: Boolean = isSystemInDarkTheme(),
	) {
		val context = LocalContext.current
		val settingsPreferences = context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE)

		isDarkTheme = when (settingsPreferences.getString(THEME, "System")) {
			"System" -> { isSystemInDarkTheme }
			"Dark" -> { true }
			"Light" -> { false }
			else -> { isSystemInDarkTheme }
		}

		themeStyle = when (settingsPreferences.getString(STYLE, "MaterialYou")) {
			ThemeStyle.MaterialYou.name -> { ThemeStyle.MaterialYou }
			ThemeStyle.Black.name -> { ThemeStyle.Black }
			ThemeStyle.QuinacridoneMagenta.name -> { ThemeStyle.QuinacridoneMagenta }
			else -> { ThemeStyle.MaterialYou }
		}
	}
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun DynamicIslandTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	style: Theme.ThemeStyle = Theme.instance.themeStyle,
	// Dynamic color is available on Android 12+
	content: @Composable () -> Unit
) {
	val context = LocalContext.current
	val systemUiController = rememberSystemUiController()

	val LightColorScheme = lightColorScheme(
		primary = Color(0xFF6200EE), // Purple 500
		onPrimary = Color(0xFFFFFFFF), // White
		primaryContainer = Color(0xFFEADDFF), // Purple 100
		onPrimaryContainer = Color(0xFF21005D), // Purple900
		// ... other color properties ...
	)

	val DarkColorScheme = darkColorScheme(
		primary = Color(0xFFBB86FC), // Purple 200
		onPrimary = Color(0xFF000000), // Black
		primaryContainer = Color(0xFF3700B3), // Purple 700
		onPrimaryContainer = Color(0xFFEADDFF), // Purple 100
		// ... other color properties ...
	)

	val colorScheme = if (darkTheme) {
		DarkColorScheme
	} else {
		LightColorScheme
	}

	SideEffect {
		systemUiController.setSystemBarsColor(
			color = Color.Transparent,
			darkIcons = when(style) {
				Theme.ThemeStyle.MaterialYou -> {
					!darkTheme
				}
				else -> {
					if (darkTheme) {
						style.darkScheme == null
					} else {
						style.lightScheme != null
					}
				}
			}
		)
	}

//	MaterialTheme(
//		colorScheme = when(style) {
//			Theme.ThemeStyle.MaterialYou -> {
//				if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//			}
//			else -> {
//				if (darkTheme) {
//					style.darkScheme ?: style.lightScheme ?: dynamicDarkColorScheme(context)
//				} else {
//					style.lightScheme ?: style.darkScheme ?: dynamicLightColorScheme(context)
//				}
//			}
//		},
//		typography = Typography,
//		content = content
//	)

	MaterialTheme(
		colorScheme = colorScheme,
		typography = Typography,
		content = content
	)
}