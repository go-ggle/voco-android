package com.example.voco.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.R
import com.example.voco.data.model.AppDatabase
import com.example.voco.data.model.Project
import com.example.voco.databinding.FragmentProjectListBinding

class ProjectAdapter (val context: Context, val pageId: Int, private val projectList : ArrayList<Project>) : RecyclerView.Adapter<ProjectAdapter.ViewHolder>() {
    private lateinit var binding: FragmentProjectListBinding
    private lateinit var countryDb : AppDatabase
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItemCount(): Int = projectList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = FragmentProjectListBinding.inflate(inflater, parent, false)
        countryDb = AppDatabase.getInstance(context)!!
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(projectList[position])
    }

    inner class ViewHolder(private val binding: FragmentProjectListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(project: Project){
            binding.title.text = project.title
            binding.date.text = project.date
            binding.favorites.isChecked = project.isFavorites

            when(pageId){
                0->{
                    binding.project.background = null
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
            val country = countryDb.CountryDao().selectById(project.countryId)
            binding.icon.setBackgroundResource(country.countryIcon)
            binding.favorites.setOnCheckedChangeListener { buttonView, isChecked ->
                // db에 즐겨찾기 여부 update
                project.isFavorites = isChecked
            }
        }
    }
}