package com.example.voco.api

import com.example.voco.data.model.Block

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
    data class UpdateProjectRequest(
        val title: String,
        val blockList: List<Block>
    )
}