package com.example.voco.api

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import com.example.voco.R
import com.example.voco.data.adapter.*
import com.example.voco.data.model.*
import com.example.voco.databinding.*
import com.example.voco.login.Glob
import com.example.voco.ui.*
import com.google.android.gms.common.util.CollectionUtils.listOf
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.*
import java.util.*
import kotlin.collections.*

open class ApiRepository(private val context: Context) { // get activity context
    private var apiService = RetrofitClient.getRetrofitClient(true).create(Api::class.java) // retrofit client which has header

    // signup request coroutine
    fun signup(user: ApiData.SignupRequest) = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java) // retrofit client which doesn't have header
        try {
            val request = CoroutineScope(IO).async { apiService.signup(user) }
            val response = request.await()
            when (response.code()) {
                201 -> {
                    showToast(R.string.toast_signup_success)
                    startLoginActivity() // move to login activity
                }
                409 -> showToast(R.string.toast_already_signup)
                else -> showToast(R.string.toast_request_error)
            }
        } catch (e: Exception) {
            showToast(R.string.toast_network_error)
        }
    }

    // login request coroutine
    fun emailLogin(user: ApiData.LoginRequest, progressBar: ProgressBar) = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java) // retrofit client which doesn't have header
        try {
            val request = CoroutineScope(IO).async { apiService.login(user) }
            val response = request.await()
            endLoading(progressBar)
            when (response.code()) {
                200 -> {
                    withContext(IO) {
                        Glob.prefs.setString("token", response.body()?.get("accessToken")!!)
                        Glob.prefs.setInt("workspace", response.body()?.get("privateTeamId")!!.toInt())
                        Glob.prefs.setIdAndPwd(user)
                    }
                    startMainActivity()
                }
                404 -> showToast(R.string.toast_wrong_id_or_pwd)
                else -> showToast(R.string.toast_request_error)
            }
        } catch (e: Exception) {
            endLoading(progressBar)
            showToast(R.string.toast_network_error)
        }
    }

    fun snsLogin(accessToken: String, type: String) = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java)
        try {
            val request = CoroutineScope(IO).async { apiService.kakaoLogin(hashMapOf(Pair("accessToken", accessToken))) }
            val response = request.await()

            when (response.code()) {
                200 -> {
                    withContext(IO) {
                        Glob.prefs.setString("token", response.body()?.get("accessToken")!!)
                        Glob.prefs.setInt("workspace", response.body()?.get("privateTeamId")!!.toInt())
                        Glob.prefs.setString("id", "sns_user")
                        Glob.prefs.setString("sns", type)
                    }
                    startMainActivity()
                }
                else -> showToast(R.string.toast_request_error)
            }
        } catch (e: Exception) {
            showToast(R.string.toast_network_error)
        }
    }

    // refreshToken coroutine
    fun refreshToken() = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java)
        try {
            val refreshToken = Glob.prefs.getString("refresh_token", "logout")
            val request = CoroutineScope(IO).async { apiService.refreshToken(hashMapOf(Pair("refreshToken", refreshToken))) }
            val response = request.await()
            when (response.code()) {
                200 -> Glob.prefs.setToken(response.body()?.get("accessToken")!!)
                401 -> backLoginActivity()
            }
        } catch (e: Exception) {
            return@launch
        }
    }

    // create new project request
    fun createProject(title: String, language: Language) = CoroutineScope(Default).launch {
        try {
            val languageId = language.ordinal
            val request = CoroutineScope(IO).async { apiService.createProject(
                Glob.prefs.getCurrentTeam(),
                ApiData.CreateProjectRequest(title, languageId)
            ) }
            val response = request.await()
            when (response.code()) {
                201-> {
                    val localDao = AppDatabase.getProjectInstance(context)!!.ProjectDao()
                    val newProject: Project = response.body()!!

                    localDao.insert(listOf(newProject)) // save new project
                    startProjectActivity(newProject.id) // move to create project page
                }
                else-> showToast(R.string.toast_request_error)
            }
        } catch (e: Exception) {
            println(e)
            showToast(R.string.toast_network_error)
        }
    }
    fun updateProjectTitle(teamId: Int, projectId: Int, title: String) = CoroutineScope(Default).launch{
        try{
            val request = CoroutineScope(IO).async{ apiService.updateProjectTitle(teamId, projectId, hashMapOf(Pair("title",title)))}
            val response = request.await()
            when(response.code()){
                200->{
                    val localDao = AppDatabase.getProjectInstance(context)!!.ProjectDao()
                    localDao.updateTitle(projectId, title)
                }
                else -> showToast(R.string.toast_request_error)
            }
        }catch (e:Exception){
            showToast(R.string.toast_network_error)
        }
    }
    fun deleteProject(teamId: Int, projectId: Int, pageId:Int,  pos: Int, projectAdapter: ProjectAdapter?) = CoroutineScope(Default).launch{
        try{
            val request= CoroutineScope(IO).async{ apiService.deleteProject(teamId, projectId)}
            val response = request.await()

            when(response.code()){
                200->{
                    val localDao = AppDatabase.getProjectInstance(context)!!.ProjectDao()
                    val blockDao = AppDatabase.getBlockInstance(context)!!.BlockDao()
                    localDao.delete(projectId)
                    blockDao.deleteAll()

                    when(pageId){
                        R.id.menu_home, -1 -> backMainActivity(pageId)
                        else->withContext(Main){
                            projectAdapter!!.deleteProject(pos)
                        }
                    }
                    showToast(R.string.toast_delete_project)
                }
                else-> showToast(R.string.toast_request_error)
            }
        }catch(e:Exception){
            println(e)
            showToast(R.string.toast_network_error)
        }
    }

    // get training sentence request
    fun getSentence(progressBar: ProgressBar) = CoroutineScope(Default).launch {
        try {
            val request = CoroutineScope(IO).async { apiService.getSentence() }
            val response = request.await()
            when (response.code()) {
                200 -> {
                    val sentenceList = response.body()?.get("trainData")!! as Serializable
                    val intent = Intent(context, RecordActivity::class.java)
                    intent.putExtra("sentences", sentenceList)
                    context.startActivity(intent)

                    endLoading(progressBar)
                }
            }
        } catch (e: Exception) {
            showToast(R.string.toast_request_error)
            endLoading(progressBar)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setVoice(textId: Int, audioPath: String, binding: ActivityRecordBinding) =
        CoroutineScope(Default).launch {
            try {
                withContext(Main) {
                    binding.recordProgressBar.visibility = View.VISIBLE
                    binding.recordButton.alpha = 0.3F
                }
                val file = File(audioPath)
                val requestFile = file.asRequestBody("audio/wav".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("audio", file.name, requestFile)
                val request = CoroutineScope(IO).async { apiService.setVoice(textId, body) }
                val response = request.await()
                withContext(Main) {
                    binding.recordProgressBar.visibility = View.GONE
                    binding.recordButton.alpha = 1F
                }
                when (response.code()) {
                    200 -> {
//                    val accuracy = response.body()?.get("accuracy")
                        val accuracy = 0.9
                        if (accuracy != null) {
                            if (accuracy < 0.9) {
                                withContext(Main) {
                                    binding.recordWarning.run {
                                        visibility = View.VISIBLE
                                        text = "지정된 문장을 제대로 읽어주세요."
                                    }
                                }
                                file.delete()
                            } else {
                                withContext(Main) {
                                    binding.recordWarning.visibility = View.GONE
                                    binding.nextRecord.alpha = 1F
                                }
                            }
                        }

                    }
                    else -> {
                        println(response.code())
                        showToast(R.string.toast_request_error)
                    }

                }
            } catch (e: Exception) {
                showToast(R.string.toast_network_error)
                File(audioPath).delete()
            }
        }

    // get project list request
    fun getProject(binding: FragmentHomeBinding, fm: FragmentManager) =
        CoroutineScope(Default).launch {
            try {
                val request = CoroutineScope(IO).async {apiService.getProjectList(Glob.prefs.getCurrentTeam()) }
                val response = request.await()

                endLoading(binding.progressBar)
                when (response.code()) {
                    200 -> {
                        val projects = response.body()?.get("projects")!!
                        // connect with project adapter
                        withContext(Main) {
                            binding.networkErrorProject.visibility = View.GONE
                            binding.networkErrorTeam.visibility = View.GONE
                            if (projects.isEmpty()) {
                                binding.noProject.visibility = View.VISIBLE
                            } else {
                                binding.noProject.visibility = View.GONE
                            }
                            binding.projects.run {
                                adapter = TabAdapter(fm, projects)
                                binding.menu.setupWithViewPager(binding.projects)
                            }
                        }
                        // save project list in local DB
                        updateLocalDb("project", projects)
                    }
                }
            } catch (e: Exception) {

            }
        }

    // get dubbing voice list request
    fun getVoice() = CoroutineScope(Default).launch {
        try {
            val voiceRequest =
                CoroutineScope(IO).async { apiService.getVoice(Glob.prefs.getCurrentTeam()) }
            val voiceResponse = voiceRequest.await()
            when (voiceResponse.code()) {
                200 -> {
                    when (voiceResponse.body()?.isEmpty()) {
                        true -> {
                            // if no available dubbing voice
                            withContext(IO) { Glob.prefs.setInt("default_voice", 0) }
                        }
                        else -> {
                            updateLocalDb("voice", voiceResponse.body()!!)
                            // set default dubbing voice
                            withContext(IO) {
                                Glob.prefs.setInt(
                                    "default_voice",
                                    voiceResponse.body()!![0].id
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            //showToast(R.string.toast_request_error)
        }
    }

    // get workspace list request
    fun getTeam(binding: FragmentHomeBinding) = CoroutineScope(Default).launch {
        try {
            val request = CoroutineScope(IO).async { apiService.getTeamList() }
            var response = request.await()
            response = when (response.code()) {
                401 -> {
                    refreshToken()
                    request.await()
                }
                else-> response
            }
            when (response.code()) {
                200 -> {
                    // connect with team adapter
                    withContext(Main) {
                        binding.teams.run {
                            adapter = TeamAdapter(binding, response.body() as ArrayList<Team>)
                            addItemDecoration(HorizontalItemDecoration(12))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            //showToast(R.string.toast_request_error)
            endLoading(binding.progressBar)
            backLoginActivity()
        }
    }

    // create team workspace request
    fun createTeam(
        binding: BottomSheetTeamBinding,
        teamName: String,
        teamAdapter: TeamAdapter
    ) = CoroutineScope(Default).launch {
        try {
            val request = CoroutineScope(IO).async { apiService.createTeam(hashMapOf(Pair("name", teamName))) }
            val response = request.await()

            when (response.code()) {
                201 -> {
                    withContext(Main) {
                        binding.subTitle.text = "초대코드를 공유해주세요"
                        binding.btn.btnRect.text = "초대코드 복사하기"
                        binding.editText.visibility = View.GONE
                        binding.boldText.run {
                            text = response.body()?.teamCode // 초대코드
                            visibility = View.VISIBLE
                        }
                        // update team list
                        teamAdapter.addTeam(response.body()!!)
                    }
                }
                else -> showToast(R.string.toast_request_error)
            }
        } catch (e: Exception) {
            showToast(R.string.toast_network_error)
        }
    }

    fun joinTeam(
        binding: BottomSheetTeamBinding,
        code: String,
        teamAdapter: TeamAdapter
    ) = CoroutineScope(Default).launch {
        try {
            val request = CoroutineScope(IO).async { apiService.joinTeam(code) }
            val response = request.await()

            when (response.code()) {
                201 -> {
                    withContext(Main) {
                        binding.subTitle.text = "팀 스페이스에 참여되었습니다"
                        binding.btn.btnRect.text = "홈으로 이동하기"
                        binding.editText.visibility = View.GONE
                        binding.boldText.run {
                            visibility = View.VISIBLE
                            text = response.body()?.name // 팀 이름
                        }
                    }
                    // update team list
                    teamAdapter.addTeam(response.body()!!)
                }
                404 -> showToast(R.string.toast_wrong_code)
                409 -> showToast(R.string.toast_already_join)
                else -> showToast(R.string.toast_request_error)
            }
        } catch (e: Exception) {
            showToast(R.string.toast_network_error)
        }
    }

    // update project list at home page
    fun updateCurrentTeam(binding: FragmentHomeBinding) = CoroutineScope(Default).launch {
        try {
            var projectRequest = CoroutineScope(IO).async { apiService.getProjectList(Glob.prefs.getCurrentTeam()) }
            val voiceRequest = CoroutineScope(IO).async { apiService.getVoice(Glob.prefs.getCurrentTeam()) }

            CoroutineScope(Default).launch {
                val projectResponse = projectRequest.await()
                when (projectResponse.code()) {
                    200 -> {
                        val projects = projectResponse.body()?.get("projects")!!
                        withContext(Main) {
                            binding.networkErrorProject.visibility = View.GONE
                            binding.networkErrorTeam.visibility = View.GONE
                            (binding.projects.adapter as TabAdapter).updateProjectList(projects)
                            if (projects.isEmpty()) {
                                binding.noProject.visibility = View.VISIBLE
                            } else {
                                binding.noProject.visibility = View.GONE
                            }
                        }
                        updateLocalDb("project", projectResponse.body()?.get("projects")!!)
                    }
                    else -> {

                    }
                }
            }
            CoroutineScope(Default).launch {
                val voiceResponse = voiceRequest.await()
                when (voiceResponse.code()) {
                    200 -> {
                        when (voiceResponse.body()!!.isEmpty()) {
                            true -> {
                                // if no available dubbing voice
                                withContext(IO) { Glob.prefs.setInt("default_voice", 0) }
                            }
                            else -> {
                                updateLocalDb("voice", voiceResponse.body()!!)
                                withContext(IO) {
                                    Glob.prefs.setInt(
                                        "default_voice",
                                        voiceResponse.body()!![0].id
                                    )
                                }

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
        try {
            val request = CoroutineScope(IO).async {
                when (isChecked) {
                    true -> apiService.createBookmark(projectId)
                    else -> apiService.deleteBookmark(projectId)
                }
            }
            val response = request.await()
            when (response.code()) {
                200 -> withContext(IO) {
                    val localDb = AppDatabase.getProjectInstance(context)
                    localDb!!.ProjectDao().updateBookmark(projectId, isChecked)
                }
                else -> showToast(R.string.toast_request_error)
            }

        } catch (e: Exception) {
            showToast(R.string.toast_network_error)
        }
    }
    fun getBlock(teamId:Int, projectId:Int, progressBar: ProgressBar) = CoroutineScope(Default).launch{
        try{
            val request = CoroutineScope(IO).async{apiService.getBlock(teamId, projectId)}
            val response = request.await()
            endLoading(progressBar)
            when(response.code()){
                200->{
                    updateLocalDb("block", response.body()?.get("blocks")!!)
                    startProjectActivity(projectId)
                }
                else-> showToast(R.string.toast_request_error)
            }
        }catch (e: Exception) {
            endLoading(progressBar)
            showToast(R.string.toast_network_error)
        }
    }
    fun createBlock(teamId: Int, projectId: Int, order:Int, progressBar: ProgressBar, blockAdapter: BlockAdapter) = CoroutineScope(Default).launch{
        try{
            val request = CoroutineScope(IO).async{ apiService.createBlock(teamId, projectId, hashMapOf(Pair("order",order),Pair("voiceId",Glob.prefs.getInt("default_voice",0))))}
            val response = request.await()
            endLoading(progressBar)
            when(response.code()){
                201-> withContext(Main){
                        blockAdapter.addBlock(response.body()!!, order-1)
                    }
                else->{
                    println(response.code())
                    showToast(R.string.toast_request_error)
                }
            }
        }catch (e: Exception) {
            println(e)
            endLoading(progressBar)
            showToast(R.string.toast_network_error)
        }
    }
    fun updateBlock(project: Project, block:Block, progressBar: ProgressBar?, blockAdapter: BlockAdapter) = CoroutineScope(Default).launch{
        try{
            val body = ApiData.UpdateBlockRequest(block.id,block.text,block.audioPath,block.interval,block.voiceId,project.language,block.order)
            val request = CoroutineScope(IO).async{ apiService.updateBlock(project.team,project.id,block.id,body)}
            val response = request.await()
            if(progressBar != null) {
                endLoading(progressBar)
            }
            when(response.code()){
                200->{
                    withContext(Main) {
                        blockAdapter.updateBlock(block)
                    }
                }
                else-> {
                    println(response.code())
                    showToast(R.string.toast_request_error)
                }

            }
        }catch (e: Exception) {
            println(e)
            if(progressBar != null) {
                endLoading(progressBar)
            }
            showToast(R.string.toast_network_error)
        }
    }
    fun deleteBlock(teamId:Int, projectId:Int, blockId:Int, progressBar: ProgressBar, blockAdapter: BlockAdapter) = CoroutineScope(Default).launch{
        try{
            Log.i("REQUEST_ERROR",Glob.prefs.getString("token","error"))
            Log.i("REQUEST_ERROR",teamId.toString())
            Log.i("REQUEST_ERROR",projectId.toString())
            Log.i("REQUEST_ERROR",blockId.toString())
            val request = CoroutineScope(IO).async{ apiService.deleteBlock(teamId,projectId,blockId)}
            val response = request.await()
            endLoading(progressBar)
            when(response.code()){
                200->{
                    withContext(Main){
                        blockAdapter.deleteBlock(blockId)
                    }
                }
                else->{
                    Log.i("REQUEST_ERROR",response.code().toString())
                    showToast(R.string.toast_request_error)
                }
            }
        }catch (e: Exception) {
            println(e)
            endLoading(progressBar)
            showToast(R.string.toast_network_error)
        }
    }

    private suspend fun showToast(textId: Int) = withContext(Main) {
        Toast.makeText(context, context.resources.getString(textId), Toast.LENGTH_SHORT).show()
    }

    private fun updateLocalDb(type: String, data: List<Any>) = CoroutineScope(IO).launch {
        when (type) {
            // update project db
            "project" -> {
                val localDao = AppDatabase.getProjectInstance(context)!!.ProjectDao()
                CoroutineScope(IO).async {
                    localDao.deleteAll()
                    localDao.insert(data as List<Project>)
                }.onAwait
            }
            // update voice db
            "voice" -> {
                val localDao = AppDatabase.getVoiceInstance(context)!!.VoiceDao()
                CoroutineScope(IO).async {
                    localDao.deleteAll()
                    localDao.insert(data as List<Voice>)
                }.onAwait
            }
            // update block db
            else -> {
                val localDao = AppDatabase.getBlockInstance(context)!!.BlockDao()
                CoroutineScope(IO).async {
                    localDao.deleteAll()
                    localDao.insert(data as List<Block>)
                }.onAwait
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(context, BottomNavigationActivity::class.java)
        context.startActivity(intent)
        ActivityCompat.finishAffinity(context as LoginActivity)
    }

    private fun backMainActivity(pageId: Int){
        val intent = Intent(context, BottomNavigationActivity::class.java)
        context.startActivity(intent)
        when(pageId){
            R.id.menu_home->(context as BottomNavigationActivity).finish()
            else-> (context as CreateProjectActivity).finish()
        }
    }

    private fun startProjectActivity(projectId: Int) {
        val intent = Intent(context, CreateProjectActivity::class.java)
        intent.putExtra("project", projectId) // project id 넘기기
        context.startActivity(intent)
    }

    private fun startLoginActivity(){
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
        (context as SignupActivity).finish()
    }

    private fun backLoginActivity(){
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
        ActivityCompat.finishAffinity(context as BottomNavigationActivity)
        Glob.prefs.logout()
    }

    private suspend fun startLoading(progressBar: ProgressBar) = withContext(Main) {
        progressBar.visibility = View.VISIBLE
    }

    private suspend fun endLoading(progressBar: ProgressBar) = withContext(Main) {
        progressBar.visibility = View.GONE
    }
}