package com.example.voco.login

import android.app.Application
import com.example.voco.data.model.PreferenceUtil
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }
    override fun onCreate() {
        super.onCreate()

        prefs = PreferenceUtil(applicationContext)
        KakaoSdk.init(this, "8088d94fe2a14aace007f77409c82402")

    }
}