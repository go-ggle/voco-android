package com.example.voco.ui.page

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.voco.api.ApiRepository
import com.example.voco.login.Glob


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    val apiRepository = ApiRepository(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when(Glob.prefs.getString("token","logout")){
            "logout" -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            else -> { // if already login
                apiRepository.refreshToken()
            }
        }
    }
}