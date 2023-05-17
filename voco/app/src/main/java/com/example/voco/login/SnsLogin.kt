package com.example.voco.login

import android.app.Activity
import android.content.Context
import com.kakao.sdk.auth.LoginClient

class SnsLogin(val context: Context, currentActivity: Activity, isFinished: Boolean) {
    private val loginCallback = LoginCallback(context, currentActivity, isFinished)
    fun kakao() {
        // if kakaotalk application is available
        if(LoginClient.instance.isKakaoTalkLoginAvailable(context)){
            // login with kakaotalk application
            LoginClient.instance.loginWithKakaoTalk(context, callback = loginCallback.kakao)
        }
        else{
            // login with kakaotalk account
            LoginClient.instance.loginWithKakaoAccount(context,callback = loginCallback.kakao)
        }
    }
    fun naver(){

    }
    fun google(){

    }
}