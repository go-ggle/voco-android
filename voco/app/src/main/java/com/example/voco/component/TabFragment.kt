package com.example.voco.component

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.voco.R
import com.example.voco.data.adapter.ProjectAdapter
import com.example.voco.data.adapter.VerticalItemDecoration
import com.example.voco.data.model.Project
import com.example.voco.databinding.ActivityBottomNavigationBinding
import com.example.voco.databinding.FragmentTabBinding

class TabFragment(private val projectList: List<Project>) : Fragment() {
    private lateinit var binding : FragmentTabBinding
    private lateinit var parentBinding : ActivityBottomNavigationBinding
    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentBinding = ActivityBottomNavigationBinding.inflate(LayoutInflater.from(context))
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTabBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.projects.run{
            adapter = ProjectAdapter(R.id.menu_home, projectList as ArrayList<Project>, parentBinding.progressBar)
            addItemDecoration(VerticalItemDecoration(2))
        }
    }
    fun updateProjectList(newProjectList: List<Project>){
        (binding.projects.adapter as ProjectAdapter).updateProjectList(newProjectList as ArrayList<Project>)
    }
}