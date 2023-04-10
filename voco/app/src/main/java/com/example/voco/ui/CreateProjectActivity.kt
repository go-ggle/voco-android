package com.example.voco.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import com.example.voco.R
import com.example.voco.data.adapter.BlockAdapter
import com.example.voco.data.adapter.VerticalItemDecoration
import com.example.voco.data.model.ProjectInfo
import com.example.voco.databinding.ActivityCreateProjectBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CreateProjectActivity : AppCompatActivity(), BlockAdapter.IntervalPicker {
    private lateinit var binding: ActivityCreateProjectBinding
    private lateinit var adapter: BlockAdapter
    private var intervalChangeItemPos = -1
    private val projectId : Int = 0
    private var projectInfoList = arrayListOf(ProjectInfo(projectId,0,0,"",0,0.01))
    @RequiresApi(Build.VERSION_CODES.O)
    var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm 저장됨")

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val window = window
        window.setBackgroundDrawableResource(R.color.light_purple)
        binding.date.text = LocalDateTime.now().format(dateFormatter)

        adapter = BlockAdapter(this, projectId, projectInfoList)
        binding.addprojectList.run {
            adapter = adapter
            addItemDecoration(VerticalItemDecoration(20))
        }

        // interval picker
        binding.intervalPicker.run {
            minute.minValue=0
            minute.maxValue=20
            second.minValue =0
            second.maxValue=59
            msecond.minValue=0
            msecond.maxValue=59

            cancelButton.setOnClickListener {
                closeIntervalPicker(false)
            }
            confirmButton.setOnClickListener {
                closeIntervalPicker(true)
            }
        }

        // back button
        binding.backButton.setOnClickListener {
            super.onBackPressed()
        }
        // add new text block at last index
        binding.addprojectAddButton.setOnClickListener {
            (binding.addprojectList.adapter as BlockAdapter).addProjectInfo(adapter.itemCount)
        }
    }
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        binding.addprojectList.clearFocus()
        return super.dispatchTouchEvent(ev)
    }
    override fun openIntervalPicker(position: Int, min:Int, sec:Int, msec:Int){
        intervalChangeItemPos = position
        binding.intervalPicker.run {
            minute.value = min
            second.value = sec
            msecond.value = msec
            root.visibility = View.VISIBLE
        }
    }
    private fun closeIntervalPicker(isUpdate: Boolean) {
        if(isUpdate)
            (binding.addprojectList.adapter as BlockAdapter).updateInterval(intervalChangeItemPos,binding.intervalPicker.minute.value,binding.intervalPicker.second.value,binding.intervalPicker.msecond.value)
        binding.intervalPicker.root.visibility = View.GONE
        intervalChangeItemPos = -1
    }

}