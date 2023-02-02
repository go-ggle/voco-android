package com.example.voco

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.voco.data.ProjectAdapter
import com.example.voco.data.VerticalItemDecoration
import com.example.voco.databinding.FragmentFavoritesBinding

class FavoritesFragment : Fragment() {
    private lateinit var binding: FragmentFavoritesBinding
    private lateinit var bottomNavigationActivity : BottomNavigationActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavigationActivity = context as BottomNavigationActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoritesBinding.inflate(layoutInflater)
        binding.favoritesList.adapter = ProjectAdapter(bottomNavigationActivity)
        binding.favoritesList.addItemDecoration(VerticalItemDecoration(50))
        return binding.root
    }
}