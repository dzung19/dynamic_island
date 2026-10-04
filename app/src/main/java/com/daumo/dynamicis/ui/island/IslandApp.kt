package com.daumo.dynamicis.ui.island

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring.DampingRatioLowBouncy
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.daumo.dynamicis.island.*
import com.daumo.dynamicis.model.service.IslandOverlayService
import com.daumo.dynamicis.ui.theme.DynamicIslandTheme
import com.daumo.dynamicis.ui.theme.Theme

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IslandApp(
	islandOverlayService: IslandOverlayService
) {
    val context = LocalContext.current
    Theme.Companion.instance.Init()
    LaunchedEffect(Unit) {
        IslandSettings.Companion.instance.loadSettings(context = context)
    }
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
		visible = (Island.isScreenOn
				|| IslandSettings.Companion.instance.showOnLockScreen)
				&& (!Island.isInLandscape || IslandSettings.Companion.instance.showInLandscape),
		modifier = Modifier

			//.background(Color.Red)
	) {
        DynamicIslandTheme(
            darkTheme = true,
            style = Theme.Companion.instance.themeStyle
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
                    if (islandView is IslandViewState.Opened || islandView is IslandViewState.Expanded) {
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

                Card(
                    shape = RoundedCornerShape(cornerPercentage),
                    modifier = Modifier
                        .then(clickModifier)
                        .width(width)
                        .height(height)
                        /*.wrapContentHeight()
                    .height(IntrinsicSize.Min)*/
                        .defaultMinSize(minHeight = height),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black,
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (islandOverlayService.islandState.state != IslandStates.Closed) {
                            bindedPlugin?.BackgroundComposable()
                        }
                        Crossfade(
                            targetState = islandOverlayService.islandState.state,
                            animationSpec = tween(100), label = ""
                        ) {
                            when (it) {
                                IslandStates.Opened -> {
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

                                IslandStates.Expanded -> {
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