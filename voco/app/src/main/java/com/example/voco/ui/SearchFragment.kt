package com.example.voco.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.voco.data.adapter.ProjectAdapter
import com.example.voco.data.adapter.VerticalItemDecoration
import com.example.voco.data.model.Project
import com.example.voco.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var bottomNavigationActivity : BottomNavigationActivity
    private lateinit var searchProjectList : ArrayList<Project>
    private var projectList : ArrayList<Project> = arrayListOf(
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,0,1),
        Project("영어 말하기대회 대본","2023년 01년 05일 16:22", 1,true,0,2),
        Project("프랑스 여행 브이로그","2023년 01년 05일 16:22", 5,false,0,3),
        Project("중국어 연습","2023년 01년 05일 16:22",3,true,0,4),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,0,5),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,0,6),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,0,7),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,1,8),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,1,9),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,1,10),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,1,11),
    )
    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavigationActivity = context as BottomNavigationActivity
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        binding.projectList.adapter = ProjectAdapter(bottomNavigationActivity, 1, projectList)
        binding.projectList.addItemDecoration(VerticalItemDecoration(28))

        binding.projectAddButton.setOnClickListener {
            val intent = Intent(bottomNavigationActivity, CreateProjectActivity::class.java)
            startActivity(intent)
        }

        binding.search.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(keyword: String?): Boolean {
                searchProjectList = projectList.filter { project: Project -> project.title.contains(keyword.toString().trim()) } as ArrayList<Project>
                binding.projectList.adapter = ProjectAdapter(bottomNavigationActivity, 1, searchProjectList)
                return true
            }

        })
        return binding.root
    }
}