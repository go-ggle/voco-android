package com.example.voco.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.voco.api.ApiRepository
import com.example.voco.data.adapter.ProjectAdapter
import com.example.voco.data.adapter.VerticalItemDecoration
import com.example.voco.data.model.AppDatabase
import com.example.voco.data.model.Project
import com.example.voco.databinding.FragmentSearchBinding
import com.example.voco.login.GlobalApplication

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var bottomNavigationActivity : BottomNavigationActivity
    private lateinit var searchProjectList : ArrayList<Project>
    private lateinit var localDb : AppDatabase
    private lateinit var projectList : ArrayList<Project>
    private lateinit var apiRepository : ApiRepository
    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavigationActivity = context as BottomNavigationActivity
        localDb = AppDatabase.getProjectInstance(bottomNavigationActivity)!!
        apiRepository = ApiRepository(bottomNavigationActivity)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        projectList = localDb.ProjectDao().selectAll() as ArrayList<Project>
        binding = FragmentSearchBinding.inflate(layoutInflater)
        binding.projectList.run{
            adapter = ProjectAdapter(bottomNavigationActivity,1, projectList)
            addItemDecoration(VerticalItemDecoration(28))
        }
        // create project button
        binding.projectAddButton.setOnClickListener {
            when(GlobalApplication.prefs.getInt("defaultVoiceId", 0)){
                0->{
                    Toast.makeText(context, "사용 가능한 더빙보이스가 없습니다.\n          목소리를 녹음해주세요         ", Toast.LENGTH_SHORT).show()
                }
                else->{
                    // title, language 작성하는 모달창 넣기
                    apiRepository.createProject("",0)
                }
            }
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