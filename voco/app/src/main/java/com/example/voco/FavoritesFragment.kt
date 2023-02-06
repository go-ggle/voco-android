package com.example.voco

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.voco.data.ProjectAdapter
import com.example.voco.data.VerticalItemDecoration
import com.example.voco.data.model.Project
import com.example.voco.databinding.FragmentFavoritesBinding

class FavoritesFragment : Fragment() {
    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var bottomNavigationActivity : BottomNavigationActivity
    private lateinit var favoritesProjectList : ArrayList<Project>
    private lateinit var searchProjectList : ArrayList<Project>

    private var projectList : ArrayList<Project> = arrayListOf(
        Project("기학 발표","2023년 01년 05일 16:22","こんにちは。これは何でもありません。 卒は何でもありません。は何でもありません。", true),
        Project("기학 발표2","2023년 01년 05일 16:22","こんにちは。これは何でもありません。 卒は何でもありません。は何でもありません。", true),
        Project("기학 발표3","2023년 01년 05일 16:22","こんにちは。これは何でもありません。 卒は何でもありません。は何でもありません。", false),
        Project("기학 발표4","2023년 01년 05일 16:22","こんにちは。これは何でもありません。 卒は何でもありません。は何でもありません。", true),
        Project("기학 발표5","2023년 01년 05일 16:22","こんにちは。これは何でもありません。 卒は何でもありません。は何でもありません。",false),
        Project("기학 발표6","2023년 01년 05일 16:22","こんにちは。これは何でもありません。 卒は何でもありません。は何でもありません。",true),
        Project("기학 발표7","2023년 01년 05일 16:22","こんにちは。これは何でもありません。 卒は何でもありません。は何でもありません。",true),
        Project("기학 발표8","2023년 01년 05일 16:22","こんにちは。これは何でもありません。 卒は何でもありません。は何でもありません。",true),
        Project("기학 발표9","2023년 01년 05일 16:22","こんにちは。これは何でもありません。 卒は何でもありません。は何でもありません。",false),
        Project("기학 발표10","2023년 01년 05일 16:22","こんにちは。これは何でもありません。 卒は何でもありません。は何でもありません。",false)
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavigationActivity = context as BottomNavigationActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        favoritesProjectList = projectList.filter { project: Project -> project.isFavorites } as ArrayList<Project>
        binding = FragmentFavoritesBinding.inflate(layoutInflater)
        binding.favoritesList.adapter = ProjectAdapter(bottomNavigationActivity,favoritesProjectList)
        binding.favoritesList.addItemDecoration(VerticalItemDecoration(35))
        binding.search.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(keyword: String?): Boolean {
                searchProjectList = projectList.filter { project: Project -> project.title.contains(keyword.toString().trim()) } as ArrayList<Project>
                binding.favoritesList.adapter = ProjectAdapter(bottomNavigationActivity, searchProjectList)
                return true
            }

        })
        return binding.root
    }
}