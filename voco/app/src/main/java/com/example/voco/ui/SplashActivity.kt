package com.example.voco.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.voco.login.Glob
import com.example.voco.api.ApiRepository


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = when(Glob.prefs.getString("token","logout")){
            "logout" -> Intent(this, LoginActivity::class.java)
            else -> {
                val apiRepository = ApiRepository(this)
                apiRepository.refreshToken()
                Intent(this, BottomNavigationActivity::class.java)
            } // if already login
        }
        startActivity(intent)
        finish()
    }
}