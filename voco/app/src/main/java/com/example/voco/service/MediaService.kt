package com.example.voco.service

import android.annotation.SuppressLint
import android.content.Context
import android.media.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.chibde.visualizer.LineBarVisualizer
import com.example.voco.R
import com.example.voco.data.adapter.BlockAdapter
import com.example.voco.data.model.Block
import com.example.voco.data.model.Language
import com.example.voco.data.model.Project
import com.example.voco.databinding.ActivityRecordBinding
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.userAgent
import java.io.*
import java.util.*


object MediaService{
    var player: AudioTrack? = null // 사용하지 않을 때는 메모리 해제
    var exoPlayer: SimpleExoPlayer? = null // 사용하지 않을 때는 메모리 해제
    private var recorder: AudioRecord? = null // 사용하지 않을 때는 메모리 해제
    private var isRecording : Boolean? = null
    private var isPlaying : Boolean? = null
    private var lineBarVisualizer : LineBarVisualizer? = null
    private lateinit var dataSourceFactory : DataSource.Factory
    private const val audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION // for Active noise cancellation
    private const val sampleRate = 44100
    private const val channelCount = AudioFormat.CHANNEL_IN_STEREO
    private const val bitRate = 16
    private const val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelCount, audioFormat)

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording(filePath: String, recordBinding : ActivityRecordBinding){
        recordBinding.recordWarning.visibility = View.GONE
        recorder = AudioRecord(audioSource, sampleRate, channelCount, audioFormat, bufferSize)
        recorder!!.startRecording()
        isRecording = true
        readRecording(filePath)
    }
    private fun readRecording(filePath: String) = CoroutineScope(Dispatchers.Default).launch {
        val readData = ByteArray(bufferSize)
        var fos: FileOutputStream? = null
        try {
            withContext(Dispatchers.IO) {
                fos = FileOutputStream (filePath)
            }

            while (isRecording!!) {
                val ret: Int = recorder!!.read(readData, 0, bufferSize) //  AudioRecord의 read 함수를 통해 pcm data 를 읽어옴

                withContext(Dispatchers.IO) {
                    fos?.write(readData, 0, bufferSize)
                } //  읽어온 readData 를 파일에 write
            }

            //recorder?.stop()
            recorder?.release()
            recorder = null

            withContext(Dispatchers.IO) {
                fos?.close()
            }

        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun stopRecording() {
        isRecording = false
        recorder?.run {
            release()
        }
        recorder = null
    }
    fun startPlaying(page: View, filePath: String) {
        if(File(filePath).exists()){
            isPlaying = true
            player = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelCount, audioFormat, bufferSize, AudioTrack.MODE_STREAM)

            lineBarVisualizer?.release()
            lineBarVisualization(page)

            readPlaying(filePath)
        }
    }
    private fun readPlaying(filePath: String) = CoroutineScope(Dispatchers.Default).launch {
        try{
            val writeData = ByteArray(bufferSize)
            var fis: FileInputStream? = null
            withContext(Dispatchers.IO){
                fis = FileInputStream(filePath)
            }
            val dis = DataInputStream(fis)
            player?.play()

            while(isPlaying!!){
                val ret = dis.read(writeData, 0, bufferSize)
                if(ret <= 0){
                    isPlaying = false
                    break
                }
                player?.write(writeData, 0, ret)
            }

            stopPlaying()

            withContext(Dispatchers.IO){
                dis.close()
                fis?.close()
            }
        }catch(e: Exception){

        }
    }
    private fun stopPlaying() {
        player?.stop()
        player?.release()
        player = null
    }
    @SuppressLint("ResourceAsColor")
    private fun lineBarVisualization(view: View) {
        lineBarVisualizer = view.findViewById<LineBarVisualizer>(R.id.visualizer)

        // setting the custom color to the line.
        lineBarVisualizer!!.setColor(R.color.pure_white)

        // define the custom number of bars we want in the visualizer between (10 - 256).
        lineBarVisualizer!!.setDensity(60F)

        // Setting the media player to the visualizer.
        lineBarVisualizer!!.setPlayer(player!!.audioSessionId)
    }
    fun setExoPlayerUrl(context: Context, playerView: PlayerControlView?,mediaUrl: String){
        exoPlayer?.stop()
        exoPlayer?.release()

        val trackSelector = DefaultTrackSelector(context)
        // Global settings.
        exoPlayer = SimpleExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()

        playerView?.player = exoPlayer
        // 미디어 데이터가 로드되는 DataSource.Factory 인스턴스 생성
        dataSourceFactory = DefaultDataSourceFactory(context, userAgent)

        // Media를 플레이 할 미디어 소스를 생성.
        val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
        val mediaSource = mediaSourceFactory.createMediaSource(Uri.parse(mediaUrl))

        // MediaSource로 플레이 할 미디어를 player에 넣어줌
        exoPlayer?.run{
            prepare(mediaSource, false, false)
            addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if(isPlaying){
                        // change to play button
                        BlockAdapter.player?.stop()
                    }
                }
            })
        }
    }
    fun releaseExoPlayer(){
        exoPlayer?.release()
        exoPlayer = null
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    fun downloadAudio(view: View, IDENTITY_POOL_ID: String, REGION: Regions, BUCKET: String, project: Project, block: Block?, fileKey: String){
        // create CredentialsProvider object
        val credentialsProvider = CognitoCachingCredentialsProvider(
            view.context.applicationContext,
            IDENTITY_POOL_ID, // 자격 증명 풀 ID
            REGION, // region
        )

        TransferNetworkLossHandler.getInstance(view.context.applicationContext)

        // create TransferUtility object
        val transferUtility = TransferUtility.builder()
            .context(view.context.applicationContext)
            .defaultBucket(BUCKET) // bucket name
            .s3Client(AmazonS3Client(credentialsProvider, Region.getRegion(REGION)))
            .build()

        val title = when(block){
            null -> "${project.title.replace(" ","_")}_${Language.values()[project.language].name.lowercase()}_${project.id}.wav" // project dubbing download
            else -> "${project.title.replace(" ","_")}_${Language.values()[project.language].name.lowercase()}_block${block.id}.wav" // block dubbing download
        }
        val filePath =  File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath, title)
        val downloadObserver = transferUtility.download(fileKey, filePath) // start download
        // download progress listener
        downloadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    if(block != null) view.alpha = 1F
                    Toast.makeText(view.context, "다운로드가 완료되었습니다", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onProgressChanged(id: Int, current: Long, total: Long) {
                try {
                    val done = (((current.toDouble() / total) * 100.0).toInt()) //as Int

                }
                catch (e: Exception) {
                    Log.d("AWS", "Trouble calculating progress percent", e)
                }
            }

            override fun onError(id: Int, ex: Exception) {
                Toast.makeText(view.context, "다운로드에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        })
    }
}