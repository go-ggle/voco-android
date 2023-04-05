package com.example.voco.api

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

class ApiData {
    data class LoginRequest(
        val email:String,
        val password: String,
    )
    data class SignupRequest(
        val email:String,
        val nickname:String,
        val password: String,
    )
    data class CreateProjectRequest(
        val title:String,
        val language:Int,
    )
}