package com.example.voco

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toDrawable
import com.example.voco.data.AddProjectAdapter
import com.example.voco.data.VerticalItemDecoration
import com.example.voco.data.model.ProjectInfo
import com.example.voco.databinding.ActivityAddProjectBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AddProjectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProjectBinding
    private val projectId : Int = 0
    private var projectInfoList = arrayListOf(ProjectInfo(projectId,0,0,"",0.01))
    private lateinit var adapter: AddProjectAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm 작성됨")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = AddProjectAdapter(this, projectId, projectInfoList)
        binding.addprojectList.adapter = adapter
        binding.addprojectList.addItemDecoration(VerticalItemDecoration(30))
        binding.date.text = LocalDateTime.now().format(dateFormatter)

        // 뒤로가기
        binding.backButton.setOnClickListener {
            super.onBackPressed()
        }
        // 새로운 블럭 추가
        binding.addprojectAddButton.setOnClickListener {
            (binding.addprojectList.adapter as AddProjectAdapter).addProjectInfo(adapter.itemCount)
        }
    }
}