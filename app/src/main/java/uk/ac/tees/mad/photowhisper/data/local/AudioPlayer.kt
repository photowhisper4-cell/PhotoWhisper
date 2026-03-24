package uk.ac.tees.mad.photowhisper.data.local

import android.content.Context
import android.media.MediaPlayer
import java.io.File

class AudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPath: String? = null

    fun play(audioPath: String, onCompletion: () -> Unit = {}) {
        try {
            stop()

            val file = File(audioPath)
            if (!file.exists()) {
                throw Exception("Audio file not found: $audioPath")
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                prepare()
                start()
                setOnCompletionListener {
                    onCompletion()
                    release()
                    mediaPlayer = null
                    currentPath = null
                }
            }
            currentPath = audioPath
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
            throw e
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        currentPath = null
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = try {
        mediaPlayer?.duration ?: 0
    } catch (e: Exception) {
        0
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun release() {
        stop()
    }
}