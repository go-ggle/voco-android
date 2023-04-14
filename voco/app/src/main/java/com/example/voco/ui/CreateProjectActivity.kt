package com.example.voco.ui

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.voco.R
import com.example.voco.data.adapter.BlockAdapter
import com.example.voco.data.adapter.VerticalItemDecoration
import com.example.voco.data.model.AppDatabase
import com.example.voco.data.model.Block
import com.example.voco.data.model.Project
import com.example.voco.databinding.ActivityCreateProjectBinding
import kotlin.properties.Delegates

class CreateProjectActivity : AppCompatActivity(), BlockAdapter.IntervalPicker {
    private var projectId by Delegates.notNull<Int>()
    private lateinit var binding: ActivityCreateProjectBinding
    private lateinit var localDb : AppDatabase
    private lateinit var blockAdapter: BlockAdapter
    private lateinit var project: Project
    private lateinit var blockList : ArrayList<Block>
    private var intervalChangeItemPos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProjectBinding.inflate(layoutInflater)
        localDb = AppDatabase.getProjectInstance(this)!!
        projectId = intent.getIntExtra("project",0)
        setContentView(binding.root)
        // get the project and project's text blocks
        project = localDb.ProjectDao().selectById(projectId)
        blockList = localDb.BlockDao().selectAll() as ArrayList<Block>
        blockAdapter = BlockAdapter(this, project, blockList)

        val window = window
        window.setBackgroundDrawableResource(R.color.light_purple)
        // set project's title and date
        binding.date.text = project.updatedAt
        binding.title.setText(project.title)

        // text block recycler view
        binding.addprojectList.run {
            adapter = blockAdapter
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
            (binding.addprojectList.adapter as BlockAdapter).addBlock(blockAdapter.itemCount)
        }
    }
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        binding.addprojectList.clearFocus()
        return super.dispatchTouchEvent(ev)
    }
    override fun openIntervalPicker(position: Int, minute:Int, second:Int, msecond:Int){
        intervalChangeItemPos = position
        binding.intervalPicker.run {
            this.minute.value = minute
            this.second.value = second
            this.msecond.value = msecond
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