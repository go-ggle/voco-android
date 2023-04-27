package com.example.voco.api

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.example.voco.R
import com.example.voco.data.adapter.HorizontalItemDecoration
import com.example.voco.data.adapter.TabAdapter
import com.example.voco.data.adapter.TeamAdapter
import com.example.voco.data.model.*
import com.example.voco.databinding.BottomSheetTeamBinding
import com.example.voco.databinding.FragmentHomeBinding
import com.example.voco.login.GlobalApplication
import com.example.voco.ui.BottomNavigationActivity
import com.example.voco.ui.CreateProjectActivity
import com.example.voco.ui.LoginActivity
import com.example.voco.ui.SignupActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.*

open class ApiRepository(private val context: Context) {
    private var apiService = RetrofitClient.getRetrofitClient(true).create(Api::class.java)
    private val _prefs =  GlobalApplication.prefs
    private suspend fun showToast(textId: Int) = withContext(Main){
        Toast.makeText(context, context.resources.getString(textId), Toast.LENGTH_SHORT).show()
    }
    private fun updateLocalDb(type:String, data:List<Any>)= CoroutineScope(IO).launch{
        when(type){
            "project" -> {
                val localDao = AppDatabase.getProjectInstance(context)!!.ProjectDao()
                CoroutineScope(IO).async {
                    localDao.deleteAll()
                    localDao.insert(data as List<Project>) }.onAwait
            }
            "voice" -> {
                val localDao = AppDatabase.getVoiceInstance(context)!!.VoiceDao()
                CoroutineScope(IO).async {
                    localDao.deleteAll()
                    localDao.insert(data as List<Voice>) }.onAwait
            }
            else -> {
                val localDao = AppDatabase.getBlockInstance(context)!!.BlockDao()
                CoroutineScope(IO).async {
                    localDao.deleteAll()
                    localDao.insert(data as List<Block>) }.onAwait
            }
        }
    }
    private fun backToLoginPage(){
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
        (context as SignupActivity).finish()
    }
    private fun goToMainPage(){
        val intent = Intent(context, BottomNavigationActivity::class.java)
        context.startActivity(intent)
        (context as LoginActivity).finish()
    }
    private fun goToBlockPage(projectId: Int){
        val intent = Intent(context, CreateProjectActivity::class.java)
        intent.putExtra("project", projectId) // project id 넘기기
        context.startActivity(intent)
    }
    // signup request coroutine
    fun signup(user: ApiData.SignupRequest) = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java)
        try{
            val request = CoroutineScope(IO).async { apiService.signup(user) }
            val response = request.await()
            when(response.code()){
                201 ->{
                    showToast(R.string.toast_signup_success)
                    backToLoginPage()
                }
                409 -> showToast(R.string.toast_already_signup)
                else ->showToast(R.string.toast_request_error)
            }
        }catch (e: Exception){
            showToast(R.string.toast_network_error)
        }
    }
    // login request coroutine
    fun emailLogin(user: ApiData.LoginRequest) = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java)
        try{
            val request = CoroutineScope(IO).async { apiService.login(user) }
            val response = request.await()
            when(response.code()) {
                200 -> {
                    withContext(IO) {
                        _prefs.setString("token", response.body()?.get("accessToken")!!)
                        _prefs.setInt("workspace",response.body()?.get("privateTeamId")!!.toInt())
                        _prefs.setIdAndPwd(user)
                    }
                    goToMainPage()
                }
                404 -> showToast(R.string.toast_wrong_id_or_pwd)
                else -> showToast(R.string.toast_request_error)
            }
        }catch(e: Exception){
            showToast(R.string.toast_network_error)
        }
    }
    // refreshToken coroutine
    private fun refreshToken() = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java)
        try {
            val request = CoroutineScope(IO).async { when(_prefs.loginMode()){
                "sns" -> apiService.login(_prefs.getIdAndPwd())
                else -> apiService.login(_prefs.getIdAndPwd())
            }}
            val response = request.await()
            when(response.code()) {
                200 -> withContext(IO){ _prefs.refreshToken(response.body()?.get("accessToken")!!) }
            }
        }catch(e: Exception){
            return@launch
        }
    }
    fun createProject(title:String, language: Int) = CoroutineScope(Default).launch {
        try{
            val request = CoroutineScope(IO).async { apiService.createProject(_prefs.getCurrentTeam(),
                ApiData.CreateProjectRequest(title, language)
            ) }
            val response = request.await()
            when(response.code()){
                200 -> {
                    val localDao = AppDatabase.getProjectInstance(context)!!.ProjectDao()
                    localDao.insert(listOf(response.body()) as List<Project>)
                    val blockLocalDao = AppDatabase.getBlockInstance(context)!!.BlockDao()
                    blockLocalDao.insert(listOf(Block(1,"",_prefs.getInt("defaultVoiceId",0),"",0,0.01)) as List<Block>)
                    goToBlockPage(response.body()?.id!!)
                }
            }
        }catch (e:Exception){
            showToast(R.string.toast_network_error)
        }
    }
    // get project list request
    fun getProject(binding: FragmentHomeBinding, fm: FragmentManager) = CoroutineScope(Default).launch {
        try{
            val projectRequest = CoroutineScope(IO).async { apiService.getProjectList(_prefs.getCurrentTeam())}
            val projectResponse : Response<HashMap<String, List<Project>>>
            val response = projectRequest.await()
            // if login session is finished
            when (response.code()){
                401 ->{
                    refreshToken() // refresh token
                    projectResponse = projectRequest.await() // request again
                }
                else-> projectResponse = response
            }
            withContext(Main){
                binding.progressBar.visibility = View.GONE
            }
            when(projectResponse.code()) {
                200 ->{
                    // connect with project adapter
                    withContext(Main){
                        binding.projects.run{
                            adapter = TabAdapter(fm, projectResponse.body()?.get("projects")!!)
                            binding.menu.setupWithViewPager(binding.projects)
                        }
                    }
                    // save project list in local DB
                    updateLocalDb("project",projectResponse.body()?.get("projects")!!)
                }
            }
        }catch(e:Exception){
            showToast(R.string.toast_request_error)
        }
    }
    // get dubbing voice list request
    fun getVoice() = CoroutineScope(Default).launch {
        try{
            val voiceRequest = CoroutineScope(IO).async{ apiService.getVoice(_prefs.getCurrentTeam())}
            val voiceResponse = voiceRequest.await()
            when (voiceResponse.code()) {
                200 ->{
                    when (voiceResponse.body()?.isEmpty()) {
                        true -> {
                            // if no available dubbing voice
                            withContext(IO) { _prefs.setInt("defaultVoiceId", 0) }
                        }
                        else -> {
                            updateLocalDb("voice", voiceResponse.body()!!)
                            // set default dubbing voice
                            withContext(IO){_prefs.setInt("defaultVoiceId", voiceResponse.body()!![0].id)}
                        }
                    }
                }
            }
        }catch (e:Exception){
            showToast(R.string.toast_request_error)
        }
    }
    // get workspace list request
    fun getTeam(binding: FragmentHomeBinding) = CoroutineScope(Default).launch {
        try{
            val teamRequest = CoroutineScope(IO).async { apiService.getTeamList() }
            val teamResponse = teamRequest.await()
            when(teamResponse.code()){
                200 ->{
                    // connect with team adapter
                    withContext(Main){
                        binding.teams.run{
                            adapter = TeamAdapter(context, binding, teamResponse.body() as ArrayList<Team>)
                            addItemDecoration(HorizontalItemDecoration(12))
                        }
                    }
                }
            }
        }catch (e:Exception){
            showToast(R.string.toast_request_error)
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
                else -> showToast(R.string.toast_request_error)
            }
        }catch (e: Exception){
            showToast(R.string.toast_network_error)
        }
    }
    fun joinTeam(parentBinding: FragmentHomeBinding, binding: BottomSheetTeamBinding, code: String) = CoroutineScope(Default).launch {
        try{
            val request = CoroutineScope(IO).async { apiService.joinTeam(code) }
            val response = request.await()

            when(response.code()){
                201 ->{
                    withContext(Main){
                        binding.subTitle.text = "팀 스페이스에 참여되었습니다"
                        binding.btn.btnRect.text = "홈으로 이동하기"
                        binding.editText.visibility = View.GONE
                        binding.boldText.run{
                            visibility = View.VISIBLE
                            text = response.body()?.name // 팀 이름
                        }
                    }
                    // update team list
                    (parentBinding.teams.adapter as TeamAdapter).addTeam(response.body()!!)
                }
                404 -> showToast(R.string.toast_wrong_code)
                409 -> showToast(R.string.toast_already_join)
                else -> showToast(R.string.toast_request_error)
            }
        }catch (e: Exception){
            showToast(R.string.toast_network_error)
        }
    }
    // update project list at home page
    fun updateCurrentTeam(binding: FragmentHomeBinding) = CoroutineScope(Default).launch{
        try {
            val projectRequest = CoroutineScope(IO).async { apiService.getProjectList(_prefs.getCurrentTeam()) }
            val voiceRequest = CoroutineScope(IO).async{ apiService.getVoice(_prefs.getCurrentTeam())}
            val projectResponse : Response<HashMap<String, List<Project>>>
            val response = projectRequest.await()
            when (response.code()){
                401 ->{
                    refreshToken() // refresh token
                    projectResponse = projectRequest.await() // request again
                }
                else-> projectResponse = response
            }
            CoroutineScope(Default).launch {
                when(projectResponse.code()) {
                    200 -> {
                        withContext(Main){
                            (binding.projects.adapter as TabAdapter).updateProjectList(projectResponse.body()?.get("projects")!!)
                        }
                        updateLocalDb("project",projectResponse.body()?.get("projects")!!)
                    }
                    else -> showToast(R.string.toast_request_error)
                }
            }
            CoroutineScope(Default).launch {
                val voiceResponse = voiceRequest.await()
                when(voiceResponse.code()){
                    200 -> {
                        when(voiceResponse.body()!!.isEmpty()){
                            true -> {
                                // if no available dubbing voice
                                withContext(IO){ _prefs.setInt("defaultVoiceId", 0) }
                            }
                            else ->{
                                updateLocalDb("voice",voiceResponse.body()!!)
                                withContext(IO) { _prefs.setInt("defaultVoiceId", voiceResponse.body()!![0].id) }
                            }
                        }
                    }
                    else -> showToast(R.string.toast_request_error)
                }
            }
        } catch (e: Exception) {
            showToast(R.string.toast_network_error)
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
                200 -> withContext(IO){
                        val localDb = AppDatabase.getProjectInstance(context)
                        localDb!!.ProjectDao().updateBookmark(projectId, isChecked)
                    }
                else -> showToast(R.string.toast_request_error)
            }

        }catch (e: Exception){
            showToast(R.string.toast_network_error)
        }
    }
}