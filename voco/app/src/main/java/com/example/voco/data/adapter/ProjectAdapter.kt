package com.example.voco.data.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.R
import com.example.voco.api.ApiRepository
import com.example.voco.data.model.AppDatabase
import com.example.voco.data.model.Project
import com.example.voco.databinding.FragmentHomeBinding
import com.example.voco.databinding.FragmentProjectBinding
import com.example.voco.databinding.FragmentTabBinding

class ProjectAdapter (val context: Context, val pageId: Int, private var projectList : List<Project>) : RecyclerView.Adapter<ProjectAdapter.ViewHolder>() {
    private lateinit var binding: FragmentProjectBinding
    private lateinit var countryDb : AppDatabase
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val apiRepository = ApiRepository(context)
    override fun getItemCount(): Int = projectList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = FragmentProjectBinding.inflate(inflater, parent, false)
        countryDb = AppDatabase.getCountryInstance(context)!!
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
                3->R.drawable.ic_china
                4->R.drawable.ic_japan
                5->R.drawable.ic_france
                else->R.drawable.ic_germany
            })
            binding.favorites.setOnCheckedChangeListener { buttonView, isChecked ->
                apiRepository.updateBookmark(project.id, isChecked)
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateProjectList(newProjectList: List<Project>){
        projectList = newProjectList
        notifyDataSetChanged()
    }
}