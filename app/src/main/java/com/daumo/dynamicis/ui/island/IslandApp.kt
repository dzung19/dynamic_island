package com.daumo.dynamicis.ui.island

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring.DampingRatioLowBouncy
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants.IterateForever
import com.airbnb.lottie.compose.rememberLottieComposition
import com.daumo.dynamicis.R
import com.daumo.dynamicis.island.*
import com.daumo.dynamicis.model.service.IslandOverlayService
import com.daumo.dynamicis.ui.theme.DynamicIslandTheme
import com.daumo.dynamicis.ui.theme.Theme


@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IslandApp(
	islandOverlayService: com.daumo.dynamicis.model.service.IslandOverlayService
) {
    val context = LocalContext.current
    _root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.Init()
    LaunchedEffect(Unit) {
        _root_ide_package_.com.daumo.dynamicis.island.IslandSettings.Companion.instance.loadSettings(context = context)
    }
    val composition =
        rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.snow_fall))
    val composition1 =
        rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.valentine))
    val (description, illustration) = createRefs()
    val islandView = islandOverlayService.islandState
    val bindedPlugin = islandOverlayService.bindedPlugins.firstOrNull()

	val height by animateDpAsState(
		targetValue = islandView.height,
		animationSpec =
		spring(
			dampingRatio = DampingRatioLowBouncy,
			stiffness = Spring.StiffnessLow
		)
	)
	val width by animateDpAsState(
		targetValue = islandView.width,
		animationSpec = spring(
			dampingRatio = DampingRatioLowBouncy,
			stiffness = Spring.StiffnessLow
		)
	)
	val cornerPercentage by animateFloatAsState(targetValue = islandView.cornerPercentage)

	AnimatedVisibility(
		visible = (_root_ide_package_.com.daumo.dynamicis.island.Island.isScreenOn
				|| _root_ide_package_.com.daumo.dynamicis.island.IslandSettings.Companion.instance.showOnLockScreen)
				&& (!_root_ide_package_.com.daumo.dynamicis.island.Island.isInLandscape || _root_ide_package_.com.daumo.dynamicis.island.IslandSettings.Companion.instance.showInLandscape),
		modifier = Modifier

			//.background(Color.Red)
	) {
        _root_ide_package_.com.daumo.dynamicis.ui.theme.DynamicIslandTheme(
            darkTheme = if (islandOverlayService.invertedTheme) !_root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.isDarkTheme else _root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.isDarkTheme,
            style = _root_ide_package_.com.daumo.dynamicis.ui.theme.Theme.Companion.instance.themeStyle
        ) {
            Box(
                modifier = Modifier
                    .padding(top = islandView.yPosition)
                    .height(height)
                    /*.wrapContentHeight()
                    .height(IntrinsicSize.Min)
                    .defaultMinSize(minHeight = height)*/
                    .width(width + 16.dp)
                    .fillMaxWidth()
                    .offset(x = islandView.xPosition)
                    .clip(RoundedCornerShape(cornerPercentage)),
                contentAlignment = Alignment.TopCenter
            ) {
                val clickModifier =
                    if (islandView is com.daumo.dynamicis.island.IslandViewState.Opened || islandView is com.daumo.dynamicis.island.IslandViewState.Expanded) {
                        Modifier
                            .clip(RoundedCornerShape(cornerPercentage))
                            .combinedClickable(
                                onClick = { bindedPlugin?.onClick() },
                                onLongClick = {
                                    if (bindedPlugin?.canExpand() == true) {
                                        islandOverlayService.expand()
                                    }
                                }
                            )
                    } else {
                        Modifier
                    }

                val borderModifier =
                    if (_root_ide_package_.com.daumo.dynamicis.island.IslandSettings.Companion.instance.showBorders) {
                        Modifier
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(cornerPercentage)
                            )
                    } else {
                        Modifier
                    }

                Card(
                    shape = RoundedCornerShape(cornerPercentage),
                    modifier = Modifier
                        .then(clickModifier)
                        .then(borderModifier)
                        .width(width)
                        .height(height)
                        /*.wrapContentHeight()
                    .height(IntrinsicSize.Min)*/
                        .defaultMinSize(minHeight = height),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Crossfade(
                            targetState = islandOverlayService.islandState.state,
                            animationSpec = tween(100), label = ""
                        ) {
                            when (it) {
                                _root_ide_package_.com.daumo.dynamicis.island.IslandStates.Opened -> {
                                    LottieAnimation(
                                        composition = composition1.value,
                                        iterations = IterateForever,
                                        contentScale = ContentScale.Crop)
                                }

                                _root_ide_package_.com.daumo.dynamicis.island.IslandStates.Expanded -> {
                                    LottieAnimation(
                                        composition = composition1.value,
                                        iterations = IterateForever,
                                        contentScale = ContentScale.FillBounds
                                    )
                                }

                                _root_ide_package_.com.daumo.dynamicis.island.IslandStates.Closed -> {
                                    LottieAnimation(
                                        composition = composition1.value,
                                        iterations = IterateForever,
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        Crossfade(
                            targetState = islandOverlayService.islandState.state,
                            animationSpec = tween(100), label = ""
                        ) {
                            when (it) {
                                _root_ide_package_.com.daumo.dynamicis.island.IslandStates.Opened -> {
                                    val boxModifier = Modifier
                                        .fillMaxHeight()
//                                    Card {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Left side

                                        Box(
                                            modifier = boxModifier,
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Crossfade(
                                                targetState = bindedPlugin,
                                            ) { plugin -> plugin?.LeftOpenedComposable() }
                                        }

                                        // Right side
                                        Box(
                                            modifier = boxModifier,
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Crossfade(
                                                targetState = bindedPlugin,
                                            ) { plugin -> plugin?.RightOpenedComposable() }
                                        }
                                    }
                                }

                                _root_ide_package_.com.daumo.dynamicis.island.IslandStates.Expanded -> {
                                    Crossfade(
                                        targetState = bindedPlugin,
                                    ) { plugin -> plugin?.Composable() }
                                }

                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}