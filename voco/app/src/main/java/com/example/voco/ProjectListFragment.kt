package com.example.voco

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.voco.data.HorizontalItemDecoration
import com.example.voco.data.ProjectCountryAdapter
import com.example.voco.databinding.FragmentProjectListBinding


class ProjectListFragment : Fragment() {
    private lateinit var binding: FragmentProjectListBinding
    private lateinit var bottomNavigationActivity : BottomNavigationActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavigationActivity = context as BottomNavigationActivity
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProjectListBinding.inflate(layoutInflater)
        return binding.root
    }
}