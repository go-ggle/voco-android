package com.example.voco.login

import android.app.Application
import com.example.voco.data.model.PreferenceUtil
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    companion object {
        // shared preference object
        lateinit var prefs: PreferenceUtil
        // 검사 정규식
        val emailValidation = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        val pwValidation = """^[0-9a-zA-Z!@#$%^+\-=]*$"""
    }
    override fun onCreate() {
        super.onCreate()

        prefs = PreferenceUtil(applicationContext)
        KakaoSdk.init(this, "8088d94fe2a14aace007f77409c82402")

    }
}