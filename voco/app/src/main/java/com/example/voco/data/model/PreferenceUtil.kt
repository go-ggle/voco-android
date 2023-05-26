package com.example.voco.data.model

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil(context: Context) {
    // id, pwd, sns, token, refresh_token, team, private_team, default_voice
    // default value: token - logout, refresh_token - logout, id - sns_user, pwd - sns_user, sns - 0, team - 0, workspace - 0, default_voice - 0
    private val prefs: SharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)

    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }

    fun setString(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }

    fun getInt(key: String, defValue: Int): Int {
        return prefs.getInt(key, defValue)
    }

    fun setInt(key: String, int: Int) {
        prefs.edit().putInt(key, int).apply()
    }
    // return the user's login route
    fun loginMode()= getInt("sns", SNS.EMAIL.ordinal)
    fun setIdAndPwd(user: Dto.LoginRequest){
        setString("id", user.email)
        setString("pwd", user.password)
    }
    fun setToken(accessToken: String, refreshToken: String) {
        setString("token", accessToken)
        setString("refresh_token", refreshToken)
    }
    fun getCurrentTeam() = when(getInt("team", 0)){
        0 -> getInt("private_team", 0) // if there is no selected team, return the user's private workspace
        else -> getInt("team", 0)
    }
    fun logout(){
        prefs.edit().clear().apply()
//        setString("token","logout")
//        setString("refresh_token", "logout")
//        setString("id","sns_user")
//        setString("pwd","sns_user")
//        setInt("sns",0)
//        setInt("team",0)
//        setInt("workspace",0)
//        setInt("default_voice",0)
    }
}