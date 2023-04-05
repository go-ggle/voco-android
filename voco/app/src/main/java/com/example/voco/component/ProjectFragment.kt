package com.example.voco.component

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.voco.api.ApiRepository
import com.example.voco.ui.BottomNavigationActivity
import com.example.voco.databinding.FragmentProjectBinding


class ProjectFragment : Fragment() {
    private lateinit var binding: FragmentProjectBinding
    private lateinit var bottomNavigationActivity : BottomNavigationActivity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavigationActivity = context as BottomNavigationActivity
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProjectBinding.inflate(layoutInflater)
        return binding.root
    }
}