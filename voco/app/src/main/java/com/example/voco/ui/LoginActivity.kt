package com.example.voco.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.voco.databinding.ActivityLoginBinding
import com.example.voco.login.GlobalApplication
import com.example.voco.login.LoginCallback
import com.kakao.sdk.auth.LoginClient

class LoginActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityLoginBinding
    private val loginCallback = LoginCallback(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        if(GlobalApplication.prefs.getString("id","logout") != "logout"){
            // 로그인이 되어있는 경우
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
        }
        // kakao login
        viewBinding.snsLoginKakao.setOnClickListener {
            // 카카오톡 실행이 가능하면
            if(LoginClient.instance.isKakaoTalkLoginAvailable(this)){
                // 카카오톡으로 로그인
                LoginClient.instance.loginWithKakaoTalk(this, callback = loginCallback.kakao)
            }
            // 카카오톡 실행이 불가능하면
            else{
                // 카카오 계정으로 로그인
                LoginClient.instance.loginWithKakaoAccount(this, callback = loginCallback.kakao)
            }
        }
    }

}

