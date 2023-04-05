package com.example.voco.api

import com.example.voco.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface Api {
    // 로그인
    @POST("/auth/login")
    suspend fun login(
        @Body request : ApiData.LoginRequest
    ): Response<HashMap<String,String>>
    // 회원가입
    @POST("/auth/signup")
    suspend fun signup(
        @Body request : ApiData.SignupRequest
    ): Response<HashMap<String,String>>

    // 팀 목록 조회
    @GET("/teams")
    suspend fun getTeamList(): Response<List<Team>>

    // 팀 생성
    @POST("/teams")
    suspend fun createTeam(
        @Body request : HashMap<String,String>
    ): Response<Team>

    // 팀 참여
    

    // 프로젝트 생성
    @POST("/teams/{teamId}/projects")
    suspend fun createProject(
        @Path("teamId") teamId: String,
        @Body request : ApiData.CreateProjectRequest
    ) : Response<Project>

    // 프로젝트 목록 조회
    @GET("/teams/{teamId}/projects")
    suspend fun getProjectList(
        @Path("teamId") teamId: String,
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
    ): Response<String>
}