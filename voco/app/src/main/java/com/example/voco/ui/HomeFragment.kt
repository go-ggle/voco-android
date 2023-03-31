package com.example.voco.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.voco.api.Api
import com.example.voco.api.ApiRepository
import com.example.voco.api.RetrofitClient
import com.example.voco.component.TeamBottomSheet
import com.example.voco.data.adapter.*
import com.example.voco.data.model.Project
import com.example.voco.databinding.FragmentHomeBinding
import com.example.voco.login.GlobalApplication
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var bottomNavigationActivity : BottomNavigationActivity
    private val bottomSheet = TeamBottomSheet()
    private lateinit var apiRepository : ApiRepository
    var projectList = listOf<Project>()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavigationActivity = context as BottomNavigationActivity
        apiRepository = ApiRepository(bottomNavigationActivity)
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

        apiRepository.getTeamList(binding, bottomNavigationActivity)
        apiRepository.getProjectList(binding, parentFragmentManager)
    }

}