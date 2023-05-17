package com.example.voco.service

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.chibde.visualizer.LineBarVisualizer
import com.example.voco.R
import com.example.voco.databinding.ActivityRecordBinding
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import okhttp3.internal.userAgent
import java.io.File
import java.io.FileOutputStream


object MediaService{
    var player: MediaPlayer? = null // 사용하지 않을 때는 메모리 해제
    private var recorder: MediaRecorder? = null // 사용하지 않을 때는 메모리 해제
    private var exoPlayer: SimpleExoPlayer? = null // 사용하지 않을 때는 메모리 해제
    private const val audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION
    private const val sampleRate = 44100
    private const val bitRate = 16
    private const val audioFormat = MediaRecorder.OutputFormat.MPEG_4
    private const val audioEncoder = MediaRecorder.AudioEncoder.AAC

    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording(fileName: String, recordBinding : ActivityRecordBinding){
        recordBinding.recordWarning.visibility = View.GONE
        recorder = MediaRecorder()
            .apply {
                setAudioSource(audioSource)
                setOutputFormat(audioFormat)
                //0setAudioEncoder(audioEncoder)
                //setAudioEncodingBitRate(bitRate)
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
        val lineBarVisualizer = view.findViewById<LineBarVisualizer>(R.id.visualizer)

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
    fun initExoPlayer(context: Context, playerView: PlayerControlView?){
        val trackSelector = DefaultTrackSelector(context)
        // Global settings.
        exoPlayer = SimpleExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()

        playerView?.player = exoPlayer
    }
    fun setExoPlayerUrl(context: Context, mediaUrl: String){

        // 미디어 데이터가 로드되는 DataSource.Factory 인스턴스 생성
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(context, userAgent)

        // Media를 플레이 할 미디어 소스를 생성.
        val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
        val mediaSource = mediaSourceFactory.createMediaSource(Uri.parse(mediaUrl))

        // MediaSource로 플레이 할 미디어를 player에 넣어줌
        exoPlayer?.prepare(mediaSource, true, false)

    }
    fun releaseExoPlayer(){
        exoPlayer?.release()
        exoPlayer = null
    }
    fun downloadAudio(context:Context, title: String, mediaUrl: String){
        val fileUri = Uri.parse(mediaUrl)

        val values = ContentValues()
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, title)
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Music")
            values.put(MediaStore.Audio.Media.IS_PENDING, 1);
        }

        val contentResolver = context.contentResolver
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val insertUri = contentResolver.insert(collection, values)

        try {
            val fileDescriptor = context.contentResolver.openFileDescriptor(insertUri!!, "w")
            val outputStream = FileOutputStream(fileDescriptor?.fileDescriptor)
            val inputStream = context.contentResolver.openInputStream(Uri.parse(mediaUrl))
            val bytes = ByteArray(8192)
            while (true) {
                val read = inputStream?.read(bytes)
                if (read == -1) {
                    break
                }
                outputStream.write(bytes, 0, read!!)
            }
            outputStream.close()
            inputStream?.close()
            fileDescriptor?.close()
            values.clear()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
            }

            context.contentResolver.update(insertUri, values, null, null)

            Toast.makeText(context, "더빙이 저장되었습니다", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}