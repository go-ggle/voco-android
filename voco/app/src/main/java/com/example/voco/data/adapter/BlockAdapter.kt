package com.example.voco.data.adapter

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.data.model.ProjectInfo
import android.view.inputmethod.InputMethodManager
import com.example.voco.databinding.FragmentBlockBinding


class BlockAdapter (val context: Context, projectId: Int, var projectInfoList : ArrayList<ProjectInfo>) : RecyclerView.Adapter<BlockAdapter.ViewHolder>() {
    private lateinit var binding: FragmentBlockBinding
    private lateinit var intervalPicker: IntervalPicker
    private val projectId = projectId
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private val keyboard: InputMethodManager by lazy {
        context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    }
    override fun getItemCount(): Int = projectInfoList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = FragmentBlockBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        intervalPicker = context as IntervalPicker
        holder.bind(projectInfoList[position])
    }

    inner class ViewHolder(private val binding: FragmentBlockBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(projectInfo: ProjectInfo) {
            binding.projectEditText.setText(projectInfo.content)
            binding.projectEditText.requestFocus()
            keyboard.showSoftInput(binding.projectEditText, 0)
            when (projectInfo.intervalMinute) {
                0 -> binding.projectIntervalButton.text = "인터벌 ${projectInfo.intervalSecond}초"
                else -> binding.projectIntervalButton.text =
                    "인터벌 ${projectInfo.intervalMinute}분 ${projectInfo.intervalSecond}초"
            }

            binding.projectAddButton.setOnClickListener {
                addProjectInfo(adapterPosition + 1)
            }

            binding.projectDeleteButton.setOnClickListener {
                deleteProjectInfo(adapterPosition)
            }
            // language 선택
            binding.menuLanguage.setOnClickListener {

            }
            // interval 선택
            binding.projectIntervalButton.setOnClickListener {
                intervalPicker.openIntervalPicker(
                    adapterPosition,
                    projectInfo.intervalMinute,
                    projectInfo.intervalSecond.toInt(),
                    ((projectInfo.intervalSecond - projectInfo.intervalSecond.toInt()) * 100).toInt()
                )
            }
            binding.projectEditText.setOnFocusChangeListener { v, hasFocus ->
                when (hasFocus) {
                    true -> {
                        keyboard.showSoftInput(v, 0)
                    }
                    false -> {
                        projectInfo.content = binding.projectEditText.text.trim().toString()
                        keyboard.hideSoftInputFromWindow(binding.root.windowToken, 0)
                        // 음성 생성 요청 보내기
                    }
                }
            }
        }
    }
    // 프로젝트 block 추가
    fun addProjectInfo(position: Int){
        if(position-1>=0 && projectInfoList[position-1].content=="" || position-1<0 && projectInfoList[0].content=="")
            Toast.makeText(context,"내용을 작성해주세요",Toast.LENGTH_SHORT).show()
        else {
            keyboard.showSoftInput(binding.projectEditText, 0)
            projectInfoList.add(position,ProjectInfo(projectId, 0, 0, "", 0, 0.01))
            this@BlockAdapter.notifyItemInserted(position)
        }
    }
    // 프로젝트 block 삭제
    fun deleteProjectInfo(position: Int){
        projectInfoList.removeAt(position)
        this@BlockAdapter.notifyItemRemoved(position)
    }
    // 프로젝트 초기화
    fun deleteProjectInfoAll(size: Int){
        projectInfoList = arrayListOf(ProjectInfo(projectId,0,0,"",0,0.01))
        this@BlockAdapter.notifyItemRangeRemoved(0,size-1)
    }
    // 인터벌 수정
    fun updateInterval(position: Int, minute:Int, second: Int, msecond: Int){
        projectInfoList[position].intervalMinute = minute
        projectInfoList[position].intervalSecond = second+msecond*0.01
        this@BlockAdapter.notifyItemChanged(position)
    }
    interface IntervalPicker{
        fun openIntervalPicker(position: Int, minute:Int, second:Int, msecond:Int)
    }
}

