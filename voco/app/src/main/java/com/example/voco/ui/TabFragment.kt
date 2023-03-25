package com.example.voco.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.voco.data.adapter.ProjectAdapter
import com.example.voco.data.adapter.VerticalItemDecoration
import com.example.voco.data.model.Project
import com.example.voco.databinding.FragmentTabBinding

class TabFragment(private val projectList: ArrayList<Project>) : Fragment() {
    private lateinit var binding : FragmentTabBinding
    private lateinit var bottomNavigationActivity : BottomNavigationActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavigationActivity = context as BottomNavigationActivity
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
            adapter = ProjectAdapter(bottomNavigationActivity,0, projectList )
            addItemDecoration(VerticalItemDecoration(2))
        }
    }
}