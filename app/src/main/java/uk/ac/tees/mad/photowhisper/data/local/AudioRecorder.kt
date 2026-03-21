package uk.ac.tees.mad.photowhisper.data.local

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTime: Long = 0

    fun startRecording(): String {
        val audioDir = File(context.filesDir, "audio").apply {
            if (!exists()) mkdirs()
        }

        outputFile = File(audioDir, "temp_${System.currentTimeMillis()}.m4a")

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(outputFile?.absolutePath)

            prepare()
            start()
        }

        startTime = System.currentTimeMillis()
        return outputFile?.absolutePath ?: ""
    }

    fun stopRecording(): Pair<String, Long> {
        val duration = System.currentTimeMillis() - startTime

        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        return Pair(outputFile?.absolutePath ?: "", duration)
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mediaRecorder = null
        outputFile?.delete()
        outputFile = null
    }

    fun isRecording(): Boolean = mediaRecorder != null
}