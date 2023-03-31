package com.example.voco.api

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.example.voco.data.adapter.HorizontalItemDecoration
import com.example.voco.data.adapter.TabAdapter
import com.example.voco.data.adapter.TeamAdapter
import com.example.voco.databinding.FragmentHomeBinding
import com.example.voco.login.GlobalApplication
import com.example.voco.ui.BottomNavigationActivity
import com.example.voco.ui.HomeFragment
import com.example.voco.ui.LoginActivity
import com.kakao.sdk.auth.model.OAuthToken
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

open class ApiRepository(private val context: Context) {
    private lateinit var apiService : Api
    fun signup(user: ApiData.SignupRequest) = CoroutineScope(Main).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java)
        try{
            val request = CoroutineScope(IO).async { apiService.signup(user) }
            val response = request.await()
            when(response.code()){
                201 ->{
                    Toast.makeText(context, "성공적으로 회원가입 되었습니다", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                }
                409 -> {
                    Toast.makeText(context, "이미 존재하는 회원입니다", Toast.LENGTH_SHORT).show()
                }
                else ->{
                    Toast.makeText(context, " 회원가입에 실패했습니다. \n나중에 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        }catch (e: Exception){
            Toast.makeText(context, "   회원가입에 실패했습니다.  \n네트워크 상태를 확인해주세요", Toast.LENGTH_SHORT).show()
        }
    }
    fun login(user: ApiData.LoginRequest) = CoroutineScope(Main).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java)
        try{
            val request = CoroutineScope(IO).async { apiService.login(user) }
            val response = request.await()
            when(response.code()) {
                200 -> {
                    GlobalApplication.prefs.setString("id", response.body()?.get("accessToken")!!)
                    GlobalApplication.prefs.setString("workspace", response.body()?.get("privateTeamId")!!)
                    val intent = Intent(context, BottomNavigationActivity::class.java)
                    context.startActivity(intent)
                }
                404 -> {
                    Toast.makeText(context, "   등록되지 않은 회원입니다.   \n아이디와 비밀번호를 다시 확인해주세요", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(context, "  로그인에 실패했습니다. \n나중에 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        }catch(e: Exception){
            Toast.makeText(context, "  로그인에 실패했습니다.  \n네트워크 상태를 확인해주세요", Toast.LENGTH_SHORT).show()
        }
    }
    fun getTeamList(binding: FragmentHomeBinding, context: Context) = CoroutineScope(Main).launch {
        apiService = RetrofitClient.getRetrofitClient(true).create(Api::class.java)
        try{
            val request = CoroutineScope(IO).async { apiService.getTeamList() }
            val response = request.await()
            when(response.code()){
                200 ->{
                    binding.teams.run{
                        adapter = TeamAdapter(context, response.body()!!)
                        addItemDecoration(HorizontalItemDecoration(12))
                    }
                }
                else ->{
                    Toast.makeText(context, "팀목록 조회에 실패했습니다.\n나중에 다시 시도해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } catch (e: Exception){
            Toast.makeText(context, "팀목록 조회에 실패했습니다. \n네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
            println("에러: $e")
        }
    }
    fun getProjectList(binding: FragmentHomeBinding, fm: FragmentManager) = CoroutineScope(Main) .launch {
        apiService = RetrofitClient.getRetrofitClient(true).create(Api::class.java)
        try {
            val request = CoroutineScope(IO).async {
                apiService.getProjectList(
                    // 본인 개인 워크스페이스 id 저장하기
                    GlobalApplication.prefs.getString("team", "1")
                )
            }
            val response = request.await()
            when(response.code()) {
                200 ->{
                    binding.projects.run{
                        adapter = TabAdapter(fm, response.body()?.get("projects")!!)
                        binding.menu.setupWithViewPager(binding.projects)
                    }
                }
                else -> {
                    Toast.makeText(context, "프로젝트 조회에 실패했습니다.\n 나중에 다시 시도해주세요.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "프로젝트 조회에 실패했습니다. \n 네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
            println("에러: $e")
        }
    }


}