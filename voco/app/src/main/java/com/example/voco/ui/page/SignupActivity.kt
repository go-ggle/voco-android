package com.example.voco.ui.page

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.voco.data.model.Dto
import com.example.voco.api.ApiRepository
import com.example.voco.databinding.ActivitySignupBinding
import com.example.voco.login.Glob
import java.util.regex.Pattern


class SignupActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivitySignupBinding
    private val apiRepository = ApiRepository(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivitySignupBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        // back to login view
        viewBinding.backButton.setOnClickListener {
            super.onBackPressed()
            finish()
        }
        viewBinding.signupButton.setOnClickListener {
            // if all formats are correct
            if (Pattern.matches(Glob.emailValidation, viewBinding.email.text) && Pattern.matches(
                    Glob.pwValidation, viewBinding.password.text) && viewBinding.password.text.length >= 8 &&
                viewBinding.password.text.toString() == viewBinding.confirmPassword.text.toString() &&
                    viewBinding.nickname.text.length in 2..8){
                viewBinding.warningEmail.visibility = View.INVISIBLE
                viewBinding.warningPassword.visibility = View.INVISIBLE
                viewBinding.warningConfirmPassword.visibility = View.INVISIBLE
                viewBinding.warningNickname.visibility = View.INVISIBLE
                // send signup request
                apiRepository.signup(Dto.SignupRequest(viewBinding.email.text.toString(), viewBinding.nickname.text.toString(), viewBinding.password.text.toString()))
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
                // check whether confirm password is equal with password
                if(viewBinding.password.text.toString() != viewBinding.confirmPassword.text.toString()){
                    viewBinding.warningConfirmPassword.visibility = View.VISIBLE
                }else{
                    viewBinding.warningConfirmPassword.visibility = View.INVISIBLE
                }
                // check nickname format
                if(viewBinding.nickname.text.length !in 2..8){
                    viewBinding.warningNickname.visibility = View.VISIBLE
                }else{
                    viewBinding.warningNickname.visibility = View.INVISIBLE
                }

            }
        }
    }
}