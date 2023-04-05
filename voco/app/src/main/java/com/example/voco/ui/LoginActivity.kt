package com.example.voco.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import com.example.voco.api.ApiData
import com.example.voco.api.ApiRepository
import com.example.voco.databinding.ActivityLoginBinding
import com.example.voco.login.GlobalApplication
import com.example.voco.login.LoginCallback
import com.kakao.sdk.auth.LoginClient
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityLoginBinding
    private val loginCallback = LoginCallback(this)
    private val apiRepository = ApiRepository(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        val defaultType = viewBinding.password.inputType
        // if already login
        if(GlobalApplication.prefs.getString("id","logout") != "logout"){
            // go to home view
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }
        // password visibility
        viewBinding.passwordVisibility.setOnCheckedChangeListener { _, isChecked ->
            when(isChecked){
                true -> viewBinding.password.inputType = InputType.TYPE_CLASS_TEXT
                else -> viewBinding.password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
        // sign up
        viewBinding.signUpButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
        // local login
        viewBinding.loginButton.setOnClickListener {
            // if email and password are correct
            if (Pattern.matches(GlobalApplication.emailValidation, viewBinding.email.text) && Pattern.matches(GlobalApplication.pwValidation, viewBinding.password.text) && viewBinding.password.text.length >= 8){
                viewBinding.warningEmail.visibility = View.INVISIBLE
                viewBinding.warningPassword.visibility = View.INVISIBLE
                // send login request
                apiRepository.login(ApiData.LoginRequest(viewBinding.email.text.toString(), viewBinding.password.text.toString()))
            }
            else {
                // check email format
                if(!Pattern.matches(GlobalApplication.emailValidation, viewBinding.email.text)){
                    viewBinding.warningEmail.visibility = View.VISIBLE
                }else{
                    viewBinding.warningEmail.visibility = View.INVISIBLE
                }
                // check password format
                if(!Pattern.matches(GlobalApplication.pwValidation, viewBinding.password.text) || viewBinding.password.text.length < 8){
                    viewBinding.warningPassword.visibility = View.VISIBLE
                }else{
                    viewBinding.warningPassword.visibility = View.INVISIBLE
                }
            }

        }
        // kakao login
        viewBinding.snsLoginKakao.setOnClickListener {
            // if kakaotalk application is available
            if(LoginClient.instance.isKakaoTalkLoginAvailable(this)){
                // login with kakaotalk application
                LoginClient.instance.loginWithKakaoTalk(this, callback = loginCallback.kakao)
            }
            else{
                // login with kakaotalk account
                LoginClient.instance.loginWithKakaoAccount(this, callback = loginCallback.kakao)
            }
        }
    }

}

