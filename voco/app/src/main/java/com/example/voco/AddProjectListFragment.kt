package com.example.voco

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.voco.databinding.FragmentAddProjectListBinding

class AddProjectListFragment : Fragment() {
    private lateinit var binding: FragmentAddProjectListBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddProjectListBinding.inflate(layoutInflater)
        return binding.root
    }
}