package com.example.voco.service

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
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
import com.example.voco.data.model.Block
import com.example.voco.data.model.Language
import com.example.voco.data.model.Project
import com.example.voco.databinding.ActivityRecordBinding
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import okhttp3.internal.userAgent
import java.io.File
import java.util.*


object MediaService{
    var player: MediaPlayer? = null // 사용하지 않을 때는 메모리 해제
    private var recorder: MediaRecorder? = null // 사용하지 않을 때는 메모리 해제
    private var exoPlayer: SimpleExoPlayer? = null // 사용하지 않을 때는 메모리 해제
    private lateinit var dataSourceFactory : DataSource.Factory
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
        // 미디어 데이터가 로드되는 DataSource.Factory 인스턴스 생성
        dataSourceFactory = DefaultDataSourceFactory(context, userAgent)
    }
    fun setExoPlayerUrl(context: Context, playerView: PlayerControlView?,mediaUrl: String){
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
        exoPlayer?.prepare(mediaSource, false, false)

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