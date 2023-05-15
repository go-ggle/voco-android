package com.example.voco.service

import android.annotation.SuppressLint
import android.content.ContentValues
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.chibde.visualizer.LineBarVisualizer
import com.example.voco.R
import com.example.voco.databinding.ActivityRecordBinding
import java.io.File


object MediaService{
    var player: MediaPlayer? = null // 사용하지 않을 때는 메모리 해제
    private lateinit var binding : ActivityRecordBinding
    private var recorder: MediaRecorder? = null // 사용하지 않을 때는 메모리 해제
    private const val audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION
    private const val sampleRate = 44100
    private const val bitRate = 16
    private const val audioFormat = MediaRecorder.OutputFormat.MPEG_4
    private const val audioEncoder = MediaRecorder.AudioEncoder.AAC

    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording(fileName: String, recordBinding : ActivityRecordBinding){
        binding = recordBinding
        binding.recordWarning.visibility = View.GONE
        recorder = MediaRecorder()
            .apply {
                setAudioSource(audioSource)
                setOutputFormat(audioFormat)
                setAudioEncoder(audioEncoder)
                setAudioEncodingBitRate(bitRate)
                setAudioChannels(1)
                setAudioSamplingRate(sampleRate)
                setOutputFile(fileName)
                try{
                    prepare()
                    start()
                }catch (e: Exception){
                    Log.e(ContentValues.TAG, "startRecording() failed")
                }
            }
    }
    fun stopRecording() {
        recorder?.run {
            stop()
            release()
        }
        recorder = null
    }
    fun startPlaying(page: View, fileName: String) {
        if(File(fileName).exists()){
            player = MediaPlayer()
                .apply{
                    try {
                        setAudioAttributes(
                            AudioAttributes. Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                        setDataSource(fileName)
                        prepare()
                        start()
                    } catch (e: Exception) {
                        Log.e(ContentValues.TAG, "startPlaying() failed")
                    }
                }
            lineBarVisualization(page)
            player?.setOnCompletionListener {
                stopPlaying()
                lineBarVisualization(page)
            }
        }
    }
    private fun stopPlaying() {
        player?.release()
        player = null
    }
    @SuppressLint("ResourceAsColor")
    private fun lineBarVisualization(view: View) {
        var lineBarVisualizer = view.findViewById<LineBarVisualizer>(R.id.visualizer)

        // setting the custom color to the line.
        lineBarVisualizer.setColor(R.color.pure_white)

        // define the custom number of bars we want in the visualizer between (10 - 256).
        lineBarVisualizer.setDensity(60F)

        // Setting the media player to the visualizer.
        if(player != null)
            lineBarVisualizer.setPlayer(player!!.audioSessionId)
        else
            lineBarVisualizer.release()
    }

}