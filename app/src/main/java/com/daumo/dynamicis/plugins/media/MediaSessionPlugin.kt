package com.daumo.dynamicis.plugins.media

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.github.compose.waveloading.DrawType
import com.github.compose.waveloading.WaveLoading
import com.skydoves.landscapist.rememberDrawablePainter
import com.daumo.dynamicis.model.service.IslandOverlayService
import com.daumo.dynamicis.model.service.NotificationService
import com.daumo.dynamicis.plugins.BasePlugin
import com.daumo.dynamicis.plugins.PluginSettingsItem

class MediaSessionPlugin(
    override val id: String = "MediaSessionPlugin",
    override val name: String = "MediaSession",
    override val description: String = "Show the current media session playing",
    override val permissions: ArrayList<String> = arrayListOf(
        Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
    ),
    override var enabled: MutableState<Boolean> = mutableStateOf(false),
    override var pluginSettings: MutableMap<String, PluginSettingsItem> = mutableMapOf(),
) : BasePlugin() {

    lateinit var context: IslandOverlayService
    private lateinit var mediaSessionManager: MediaSessionManager

    private var callbackMap = mutableStateMapOf<String, MediaCallback>()

    // private var mediaStruct by mutableStateOf<MediaStruct?>(null)
    private var songPosition by mutableFloatStateOf(0f)
    private var duration: Long by mutableLongStateOf(0)
    private var elapsed: Long by mutableLongStateOf(0)

    private val listenerForActiveSessions =
        OnActiveSessionsChangedListener { controllers ->
            if (controllers != null) {
                for (controller in controllers) {
                    // Cancel if already exists
                    if (callbackMap[controller.packageName] != null) return@OnActiveSessionsChangedListener

                    // Create callback for this controller and add it to the map of callbacks
                    val callback = MediaCallback(controller, this)
                    callbackMap[controller.packageName] = callback
                    controller.registerCallback(callback)
                }
            }
        }

    fun removeMedia(mediaController: MediaController) {
        callbackMap.remove(mediaController.packageName)
        if (callbackMap.isEmpty()) {
            context.removePlugin(this)
        }
    }

    override fun canExpand(): Boolean {
        return true
    }

    override fun onCreate(context: IslandOverlayService?) {
        this.context = context ?: return

        // Get the media session manager
        mediaSessionManager =
            context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

        try {
            // Register the listener for active sessions
            mediaSessionManager.addOnActiveSessionsChangedListener(
                listenerForActiveSessions,
                ComponentName(context, NotificationService::class.java)
            )
            mediaSessionManager.getActiveSessions(
                ComponentName(
                    context,
                    NotificationService::class.java
                )
            ).forEach { controller ->
                // Cancel if already exists
                if (callbackMap[controller.packageName] != null) return@forEach

                val callback = MediaCallback(controller, this)
                callbackMap[controller.packageName] = callback
                controller.registerCallback(callback)
            }
        } catch (e: SecurityException) {
            Log.w("MediaSessionPlugin", "Notification Listener permission not granted: ${e.message}")
        } catch (e: Exception) {
            Log.e("MediaSessionPlugin", "Error initializing MediaSessionPlugin: ${e.message}")
        }
    }

    @Composable
    override fun Composable() {
        val mediaCallback = callbackMap.values.firstOrNull() ?: return

        val controller = mediaCallback.mediaController
        val controls = controller.transportControls

        var isDragging by remember { mutableStateOf(false) }
        var draggedPosition by remember { mutableFloatStateOf(0f) }
        var draggedOffset by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(controller.playbackState?.position) {
            elapsed = controller.playbackState?.position ?: 0
            duration = controller.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0

            songPosition = if (duration > 0) ((elapsed.toFloat() / duration) * 100).coerceIn(0f, 100f) else 0f
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Cover + title + artist
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Crossfade(targetState = mediaCallback.mediaStruct.cover.value) { cover ->
                    if (cover != null) {
                        Image(
                            bitmap = cover.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .size(64.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier
                                .size(128.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Crossfade(targetState = mediaCallback.mediaStruct.title.value) { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Crossfade(targetState = mediaCallback.mediaStruct.artist.value) { artist ->
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                IconButton(
                    onClick = { context.shrink() },
                    modifier = Modifier.align(Alignment.Top)
                ) { Icon(imageVector = Icons.Default.ExpandLess, contentDescription = null) }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Slider controlling the position in the song
            Slider(
                value = if (isDragging) draggedPosition else animateFloatAsState(if(songPosition.isNaN()) 0f else songPosition).value,
                onValueChange = { value ->
                    Log.d("MediaSessionPlugin", "onValueChange: $value")
                    draggedPosition = value
                    isDragging = true
                },
                onValueChangeFinished = {
                    controls.seekTo(((draggedPosition / 100) * duration).toLong())
                    isDragging = false
                },
                valueRange = 0f..100f,
            )

            // Controls
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center.apply { Arrangement.spacedBy(16.dp) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { controls.skipToPrevious() }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous track"
                    )
                }
                FilledIconButton(onClick = {
                    if (mediaCallback.mediaStruct.isPlaying()) {
                        controls.pause()
                    } else {
                        controls.play()
                    }
                }
                ) {
                    val icon = if (mediaCallback.mediaStruct.isPlaying()) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    }
                    Icon(imageVector = icon, contentDescription = "Play/Pause")
                }
                IconButton(onClick = { controls.skipToNext() }) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next track")
                }
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onClick() {
        val current = callbackMap.values.firstOrNull() ?: return

        val controller = current.mediaController
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(controller.packageName) ?: return
        try {
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            pendingIntent.send()
        } catch (e: Exception) {
            Log.w("MediaSessionPlugin", "Failed to launch media app: ${e.message}")
        }
    }

    override fun onDestroy() {
        if (!::mediaSessionManager.isInitialized) return
        try {
            callbackMap.values.forEach { callback ->
                callback.mediaController.unregisterCallback(callback)
            }
            callbackMap.clear()
            // Unregister the listener for active sessions
            mediaSessionManager.removeOnActiveSessionsChangedListener(listenerForActiveSessions)
        } catch (e: Exception) {
            Log.w("MediaSessionPlugin", "Error during onDestroy: ${e.message}")
        }
    }

    @Composable
    override fun PermissionsRequired() {

    }

    override fun onLeftSwipe() {}
    override fun onRightSwipe() {}

    @Composable
    override fun LeftOpenedComposable() {
        val mediaCallback = callbackMap.values.firstOrNull() ?: return
        val cover = mediaCallback.mediaStruct.cover.value

        if (cover != null) {
            Crossfade(targetState = cover) { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Cover",
                    modifier = Modifier.clip(CircleShape)
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
            )
        }
    }

    @Composable
    override fun RightOpenedComposable() {
        val mediaCallback = callbackMap.values.firstOrNull()
        if (mediaCallback == null) {
            Log.d("MediaSessionPlugin", "RightOpenedComposable: No media callback")
            return
        }

        LaunchedEffect(mediaCallback.mediaStruct.playbackState.value.position) {
            elapsed = mediaCallback.mediaStruct.playbackState.value.position
            duration = mediaCallback.mediaStruct.duration.value

            songPosition = if (duration > 0) ((elapsed.toFloat() / duration) * 100).coerceIn(0f, 100f) else 0f
        }

        val icon = try {
            context.packageManager.getApplicationIcon(
                mediaCallback.mediaController.packageName ?: "com.daumo.dynamicis"
            )
        } catch (_: Exception) {
            null
        }

        WaveLoading(
            progress = animateFloatAsState(targetValue = (songPosition / 100).coerceIn(0f, 1f)).value,
            backDrawType = DrawType.DrawImage,
            modifier = Modifier
                .fillMaxHeight()
                .clip(CircleShape)
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            if (icon != null) {
                Image(
                    painter = rememberDrawablePainter(drawable = icon),
                    contentDescription = null
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null
                )
            }
        }
    }
}