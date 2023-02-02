package com.example.voco

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, BottomNavigationActivity::class.java)
        startActivity(intent)
        finish()
    }
}