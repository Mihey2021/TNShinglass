package ru.tn.shinglass.activity.utilites

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import kotlinx.coroutines.*
import ru.tn.shinglass.R
import kotlin.coroutines.EmptyCoroutineContext

enum class SoundType(val resource: Int) {
    ANOTHER_ITEM_SCAN(R.raw.another_item_scanned),
    ITEM_NOT_FOUND(R.raw.item_not_found),
    CELL_NOT_FOUND(R.raw.cell_not_found),
    ANOTHER_CELL_SCAN(R.raw.another_cell_scanned),
    CELL_CHANGED(R.raw.cell_changed),
    ATTENTION(R.raw.attention),
    SMALL_ERROR(R.raw.small_error),
    ERROR(R.raw.error),
}

interface SoundPlayerInput {
    fun playSound()
    fun initAudioAttributes(): AudioAttributes
}

class SoundPlayer(
    private val context: Context,
    private val type: SoundType = SoundType.ERROR,
    private var numberOfRepeats: Int = 0
) : SoundPlayerInput {
    private var mediaPlayer: MediaPlayer? = null

    init {
        if (numberOfRepeats < 0) numberOfRepeats *= -1
        if (numberOfRepeats > 0) numberOfRepeats -= 1
    }

    override fun playSound() {
        CoroutineScope(EmptyCoroutineContext).launch {
            var count = 0
            if (mediaPlayer == null) {
                val audioAttributes = initAudioAttributes()
                mediaPlayer = MediaPlayer.create(context, type.resource)
                    .also { mPlayer ->
                        mPlayer.setAudioAttributes(audioAttributes)
                        mPlayer.setOnCompletionListener {
                            if (count < numberOfRepeats) {
                                count++
                                mPlayer.seekTo(0)
                                mPlayer.start()
                            } else {
                                mPlayer.release()
                            }
                        }
                    }
            }
            mediaPlayer?.start()
        }
    }

    override fun initAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .build()

}