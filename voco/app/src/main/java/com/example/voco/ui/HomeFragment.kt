package com.example.voco.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.voco.component.TeamBottomSheet
import com.example.voco.data.adapter.*
import com.example.voco.data.model.Project
import com.example.voco.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var bottomNavigationActivity : BottomNavigationActivity
    private val bottomSheet = TeamBottomSheet()
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
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        binding.addTeamButton.setOnClickListener {
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.teams.run{
            adapter = TeamAdapter(bottomNavigationActivity)
            addItemDecoration(HorizontalItemDecoration(12))
        }
        binding.projects.run{
            adapter = TabAdapter(parentFragmentManager, projectList)
            binding.menu.setupWithViewPager(binding.projects)
        }
    }

}