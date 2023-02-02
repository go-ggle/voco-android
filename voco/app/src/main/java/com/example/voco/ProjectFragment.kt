package com.example.voco

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.voco.data.ProjectAdapter
import com.example.voco.data.VerticalItemDecoration
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
    ): View? {
        binding = FragmentProjectBinding.inflate(layoutInflater)
        binding.projectList.adapter = ProjectAdapter(bottomNavigationActivity)
        binding.projectList.addItemDecoration(VerticalItemDecoration(50))

        binding.projectAddButton.setOnClickListener {
            val intent = Intent(bottomNavigationActivity, AddProjectActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }
}