package com.daumo.dynamicis.plugins.media

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi

class MediaCallback(
	val mediaController: MediaController,
	private val context: MediaSessionPlugin,
) : MediaController.Callback() {

	private lateinit var mediaMetadata: MediaMetadata
	val mediaStruct = MediaStruct()
	private val handler = Handler(Looper.getMainLooper())

	init {
		val metadata = mediaController.metadata
		val playbackState = mediaController.playbackState
		if (metadata != null) {
			mediaMetadata = metadata
		}
		if (playbackState != null) {
			mediaStruct.playbackState.value = playbackState
		}
		if (metadata != null && playbackState != null) {
			updateMediaStruct(addPlugin = mediaStruct.playbackState.value.state == PlaybackState.STATE_PLAYING)
		}
	}

    override fun onPlaybackStateChanged(state: PlaybackState?) {
		super.onPlaybackStateChanged(state)
		if (state == null) return
		// Update the playback state
		mediaStruct.playbackState.value = state
		context.context.addPlugin(context)

		// If media is paused, remove the plugin after 60 seconds
		if (state.state == PlaybackState.STATE_PLAYING) {
			handler.removeCallbacksAndMessages(null)
		} else {
			handler.postDelayed ({
				context.context.removePlugin(context)
			}, 60000)
		}
	}

	override fun onMetadataChanged(metadata: MediaMetadata?) {
		super.onMetadataChanged(metadata)
		mediaMetadata = metadata ?: return

		if (this::mediaMetadata.isInitialized) {
			updateMediaStruct()
		}
	}

    override fun onSessionDestroyed() {
		super.onSessionDestroyed()
		context.removeMedia(mediaController)
	}

	private fun updateMediaStruct(addPlugin: Boolean = true) {
		if (!this::mediaMetadata.isInitialized) return
		mediaStruct.title.value = (mediaMetadata.getText(MediaMetadata.METADATA_KEY_TITLE) ?: "").toString()
		mediaStruct.artist.value = (mediaMetadata.getText(MediaMetadata.METADATA_KEY_ARTIST) ?: "").toString()
		mediaStruct.cover.value = mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
		mediaStruct.duration.value = mediaMetadata.getLong(MediaMetadata.METADATA_KEY_DURATION)

		if (addPlugin) { context.context.addPlugin(context) }
	}
}
