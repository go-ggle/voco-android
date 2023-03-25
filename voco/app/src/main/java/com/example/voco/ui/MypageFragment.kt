package com.example.voco.ui

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.voco.R
import com.example.voco.databinding.FragmentBlockBinding
import com.example.voco.databinding.FragmentMypageBinding
import com.example.voco.login.GlobalApplication

class MypageFragment : Fragment() {
    private lateinit var binding: FragmentMypageBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMypageBinding.inflate(layoutInflater)
        binding.logout.setOnClickListener {
            GlobalApplication.prefs.setString("id","logout")
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }
}
