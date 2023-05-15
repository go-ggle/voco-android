package com.example.voco.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.voco.api.ApiRepository
import com.example.voco.component.TeamBottomSheet
import com.example.voco.databinding.FragmentHomeBinding
import androidx.core.app.ActivityCompat

class HomeFragment : androidx.fragment.app.Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var bottomSheet : TeamBottomSheet
    private lateinit var apiRepository : ApiRepository

    override fun onAttach(context: Context) {
        super.onAttach(context)
        apiRepository = ApiRepository(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        bottomSheet = TeamBottomSheet(binding, apiRepository)
        binding.addTeamButton.setOnClickListener {
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiRepository.getTeam(binding)
        apiRepository.getProject(binding, parentFragmentManager)
        apiRepository.getVoice()
    }

}