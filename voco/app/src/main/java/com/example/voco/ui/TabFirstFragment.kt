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

class TabFirstFragment : Fragment() {
    private lateinit var binding : FragmentTabBinding
    private lateinit var bottomNavigationActivity : BottomNavigationActivity
    private var projectList : ArrayList<Project> = arrayListOf(
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,1),
        Project("영어 말하기대회 대본","2023년 01년 05일 16:22", 1,true,2),
        Project("프랑스 여행 브이로그","2023년 01년 05일 16:22", 5,false,3),
        Project("중국어 연습","2023년 01년 05일 16:22",3,true,4),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,5),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,6),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,7),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,8),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,9),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,10),
        Project("미국 여행 브이로그","2023년 01년 05일 16:22",0,true,11),
    )
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTabBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.projects.adapter = ProjectAdapter(bottomNavigationActivity,0,projectList)
        binding.projects.addItemDecoration(VerticalItemDecoration(2))
        super.onViewCreated(view, savedInstanceState)
    }

    fun newInstant() : TabFirstFragment
    {
        val args = Bundle()
        val frag = TabFirstFragment()
        frag.arguments = args
        return frag
    }
}