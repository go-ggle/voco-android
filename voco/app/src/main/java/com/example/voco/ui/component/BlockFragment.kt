package com.example.voco.ui.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.voco.databinding.FragmentBlockBinding

class BlockFragment : Fragment() {
    private lateinit var binding: FragmentBlockBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBlockBinding.inflate(layoutInflater)
        return binding.root
    }
}