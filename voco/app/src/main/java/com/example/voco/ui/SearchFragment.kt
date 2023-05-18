package com.example.voco.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.voco.R
import com.example.voco.api.ApiRepository
import com.example.voco.data.adapter.ProjectAdapter
import com.example.voco.data.adapter.VerticalItemDecoration
import com.example.voco.data.model.AppDatabase
import com.example.voco.data.model.Project
import com.example.voco.databinding.FragmentSearchBinding
import com.example.voco.dialog.CreateProjectDialog
import com.example.voco.login.Glob

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var localDb : AppDatabase
    private lateinit var apiRepository : ApiRepository
    private lateinit var searchProjectList : ArrayList<Project>
    private lateinit var projectList : ArrayList<Project>
    private lateinit var dlg : CreateProjectDialog
    override fun onAttach(context: Context) {
        super.onAttach(context)
        localDb = AppDatabase.getProjectInstance(context)!!
        apiRepository = ApiRepository(context)
        dlg = CreateProjectDialog(context)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        projectList = localDb.ProjectDao().selectAll() as ArrayList<Project>

        binding = FragmentSearchBinding.inflate(inflater)
        binding.noProject.visibility = when(projectList.size){
            0 -> View.VISIBLE
            else -> View.GONE
        }
        binding.projectList.run{
            adapter = ProjectAdapter(R.id.menu_board, projectList, binding.progressBar)
            addItemDecoration(VerticalItemDecoration(28))
        }
        // create project button
        binding.projectAddButton.setOnClickListener {
            when(Glob.prefs.getInt("default_voice", 0)){
                0->{
                    Toast.makeText(context, "사용 가능한 더빙보이스가 없습니다.\n          목소리를 녹음해주세요         ", Toast.LENGTH_SHORT).show()
                }
                else->{
                    // title, language 작성하는 모달창
                    dlg.show()
                }
            }
        }
        // search project by title
        binding.search.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(keyword: String?): Boolean {
                searchProjectList = projectList.filter { project: Project -> project.title.contains(keyword.toString().trim()) } as ArrayList<Project>
                binding.projectList.adapter = ProjectAdapter(R.id.menu_board, searchProjectList, binding.progressBar)
                return true
            }

        })
        return binding.root
    }
}