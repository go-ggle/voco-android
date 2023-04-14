package com.example.voco.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.voco.login.GlobalApplication


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = when(GlobalApplication.prefs.getString("token","logout")){
            "logout" -> Intent(this, LoginActivity::class.java)
            else -> Intent(this, BottomNavigationActivity::class.java) // if already login
        }
        startActivity(intent)
        finish()
    }
}