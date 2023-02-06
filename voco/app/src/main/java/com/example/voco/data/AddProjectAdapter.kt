package com.example.voco.data

import android.content.Context
import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.data.model.ProjectInfo
import com.example.voco.databinding.FragmentAddProjectListBinding

class AddProjectAdapter (context: Context, projectId: Int, var projectInfoList : ArrayList<ProjectInfo>) : RecyclerView.Adapter<AddProjectAdapter.ViewHolder>() {
    private lateinit var binding: FragmentAddProjectListBinding
    private val projectId = projectId
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItemCount(): Int = projectInfoList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddProjectAdapter.ViewHolder {
        binding = FragmentAddProjectListBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddProjectAdapter.ViewHolder, position: Int) {
        holder.bind(projectInfoList[position])
    }

    inner class ViewHolder(private val binding: FragmentAddProjectListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(projectInfo: ProjectInfo){
            binding.projectEditText.setText(projectInfo.content)
            binding.projectIntervalButton.text = "인터벌 ${projectInfo.interval}초"

            binding.projectAddButton.setOnClickListener {
                addProjectInfo(adapterPosition)
            }

            binding.projectDeleteButton.setOnClickListener {
                projectInfoList.removeAt(adapterPosition)
                this@AddProjectAdapter.notifyItemRemoved(adapterPosition)
            }
            // language 선택
            binding.menuLanguage.setOnClickListener {

            }
            // interval 선택
            binding.projectIntervalButton.setOnClickListener {

            }
        }
    }
    // 프로젝트 block 추가
    fun addProjectInfo(position: Int){
        projectInfoList.add(ProjectInfo(projectId,0,0,"",0.01))
        this@AddProjectAdapter.notifyItemInserted(position+1)
    }
    // 프로젝트 초기화
    fun deleteProjectInfoAll(size: Int){
        projectInfoList = arrayListOf(ProjectInfo(projectId,0,0,"",0.01))
        this@AddProjectAdapter.notifyItemRangeRemoved(0,size-1)
    }

}