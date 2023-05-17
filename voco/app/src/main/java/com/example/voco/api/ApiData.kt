package com.example.voco.api

import com.example.voco.data.model.Block
import java.io.Serializable

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
    data class UpdateBlockRequest(
        val id: Int,
        val text: String,
        val audioPath: String,
        val interval: Int,
        val voiceId: Int,
        val language: Int,
        val order: Int
    )
    data class SentenceResponse(
        val textId: Int,
        val text: String
    ): Serializable
}