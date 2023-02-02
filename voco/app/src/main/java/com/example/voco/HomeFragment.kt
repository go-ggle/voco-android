package com.example.voco

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.data.CountryAdapter
import com.example.voco.data.HorizontalItemDecoration
import com.example.voco.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var bottomNavigationActivity : BottomNavigationActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bottomNavigationActivity = context as BottomNavigationActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        binding.homeCountryList.adapter = CountryAdapter(bottomNavigationActivity)
        binding.homeCountryList.addItemDecoration(HorizontalItemDecoration(55))
        return binding.root
    }

}