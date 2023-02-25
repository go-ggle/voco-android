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

        adapter = BlockAdapter(this, projectId, projectInfoList)
        binding.addprojectList.adapter = adapter
        binding.addprojectList.addItemDecoration(VerticalItemDecoration(40))
        binding.date.text = LocalDateTime.now().format(dateFormatter)

        // 인터벌 선택 picker
        binding.intervalPicker.minute.minValue=0
        binding.intervalPicker.minute.maxValue=20
        binding.intervalPicker.second.minValue =0
        binding.intervalPicker.second.maxValue=59
        binding.intervalPicker.msecond.minValue=0
        binding.intervalPicker.msecond.maxValue=59

        binding.intervalPicker.cancelButton.setOnClickListener {
            closeIntervalPicker(false)
        }
        binding.intervalPicker.confirmButton.setOnClickListener {
            closeIntervalPicker(true)
        }

        // 뒤로가기
        binding.backButton.setOnClickListener {
            super.onBackPressed()
        }
        // 리스트 마지막에 새로운 블럭 추가
        binding.addprojectAddButton.setOnClickListener {
            (binding.addprojectList.adapter as BlockAdapter).addProjectInfo(adapter.itemCount)
        }
    }
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        binding.addprojectList.clearFocus()
        return super.dispatchTouchEvent(ev)
    }
    override fun openIntervalPicker(position: Int, minute:Int, second:Int, msecond:Int){
        intervalChangeItemPos = position
        binding.intervalPicker.minute.value = minute
        binding.intervalPicker.second.value = second
        binding.intervalPicker.msecond.value = msecond
        binding.intervalPicker.root.visibility = View.VISIBLE
    }
    private fun closeIntervalPicker(isUpdate: Boolean) {
        if(isUpdate)
            (binding.addprojectList.adapter as BlockAdapter).updateInterval(intervalChangeItemPos,binding.intervalPicker.minute.value,binding.intervalPicker.second.value,binding.intervalPicker.msecond.value)
        binding.intervalPicker.root.visibility = View.GONE
        intervalChangeItemPos = -1
    }

}