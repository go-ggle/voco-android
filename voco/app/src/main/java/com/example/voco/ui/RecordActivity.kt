package com.example.voco.ui

import android.content.pm.PackageManager
import android.graphics.Point
import android.media.*
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.voco.R
import com.example.voco.api.ApiData
import com.example.voco.api.ApiRepository
import com.example.voco.data.adapter.RecordAdapter
import com.example.voco.databinding.ActivityRecordBinding
import com.example.voco.service.MediaService
import com.ramijemli.percentagechartview.PercentageChartView
import java.io.File
import java.util.*
import kotlin.math.abs
import kotlin.system.exitProcess

class RecordActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityRecordBinding
    private lateinit var sentenceList : List<ApiData.SentenceResponse>
    private val apiRepository = ApiRepository(this)
    private var currItem = 1
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        sentenceList = intent.getSerializableExtra("sentences") as List<ApiData.SentenceResponse>
        getPermission() // get record permission

        val callback: OnPageChangeCallback = object : OnPageChangeCallback() {
            override fun onPageSelected(pos: Int) {
                super.onPageSelected(pos)
                val marginPx = dpToPx(26)
                val barWidthPx = viewBinding.bar.width-marginPx
                val textViewPx = viewBinding.progressText.width
                val locationX = barWidthPx*(pos+1)*0.0125.toFloat() - textViewPx/2
                viewBinding.bar.value = (pos+1)*0.0125.toFloat()
                viewBinding.progressText.run{
                    text = (pos+1).toString()+"/80"
                    translationX = when {
                        locationX <= 0 -> 0F
                        locationX > barWidthPx - textViewPx -> (barWidthPx - textViewPx).toFloat()
                        else -> locationX
                    }
                }
            }
        }
        viewBinding.prevRecord.run {
            setOnClickListener {
                if(currItem>0) prevSentence()
            }
            if(currItem == 1) alpha = 0.3F
        }
        viewBinding.nextRecord.run {
            setOnClickListener {
                if(it.alpha == 1F) nextSentence()
                val fileName = "${externalCacheDir?.absolutePath}/${currItem}" + ".wav"
                it.alpha = if(File(fileName).exists())
                    1F
                else
                    0.3F

            }
            val fileName = "${externalCacheDir?.absolutePath}/${currItem}" + ".wav"
            alpha = if(File(fileName).exists())
                1F
            else
                0.3F
        }

        val display = this.display
        val size = Point()
        display!!.getSize(size) // or getSize(size)
        val height = size.y
        val offsetPx = (height - (viewBinding.header.height + viewBinding.recordButton.height + dpToPx(100)) - dpToPx(10))/2
        viewBinding.recordVoice.run {
            offscreenPageLimit = 80
            isUserInputEnabled = false
            adapter = RecordAdapter(sentenceList) // match adapter
            registerOnPageChangeCallback(callback) // match page change callback
            setPageTransformer { page, position ->
                page.run {
                    // make side view
                    translationY = position * -offsetPx
                    // if page is side view, give alpha 0.3F
                    alpha = when(position){
                        0F -> 1F
                        else -> 0.3F
                    }

                    (findViewById<ImageView>(R.id.record_fin_check)).visibility = when(position<0){
                        true -> View.VISIBLE
                        else -> View.GONE
                    }
                    (findViewById<PercentageChartView>(R.id.record_percent)).visibility = when(position){
                        0F -> View.VISIBLE
                        else -> View.INVISIBLE
                    }
                    (findViewById<AppCompatButton>(R.id.record_play_button)).run {
                        val fileName = "${externalCacheDir?.absolutePath}/${currItem}" + ".wav"
                        visibility = when(position){
                            0F -> View.VISIBLE
                            else -> View.GONE
                        }
                        setOnClickListener {
                            if(MediaService.player==null) {
                                MediaService.startPlaying(page, fileName)
                            }
                        }
                    }

                }
                val minScale = 0.85f // scale
                val scaleFactor = minScale.coerceAtLeast(1 - abs(position * getEase(abs(position))))
                if (position < -1) return@setPageTransformer
                // page scale
                if (position <= 1) {
                    // animation views
                    page.run{
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                    }
                } else {
                    // side views
                    page.run{
                        scaleX = minScale
                        scaleY = minScale
                    }
                }
            }
        }

        viewBinding.recordButton.setOnCheckedChangeListener { buttonView, isChecked ->
            val fileName = "${externalCacheDir?.absolutePath}/${currItem}" + ".wav"
            when(buttonView.alpha){
                1F->{
                    if(isChecked){
                        MediaService.startRecording(fileName, viewBinding)
                    }else{
                        MediaService.stopRecording()
                        apiRepository.setVoice(currItem, fileName, viewBinding)
                    }
                }
                else->{
                    buttonView.isChecked = !isChecked
                }
            }
        }
        viewBinding.backButton.setOnClickListener {
            super.onBackPressed()
            finish()
        }
    }

    private fun getPermission(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
            ActivityCompat.requestPermissions(this, permissions, 100)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty()) {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED)
                        exitProcess(0)
                }
            }
        }
    }
    private fun nextSentence(){
        if(viewBinding.recordVoice.currentItem < 80){
            val nextItem = viewBinding.recordVoice.currentItem + 1
            currItem = nextItem + 1
            viewBinding.recordVoice.setCurrentItem(nextItem, true)
        }
        viewBinding.prevRecord.alpha = 1F
        viewBinding.nextRecord.alpha = 0.3F
    }
    private fun prevSentence(){
        if(viewBinding.recordVoice.currentItem > 0){
            val prevItem = viewBinding.recordVoice.currentItem - 1
            currItem = prevItem + 1
            viewBinding.recordVoice.setCurrentItem(prevItem, true)
        }
        if(currItem == 1)
            viewBinding.prevRecord.alpha = 0.3F
        viewBinding.nextRecord.alpha = 1F
    }
    private fun getEase(position: Float): Float {
        val sqt = position * position
        return sqt / (2.0f * (sqt - position) + 1.0f)
    }
    fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), this.resources.displayMetrics).toInt()
    }
}

