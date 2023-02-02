package com.example.voco

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.voco.databinding.ActivityAddProjectBinding

class AddProjectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProjectBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backButton.setOnClickListener {
            // 뒤로가기
            super.onBackPressed()
        }
    }
}