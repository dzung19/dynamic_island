package com.daumo.dynamicis.ui.animation

import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WavesLoadingIndicator(modifier: Modifier = Modifier, color: Color, progress: Float) {
	BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
		val constraintsWidth = maxWidth
		val constraintsHeight = maxHeight
		val density = LocalDensity.current

		val wavesShader by produceState<Shader?>(
			initialValue = null,
			constraintsHeight,
			constraintsWidth,
			color,
			density
		) {
			val widthPx = with(density) { constraintsWidth.roundToPx() }
			val heightPx = with(density) { constraintsHeight.roundToPx() }
			if (widthPx > 0 && heightPx > 0) {
				value = withContext(Dispatchers.Default) {
					createWavesShader(
						width = widthPx,
						height = heightPx,
						color = color
					)
				}
			}
		}

		if (progress > 0f && wavesShader != null) {
			WavesOnCanvas(shader = wavesShader!!, progress = progress.coerceAtMost(0.99f))
		}
	}
}

@Composable
private fun WavesOnCanvas(shader: Shader, progress: Float) {
	val matrix = remember { Matrix() }

	val paint = remember(shader) {
		Paint().apply {
			isAntiAlias = true
			this.shader = shader
		}
	}

	val wavesTransition = rememberWavesTransition()
	val amplitudeRatio by wavesTransition.amplitudeRatio
	val waveShiftRatio by wavesTransition.waveShiftRatio

	Canvas(modifier = Modifier.fillMaxSize()) {
		drawIntoCanvas {
			val height = size.height
			val width = size.width
			matrix.setScale(1f, amplitudeRatio / AmplitudeRatio, 0f, WaterLevelRatio * height)
			matrix.postTranslate(waveShiftRatio * width, (WaterLevelRatio - progress) * height)
			shader.setLocalMatrix(matrix)
			it.drawRect(0f, 0f, width, height, paint)
		}
	}
}

private class WavesTransition(
	val waveShiftRatio: State<Float>,
	val amplitudeRatio: State<Float>
)

@Composable
private fun rememberWavesTransition(): WavesTransition {
	val transition = rememberInfiniteTransition(label = "ratio")

	val waveShiftRatio = transition.animateFloat(
		initialValue = 0f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(
			tween(
				durationMillis = WavesShiftAnimationDurationInMillis,
				easing = LinearEasing
			)
		), label = "ratio"
	)

	val amplitudeRatio = transition.animateFloat(
		initialValue = 0.06f,
		targetValue = 0.10f,
		animationSpec = infiniteRepeatable(
			animation = tween(
				durationMillis = WavesAmplitudeAnimationDurationInMillis,
				easing = FastOutLinearInEasing
			),
			repeatMode = RepeatMode.Reverse
		), label = "ratio"
	)

	return remember(transition) {
		WavesTransition(waveShiftRatio = waveShiftRatio, amplitudeRatio = amplitudeRatio)
	}
}

@Stable
private fun createWavesShader(width: Int, height: Int, color: Color): Shader {
	if (width <= 0 || height <= 0) {
		val emptyBitmap = ImageBitmap(1, 1, ImageBitmapConfig.Argb8888)
		return ImageShader(image = emptyBitmap, tileModeX = TileMode.Clamp, tileModeY = TileMode.Clamp)
	}
	val waveCycles = maxOf(2, width / 200)
	val angularFrequency = waveCycles * 2f * PI / width
	val amplitude = height * AmplitudeRatio
	val waterLevel = height * WaterLevelRatio

	val bitmap = ImageBitmap(width = width, height = height, ImageBitmapConfig.Argb8888)
	val canvas = Canvas(bitmap)

	val wavePaint = Paint().apply {
		strokeWidth = 2f
		isAntiAlias = true
	}

	val waveY = FloatArray(size = width + 1)

	wavePaint.color = color.copy(alpha = 0.35f)
	for (beginX in 0..width) {
		val wx = beginX * angularFrequency
		val beginY = waterLevel + amplitude * sin(wx).toFloat()
		canvas.drawLine(
			p1 = Offset(x = beginX.toFloat(), y = beginY),
			p2 = Offset(x = beginX.toFloat(), y = (height + 1).toFloat()),
			paint = wavePaint
		)
		waveY[beginX] = beginY
	}

	wavePaint.color = color.copy(alpha = 0.65f)
	val endX = width + 1
	val waveToShift = width / (waveCycles * 4)
	for (beginX in 0..width) {
		canvas.drawLine(
			p1 = Offset(x = beginX.toFloat(), y = waveY[(beginX + waveToShift).rem(endX)]),
			p2 = Offset(x = beginX.toFloat(), y = (height + 1).toFloat()),
			paint = wavePaint
		)
	}
	return ImageShader(image = bitmap, tileModeX = TileMode.Repeated, tileModeY = TileMode.Clamp)
}

private const val AmplitudeRatio = 0.08f
private const val WaterLevelRatio = 0.5f
private const val WavesShiftAnimationDurationInMillis = 2200
private const val WavesAmplitudeAnimationDurationInMillis = 2500