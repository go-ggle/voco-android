package com.example.voco.service

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
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
import com.example.voco.databinding.ActivityRecordBinding
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import okhttp3.internal.userAgent
import java.io.File


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
        // Cognito 샘플 코드. CredentialsProvider 객체 생성
        val credentialsProvider = CognitoCachingCredentialsProvider(
            context.applicationContext,
            "ap-northeast-2:167efb36-dea5-4724-935d-0c419fc48f12", // 자격 증명 풀 ID
            Regions.AP_NORTHEAST_2 // 리전
        )

        // 반드시 호출해야 한다.
        TransferNetworkLossHandler.getInstance(context.applicationContext)

        // TransferUtility 객체 생성
        val transferUtility = TransferUtility.builder()
            .context(context.applicationContext)
            .defaultBucket("Bucket_Name") // 디폴트 버킷 이름.
            .s3Client(AmazonS3Client(credentialsProvider, Region.getRegion(Regions.AP_NORTHEAST_2)))
            .build()

        // 다운로드 실행. object: "SomeFile.mp4". 두 번째 파라메터는 Local경로 File 객체.
        val downloadObserver = transferUtility.download("SomeFile.mp4", File("/SomeFile.mp4"))
        // 다운로드 과정을 알 수 있도록 Listener를 추가할 수 있다.
        downloadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    Log.d("AWS", "DOWNLOAD Completed!")
                }
            }

            override fun onProgressChanged(id: Int, current: Long, total: Long) {
                try {
                    val done = (((current.toDouble() / total) * 100.0).toInt()) //as Int
                    Log.d("AWS", "DOWNLOAD - - ID: $id, percent done = $done")
                }
                catch (e: Exception) {
                    Log.d("AWS", "Trouble calculating progress percent", e)
                }
            }

            override fun onError(id: Int, ex: Exception) {
                Log.d("AWS", "DOWNLOAD ERROR - - ID: $id - - EX: ${ex.message.toString()}")
            }
        })
    }
}