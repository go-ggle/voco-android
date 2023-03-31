package com.example.voco.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.voco.databinding.ActivityVoiceRegistrationBinding

class VoiceRegistrationActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityVoiceRegistrationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityVoiceRegistrationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }
}