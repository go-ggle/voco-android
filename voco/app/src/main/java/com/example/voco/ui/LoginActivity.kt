package com.example.voco.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.voco.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }
}
