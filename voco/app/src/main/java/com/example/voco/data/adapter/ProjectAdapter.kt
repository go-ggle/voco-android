package com.example.voco.data.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.R
import com.example.voco.api.ApiRepository
import com.example.voco.data.model.AppDatabase
import com.example.voco.data.model.Project
import com.example.voco.databinding.FragmentProjectBinding
import com.example.voco.ui.CreateProjectActivity
import android.app.AlertDialog
import android.content.DialogInterface
import com.example.voco.databinding.FragmentSearchBinding
import com.example.voco.login.Glob

class ProjectAdapter (val pageId: Int, private var projectList : ArrayList<Project>) : RecyclerView.Adapter<ProjectAdapter.ViewHolder>() {
    private lateinit var binding: FragmentProjectBinding
    private lateinit var parentBinding: FragmentSearchBinding
    private lateinit var countryDb : AppDatabase
    private lateinit var apiRepository : ApiRepository
    private lateinit var parentContext: Context
    override fun getItemCount(): Int = projectList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = FragmentProjectBinding.inflate(inflater, parent, false)
        parentBinding = FragmentSearchBinding.inflate(inflater)
        countryDb = AppDatabase.getCountryInstance(parent.context)!!
        apiRepository = ApiRepository(parent.context)
        parentContext = parent.context
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(projectList[position])
    }

    inner class ViewHolder(private val binding: FragmentProjectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(project: Project){
            binding.title.text = project.title
            binding.date.text = project.updatedAt
            binding.favorites.isChecked = project.bookmarked

            when(pageId){
                0->{
                    binding.project.background = null
                    binding.favorites.visibility = View.GONE
                    if(itemCount == 1)
                        binding.line.setBackgroundResource(R.color.transparency)
                    else {
                        when (adapterPosition) {
                            0 -> binding.line.setBackgroundResource(R.color.light_gray3)
                            itemCount - 1 -> binding.line.setBackgroundResource(R.color.transparency)
                        }
                    }
                }
                1 ->{
                    binding.line.setBackgroundResource(R.color.transparency)
                }
            }
            binding.icon.setBackgroundResource(when(project.language){
                0->R.drawable.ic_america
                1->R.drawable.ic_united_kingdom
                2 -> R.drawable.ic_germany
                3->R.drawable.ic_china
                4->R.drawable.ic_japan
                else->R.drawable.ic_france
            })
            binding.favorites.setOnCheckedChangeListener { buttonView, isChecked ->
                apiRepository.updateBookmark(project.id, isChecked)
            }
            binding.root.setOnClickListener {
                apiRepository.getBlock(project.team, project.id, parentBinding)
            }
            binding.root.setOnLongClickListener {
                val dlg = AlertDialog.Builder(parentContext,android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dlg.setTitle("프로젝트 삭제")
                dlg.setMessage("프로젝트를 삭제하시겠습니까?")
                dlg.setNegativeButton("아니요", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
                dlg.setPositiveButton("삭제할게요", DialogInterface.OnClickListener { dialog, which ->
                    apiRepository.deleteProject(project.team, project.id, pageId, adapterPosition, parentBinding)
                })
                dlg.show()
                true
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateProjectList(newProjectList: ArrayList<Project>){
        projectList = newProjectList
        notifyDataSetChanged()
    }
    fun deleteProject(pos: Int){
        projectList.removeAt(pos)
        notifyItemRemoved(pos)
    }
    fun updateTitle(title:String, projectId:Int){
        val pos = projectList.indexOf(projectList.find{it.id == projectId})
        projectList[pos].title = title
        notifyItemChanged(pos)
    }
}