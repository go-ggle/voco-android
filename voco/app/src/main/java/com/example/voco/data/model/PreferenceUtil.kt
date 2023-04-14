package com.example.voco.data.model

import android.content.Context
import android.content.SharedPreferences
import com.example.voco.api.ApiData

class PreferenceUtil(context: Context) {
    // id, pwd, sns, token, team, workspace, defaultVoiceId
    // if there are no value: token - logout, id - sns_user, pwd - sns_user, sns - email_user, team - 0, workspace - 0, defaultVoiceId - 0
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
    fun loginMode()= when(getString("id","sns_user")){
            "sns_user" -> "sns"
            else -> "email"
        }
    fun getIdAndPwd() = ApiData.LoginRequest(getString("id","sns_user"),getString("pwd","sns_user"))
    fun setIdAndPwd(user: ApiData.LoginRequest){
        setString("id", user.email)
        setString("pwd", user.password)
        setString("sns", "email_user")
    }
    fun refreshToken(token: String) {
        setString("token", token)
    }
    fun getCurrentTeam() = when(getInt("team", 0)){
        0 -> getInt("workspace", 0)
        else -> getInt("team", 0)
    }
    fun logout(){
        setString("token","logout")
        setString("id","logout")
        setString("pwd","logout")
        setString("sns","logout")
        setInt("team",0)
        setInt("workspace",0)
        setInt("defaultVoiceId",0)
    }
}