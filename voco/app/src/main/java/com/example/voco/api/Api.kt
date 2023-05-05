package com.example.voco.api

import com.example.voco.data.model.Project
import com.example.voco.data.model.Team
import com.example.voco.data.model.Voice
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface Api {
    // 로그인
    @POST("/auth/login")
    suspend fun login(
        @Body request : ApiData.LoginRequest
    ): Response<HashMap<String,String>>

    // 카카오 로그인
    @POST("/oauth/kakao")
    suspend fun kakaoLogin(
        @Body request : HashMap<String, String>
    ): Response<HashMap<String,String>>

    // 회원가입
    @POST("/auth/signup")
    suspend fun signup(
        @Body request : ApiData.SignupRequest
    ): Response<HashMap<String,String>>

    // 훈련용 문장 조회
    @GET("/train-data")
    suspend fun getSentence() : Response<HashMap<String, List<ApiData.SentenceResponse>>>

    // 목소리 등록
    @Multipart
    @POST("/inputs/{textId}")
    suspend fun setVoice(
        @Path("textId") textId: Int,
        @Part audio: MultipartBody.Part
    ) : Response<HashMap<String,Double>>

    // 팀 목록 조회
    @GET("/teams")
    suspend fun getTeamList(): Response<List<Team>>

    // 팀 생성
    @POST("/teams")
    suspend fun createTeam(
        @Body request : HashMap<String,String>
    ): Response<Team>

    // 팀 참여
    @POST("/teams/{teamCode}")
    suspend fun joinTeam(
        @Path("teamCode") teamCode: String
    ): Response<Team>

    // 팀 내 모델 생성된 유저 목록 조회
    @GET("/teams/{teamId}/voices")
    suspend fun getVoice(
        @Path("teamId") teamId: Int
    ) : Response<List<Voice>>

    // 프로젝트 생성
    @POST("/teams/{teamId}/projects")
    suspend fun createProject(
        @Path("teamId") teamId: Int,
        @Body request : ApiData.CreateProjectRequest
    ) : Response<Project>

    // 프로젝트 목록 조회
    @GET("/teams/{teamId}/projects")
    suspend fun getProjectList(
        @Path("teamId") teamId: Int,
    ): Response<HashMap<String, List<Project>>>

    // 북마크 생성
    @POST("/bookmarks/{projectId}")
    suspend fun createBookmark(
        @Path("projectId") projectId: Int
    ): Response<HashMap<String, Int>>

    // 북마크 해제
    @DELETE("/bookmarks/{projectId}")
    suspend fun deleteBookmark(
        @Path("projectId") projectId: Int
    ): Response<HashMap<String, String>>
}