package com.example.voco.data.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.R
import com.example.voco.api.ApiRepository
import com.example.voco.data.model.Language
import com.example.voco.data.model.Project
import com.example.voco.databinding.FragmentProjectBinding

class ProjectAdapter (val pageId: Int, private var projects : ArrayList<Project>, val progressBar: ProgressBar) : RecyclerView.Adapter<ProjectAdapter.ViewHolder>() {
    private lateinit var binding: FragmentProjectBinding
    private lateinit var apiRepository : ApiRepository
    private lateinit var dlg : AlertDialog.Builder

    override fun getItemCount(): Int = projects.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        binding = FragmentProjectBinding.inflate(inflater, parent, false)
        apiRepository = ApiRepository(parent.context)
        dlg = AlertDialog.Builder(parent.context,android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)

        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(projects[position])
    }

    inner class ViewHolder(private val binding: FragmentProjectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(project: Project){
            binding.run {
                title.text = project.title
                date.text = project.updatedAt
                icon.setBackgroundResource(
                    when (project.language) {
                        Language.ENGLISH.ordinal -> R.drawable.ic_america
                        Language.ENGLISH_UK.ordinal -> R.drawable.ic_united_kingdom
                        Language.GERMAN.ordinal -> R.drawable.ic_germany
                        Language.CHINESE.ordinal -> R.drawable.ic_china
                        Language.JAPANESE.ordinal -> R.drawable.ic_japan
                        Language.FRENCH.ordinal -> R.drawable.ic_france
                        else -> R.drawable.background_circle
                    }
                )
            }
            when(pageId){
                R.id.menu_home->{
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
                R.id.menu_board ->{
                    binding.line.setBackgroundResource(R.color.transparency)
                }
            }
            binding.favorites.run {
                isChecked = project.bookmarked
                setOnCheckedChangeListener { _, isChecked ->
                    apiRepository.updateBookmark(project.id, isChecked)
                }
            }
            binding.root.run{
                setOnClickListener {
                    progressBar.visibility = View.VISIBLE
                    apiRepository.getBlock(project.team, project.id, progressBar)
                }
                setOnLongClickListener {
                    dlg.run{
                        setTitle("프로젝트 삭제")
                        setMessage("프로젝트를 삭제하시겠습니까?")
                        setNegativeButton("아니요", DialogInterface.OnClickListener { dialog, _ ->
                            dialog.dismiss()
                        })
                        setPositiveButton("삭제할게요", DialogInterface.OnClickListener { _, _ ->
                            apiRepository.deleteProject(project.team, project.id, pageId, adapterPosition, this@ProjectAdapter)
                        })
                        show()
                    }
                    true
                }
            }
        }
    }
    fun updateProjectList(updatedProjects: ArrayList<Project>){
        projects = updatedProjects
        notifyDataSetChanged()
    }
    fun deleteProject(pos: Int){
        projects.removeAt(pos)
        notifyItemRemoved(pos)
    }
}