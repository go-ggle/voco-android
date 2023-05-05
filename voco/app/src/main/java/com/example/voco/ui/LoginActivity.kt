package com.example.voco.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.voco.api.ApiData
import com.example.voco.api.ApiRepository
import com.example.voco.databinding.ActivityLoginBinding
import com.example.voco.login.Glob
import com.example.voco.login.LoginCallback
import com.kakao.sdk.auth.LoginClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityLoginBinding
    private val loginCallback = LoginCallback(this)
    private val apiRepository = ApiRepository(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

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
            if (Pattern.matches(Glob.emailValidation, viewBinding.email.text) && Pattern.matches(Glob.pwValidation, viewBinding.password.text) && viewBinding.password.text.length >= 8){
                viewBinding.warningEmail.visibility = View.INVISIBLE
                viewBinding.warningPassword.visibility = View.INVISIBLE
                viewBinding.progressBar.visibility = View.VISIBLE

                // send login request
                CoroutineScope(Dispatchers.Default).launch{
                    CoroutineScope(Dispatchers.IO).async { apiRepository.emailLogin(ApiData.LoginRequest(viewBinding.email.text.toString(), viewBinding.password.text.toString()))}.onAwait
                    CoroutineScope(Dispatchers.Main).async{viewBinding.progressBar.visibility = View.GONE}
                }

            }
            else {
                // check email format
                if(!Pattern.matches(Glob.emailValidation, viewBinding.email.text)){
                    viewBinding.warningEmail.visibility = View.VISIBLE
                }else{
                    viewBinding.warningEmail.visibility = View.INVISIBLE
                }
                // check password format
                if(!Pattern.matches(Glob.pwValidation, viewBinding.password.text) || viewBinding.password.text.length < 8){
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
                LoginClient.instance.loginWithKakaoAccount(this,callback = loginCallback.kakao)
            }
        }
    }

}

