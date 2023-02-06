package com.example.voco.data

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.R
import com.example.voco.data.model.Project
import com.example.voco.databinding.FragmentProjectListBinding

class ProjectAdapter (context: Context, val projectList : ArrayList<Project>) : RecyclerView.Adapter<ProjectAdapter.ViewHolder>() {
    private lateinit var binding: FragmentProjectListBinding
    private val parent : Context = context
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getItemCount(): Int = projectList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectAdapter.ViewHolder {
        binding = FragmentProjectListBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectAdapter.ViewHolder, position: Int) {
        holder.bind(projectList[position])
    }

    inner class ViewHolder(private val binding: FragmentProjectListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(project: Project){
            binding.title.text = project.title
            binding.date.text = project.date
            binding.preview.text = project.content
            binding.countryList.adapter = ProjectCountryAdapter(parent,arrayListOf(
                R.drawable.ic_america,
                R.drawable.ic_china,
                R.drawable.ic_japan))
            binding.favorites.isChecked = project.isFavorites
            binding.countryList.addItemDecoration(HorizontalItemDecoration(10))
            binding.favorites.setOnCheckedChangeListener { buttonView, isChecked ->
                // db에 즐겨찾기 여부 update
            }
        }
    }
}