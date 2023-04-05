package com.example.voco.api

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.example.voco.data.adapter.HorizontalItemDecoration
import com.example.voco.data.adapter.TabAdapter
import com.example.voco.data.adapter.TeamAdapter
import com.example.voco.data.model.AppDatabase
import com.example.voco.data.model.Team
import com.example.voco.databinding.BottomSheetTeamBinding
import com.example.voco.databinding.FragmentHomeBinding
import com.example.voco.login.GlobalApplication
import com.example.voco.ui.BottomNavigationActivity
import com.example.voco.ui.HomeFragment
import com.example.voco.ui.LoginActivity
import com.example.voco.ui.SignupActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class ApiRepository(private val context: Context) {
    private var apiService = RetrofitClient.getRetrofitClient(true).create(Api::class.java)

    // signup request coroutine
    fun signup(user: ApiData.SignupRequest) = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java)
        try{
            val request = CoroutineScope(IO).async { apiService.signup(user) }
            val response = request.await()
            when(response.code()){
                201 ->{
                    withContext(Main){
                        Toast.makeText(context, "성공적으로 회원가입 되었습니다", Toast.LENGTH_SHORT).show()
                    }
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as SignupActivity).finish()
                }
                409 -> {
                    withContext(Main){
                        Toast.makeText(context, "이미 존재하는 회원입니다", Toast.LENGTH_SHORT).show()
                    }
                }
                else ->{
                    withContext(Main){
                        Toast.makeText(context, " 회원가입에 실패했습니다. \n나중에 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }catch (e: Exception){
            withContext(Main){
                Toast.makeText(context, "   회원가입에 실패했습니다.  \n네트워크 상태를 확인해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // login request coroutine
    fun login(user: ApiData.LoginRequest) = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java)
        try{
            val request = CoroutineScope(IO).async { apiService.login(user) }
            val response = request.await()
            when(response.code()) {
                200 -> {
                    CoroutineScope(IO).launch {
                        GlobalApplication.prefs.setString(
                            "id",
                            response.body()?.get("accessToken")!!
                        )
                        GlobalApplication.prefs.setString(
                            "workspace",
                            response.body()?.get("privateTeamId")!!
                        )
                    }
                    val intent = Intent(context, BottomNavigationActivity::class.java)
                    context.startActivity(intent)
                    (context as LoginActivity).finish()
                }
                404 -> {
                    withContext(Main){
                        Toast.makeText(context, "   등록되지 않은 회원입니다.   \n아이디와 비밀번호를 다시 확인해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    withContext(Main){
                        Toast.makeText(context, "  로그인에 실패했습니다. \n나중에 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }catch(e: Exception){
            withContext(Main){
                Toast.makeText(context, "  로그인에 실패했습니다.  \n네트워크 상태를 확인해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // get data for home page (team list and project list)
    fun getHomepageData(binding: FragmentHomeBinding, fm: FragmentManager, context: Context) = CoroutineScope(Default).launch {
        try{
            // get team list
            val teamRequest = CoroutineScope(IO).async { apiService.getTeamList() }
            // get project list
            val projectRequest = CoroutineScope(IO).async {
                apiService.getProjectList(
                    // specify team id
                    GlobalApplication.prefs.getString("team", GlobalApplication.prefs.getString("workspace","1"))
                )
            }
            val teamResponse = teamRequest.await()
            val projectResponse = projectRequest.await()
            withContext(Main){
                binding.progressBar.visibility = View.GONE
            }
            when {
                // if both requests are failed
                teamResponse.code() != 200 && projectResponse.code() != 200 ->{
                    withContext(Main){
                        Toast.makeText(context, "  조회에 실패했습니다.  \n나중에 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    // handle team request result
                    when(teamResponse.code()){
                        200 ->{
                            withContext(Main){
                                binding.teams.run{
                                    adapter = TeamAdapter(context, binding, teamResponse.body() as ArrayList<Team>)
                                    addItemDecoration(HorizontalItemDecoration(12))
                                }
                            }
                        }
                        else ->{
                            withContext(Main){
                            Toast.makeText(context, "팀목록 조회에 실패했습니다.\n나중에 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        }
                        }
                    }
                    // handle project request result
                    when(projectResponse.code()) {
                        200 ->{
                            withContext(IO){
                                // save project list in local DB
                                val localDb = AppDatabase.getProjectInstance(context)!!
                                if(localDb.ProjectDao().selectAll().isEmpty())
                                    localDb.ProjectDao().insert(projectResponse.body()?.get("projects")!!)
                            }
                            withContext(Main){
                                binding.projects.run{
                                    adapter = TabAdapter(fm, projectResponse.body()?.get("projects")!!)
                                    binding.menu.setupWithViewPager(binding.projects)
                                }
                            }
                        }
                        else -> {
                            withContext(Main){
                                Toast.makeText(context, "프로젝트 조회에 실패했습니다.\n 나중에 다시 시도해주세요.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception){
            Toast.makeText(context, "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
            println(GlobalApplication.prefs.getString("id","0"))
            println("에러")
            println(e)
        }
    }
    // create team workspace request
    fun createTeam(parentBinding: FragmentHomeBinding, binding: BottomSheetTeamBinding, teamName: String) = CoroutineScope(Default).launch {
        try{
            val request = CoroutineScope(IO).async { apiService.createTeam(hashMapOf(Pair("name",teamName))) }
            val response = request.await()

            when(response.code()){
                201 -> {
                    withContext(Main){
                        binding.subTitle.text = "초대코드를 공유해주세요"
                        binding.btn.btnRect.text = "초대코드 복사하기"
                        binding.editText.visibility = View.GONE
                        binding.boldText.run{
                            text = response.body()?.teamCode // 초대코드
                            visibility = View.VISIBLE
                        }
                        // update team list
                        (parentBinding.teams.adapter as TeamAdapter).addTeam(response.body()!!)
                    }
                }
                else -> {
                    withContext(Main){
                        Toast.makeText(context, "  팀 생성에 실패했습니다. \n 나중에 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }catch (e: Exception){
            withContext(Main){
                Toast.makeText(context, " 팀 생성에 실패했습니다. \n 네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // update project list at home page
    fun updateProjectList(binding: FragmentHomeBinding) = CoroutineScope(Default).launch{
        try {
            val request = CoroutineScope(IO).async {
                apiService.getProjectList(
                    // specify team id
                    GlobalApplication.prefs.getString("team", GlobalApplication.prefs.getString("workspace","1"))
                )
            }
            val response = request.await()
            when(response.code()) {
                200 ->{
                    withContext(IO) {
                        val localDb = AppDatabase.getProjectInstance(context)!!
                        localDb.ProjectDao().deleteAll()
                        localDb.ProjectDao().insert(response.body()?.get("projects")!!)
                    }
                    withContext(Main){
                        (binding.projects.adapter as TabAdapter).updateProjectList(response.body()?.get("projects")!!)
                    }
                }
                else -> {
                    withContext(Main){
                        Toast.makeText(context, "프로젝트 조회에 실패했습니다.\n 나중에 다시 시도해주세요.", Toast.LENGTH_SHORT)
                        .show()
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Main){
                Toast.makeText(context, "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun updateBookmark(projectId: Int, isChecked: Boolean) = CoroutineScope(Default).launch {
        try{
            val request = CoroutineScope(IO).async {
                when(isChecked){
                    true -> apiService.createBookmark(projectId)
                    else -> apiService.deleteBookmark(projectId)
                }
            }
            val response = request.await()
            when(response.code()){
                200 ->{
                    withContext(IO){
                        val localDb = AppDatabase.getProjectInstance(context)
                        localDb!!.ProjectDao().updateBookmark(projectId, isChecked)
                    }
                }
                else -> {
                    withContext(Main){
                        Toast.makeText(context, "북마크가 변경되지 않았습니다.\n나중에 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }catch (e: Exception){
            withContext(Main){
                Toast.makeText(context, "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}