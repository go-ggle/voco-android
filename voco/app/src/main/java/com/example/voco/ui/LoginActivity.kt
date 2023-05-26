package com.example.voco.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.voco.api.ApiRepository
import com.example.voco.data.model.Dto
import com.example.voco.databinding.ActivityLoginBinding
import com.example.voco.login.Glob
import com.example.voco.login.SnsLogin
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityLoginBinding
    private val snsLogin = SnsLogin(this, this, true)
    private val apiRepository = ApiRepository(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        // password visibility
        viewBinding.passwordVisibility.setOnCheckedChangeListener { _, isChecked ->
            viewBinding.password.run{
                inputType = when(isChecked){
                    true -> InputType.TYPE_CLASS_TEXT
                    else -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                setSelection(viewBinding.password.length())
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
                apiRepository.emailLogin(
                    Dto.LoginRequest(viewBinding.email.text.toString(), viewBinding.password.text.toString()),
                    viewBinding.progressBar
                )
            }
            else {
                // check email format
                viewBinding.warningEmail.visibility = if(!Pattern.matches(Glob.emailValidation, viewBinding.email.text)) View.VISIBLE
                                                      else View.INVISIBLE
                // check password format
                viewBinding.warningPassword.visibility = if(!Pattern.matches(Glob.pwValidation, viewBinding.password.text) || viewBinding.password.text.length < 8) View.VISIBLE
                                                         else View.INVISIBLE
            }
        }
        // kakao login
        viewBinding.snsLoginKakao.setOnClickListener {
            viewBinding.progressBar.visibility = View.VISIBLE
            finish()
            snsLogin.kakao()
        }
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            val dlg = AlertDialog.Builder(this,android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
            dlg.run{
                setTitle("VOCO 종료")
                setMessage("VOCO를 종료하시겠습니까?")
                setNegativeButton("아니요") { dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton("종료할게요") { _, _ ->
                    ActivityCompat.finishAffinity(this@LoginActivity)
                }
                show()
            }
            return true
        }
        return false
    }

}

