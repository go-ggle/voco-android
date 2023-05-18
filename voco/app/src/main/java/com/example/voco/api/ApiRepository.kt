package com.example.voco.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.EditText
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
import com.example.voco.login.SnsLogin
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
    fun emailLogin(user: ApiData.LoginRequest, currentActivity: Activity, isFinished: Boolean, progressBar: ProgressBar?) = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java) // retrofit client which doesn't have header
        try {
            val request = CoroutineScope(IO).async { apiService.login(user) }
            val response = request.await()

            if(progressBar != null)
                endLoading(progressBar)
            when (response.code()) {
                200 -> {
                    withContext(IO) {
                        Glob.prefs.run{
                            setString("token", response.body()?.get("accessToken")!!)
                            setInt("workspace", response.body()?.get("privateTeamId")!!.toInt())
                            setIdAndPwd(user)
                        }
                    }
                    if(isFinished)
                        startMainActivity(currentActivity)
                    //else backActivity(currentActivity)

                }
                404 -> showToast(R.string.toast_wrong_id_or_pwd)
                else -> showToast(R.string.toast_request_error)
            }
        } catch (e: Exception) {
            if(progressBar != null)
                endLoading(progressBar)
            println(e)
            showToast(R.string.toast_network_error)
        }
    }

    fun snsLogin(accessToken: String, type: Int, currentActivity: Activity, isFinished: Boolean) = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java)
        try {
            val request = CoroutineScope(IO).async { apiService.kakaoLogin(hashMapOf(Pair("accessToken", accessToken))) }
            val response = request.await()

            when (response.code()) {
                200 -> {
                    withContext(IO) {
                        Glob.prefs.run {
                            setString("token", response.body()?.get("accessToken")!!)
                            setInt("workspace", response.body()?.get("privateTeamId")!!.toInt())
                            setString("id", "sns_user")
                            setInt("sns", type)
                        }
                    }
                    if(isFinished)
                        startMainActivity(currentActivity)
                    //else backActivity(currentActivity)
                }
                else -> showToast(R.string.toast_request_error)
            }
        } catch (e: Exception) {
            showToast(R.string.toast_network_error)
        }
    }

    // refresh accessToken with refreshToken
    fun refreshToken(currentActivity: Activity, isFinished: Boolean) = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java)
        try {
            val refreshToken = Glob.prefs.getString("refresh_token", "logout")
            val request = CoroutineScope(IO).async { apiService.refreshToken(hashMapOf(Pair("refreshToken", refreshToken))) }
            val response = request.await()
            when (response.code()) {
                200 -> Glob.prefs.setToken(response.body()?.get("accessToken")!!)
                401 -> {
                    // if refresh token expired
                    when(Glob.prefs.loginMode()){
                        SNS.EMAIL.ordinal ->{
                            emailLogin(Glob.prefs.getIdAndPwd(), currentActivity, isFinished,null)
                        }
                        SNS.KAKAO.ordinal->{
                            val snsLogin = SnsLogin(context, currentActivity, isFinished)
                            snsLogin.kakao()
                        }
                        SNS.GOOGLE.ordinal->{
                            val snsLogin = SnsLogin(context, currentActivity, isFinished)
                            snsLogin.kakao()
                        }
                        SNS.NAVER.ordinal->{
                            val snsLogin = SnsLogin(context, currentActivity, isFinished)
                            snsLogin.kakao()
                        }
                    }
                }
                else ->{
                    backLoginActivity(currentActivity)
                }
            }
        } catch (e: Exception) {
            backLoginActivity(currentActivity)
            return@launch
        }
    }

    // create new project request
    fun createProject(title: String, language: Language, progressBar: ProgressBar) = CoroutineScope(Default).launch {
        try {
            val request = CoroutineScope(IO).async { apiService.createProject(
                Glob.prefs.getCurrentTeam(),
                ApiData.CreateProjectRequest(title, language.ordinal)
            ) }
            var response = request.await()
            response = when (response.code()) {
                401 -> {
                    refreshToken(context as BottomNavigationActivity, false)
                    request.await()
                }
                else-> response
            }
            endLoading(progressBar)
            when (response.code()) {
                201-> {
                    val localDao = AppDatabase.getProjectInstance(context)!!.ProjectDao()
                    val blockDao = AppDatabase.getBlockInstance(context)!!.BlockDao()
                    val newProject: Project = response.body()!!

                    blockDao.deleteAll()
                    localDao.insert(listOf(newProject)) // save new project
                    startProjectActivity(newProject.id) // move to create project page
                }
                else-> showToast(R.string.toast_request_error)
            }
        } catch (e: Exception) {
            println(e)
            endLoading(progressBar)
            showToast(R.string.toast_network_error)
        }
    }
    fun updateProjectTitle(teamId: Int, projectId: Int, title: String) = CoroutineScope(Default).launch{
        try{
            val request = CoroutineScope(IO).async{ apiService.updateProjectTitle(teamId, projectId, hashMapOf(Pair("title",title)))}
            var response = request.await()
            response = when (response.code()) {
                401 -> {
                    refreshToken(context as CreateProjectActivity, false)
                    request.await()
                }
                else-> response
            }

            when(response.code()){
                200->{
                    val localDao = AppDatabase.getProjectInstance(context)!!.ProjectDao()
                    localDao.updateTitle(projectId, title)
                }
                else -> showToast(R.string.toast_update_title_error)
            }
        }catch (e:Exception){
            println(e)
            showToast(R.string.toast_network_error)
        }
    }
    fun deleteProject(teamId: Int, projectId: Int, pageId:Int,  pos: Int, projectAdapter: ProjectAdapter?) = CoroutineScope(Default).launch{
        try{
            val request= CoroutineScope(IO).async{ apiService.deleteProject(teamId, projectId)}
            var response = request.await()
            response = when (response.code()) {
                401 -> {
                    when(pageId){
                        -1 -> refreshToken(context as CreateProjectActivity, false)
                        else -> refreshToken(context as BottomNavigationActivity, false)
                    }
                    request.await()
                }
                else-> response
            }

            when(response.code()){
                200->{
                    val localDao = AppDatabase.getProjectInstance(context)!!.ProjectDao()
                    val blockDao = AppDatabase.getBlockInstance(context)!!.BlockDao()
                    localDao.delete(projectId)
                    blockDao.deleteAll()

                    when(pageId){
                        -1 -> backMainActivity(pageId)
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
            var response = request.await()
            response = when (response.code()) {
                401 -> {
                    refreshToken(context as BottomNavigationActivity, false)
                    request.await()
                }
                else-> response
            }

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
                    binding.run {
                        recordProgressBar.visibility = View.VISIBLE
                        recordButton.alpha = 0.3F
                    }
                }
                val file = File(audioPath)
                val requestFile = file.asRequestBody("audio/wav".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("audio", file.name, requestFile)
                val request = CoroutineScope(IO).async { apiService.setVoice(textId, body) }
                val response = request.await()
                withContext(Main) {
                    binding.run{
                        recordProgressBar.visibility = View.GONE
                        recordButton.alpha = 1F
                    }
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
                                    binding.run{
                                        recordWarning.visibility = View.GONE
                                        nextRecord.alpha = 1F
                                    }
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
                var response = request.await()
                response = when (response.code()) {
                    401 -> {
                        refreshToken(context as BottomNavigationActivity, false)
                        request.await()
                    }
                    else-> response
                }

                endLoading(binding.progressBar)
                when (response.code()) {
                    200 -> {
                        val projects = response.body()?.get("projects")!!
                        // connect with project adapter
                        withContext(Main) {
                            binding.run {
                                networkErrorProject.visibility = View.GONE
                                networkErrorTeam.visibility = View.GONE
                                noProject.visibility = when(projects.isEmpty()){
                                    true -> View.VISIBLE
                                    else -> View.GONE
                                }
                            }
                            binding.projects.run {
                                adapter = TabAdapter(fm, projects, binding.progressBar)
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
            val request = CoroutineScope(IO).async { apiService.getVoice(Glob.prefs.getCurrentTeam()) }
            var response = request.await()
            response = when (response.code()) {
                401 -> {
                    refreshToken(context as BottomNavigationActivity, false)
                    request.await()
                }
                else-> response
            }

            when (response.code()) {
                200 -> {
                    when (response.body()?.isEmpty()) {
                        true -> {
                            // if no available dubbing voice
                            withContext(IO) { Glob.prefs.setInt("default_voice", 0) }
                        }
                        else -> {
                            updateLocalDb("voice", response.body()!!)
                            // set default dubbing voice
                            withContext(IO) {
                                Glob.prefs.setInt(
                                    "default_voice",
                                    response.body()!![0].id
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
                    refreshToken(context as BottomNavigationActivity, false)
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
            backLoginActivity(null)
        }
    }

    // create team workspace request
    fun createTeam(
        binding: BottomSheetTeamBinding,
        teamName: String,
        teamAdapter: TeamAdapter,
        progressBar: ProgressBar
    ) = CoroutineScope(Default).launch {
        try {
            val request = CoroutineScope(IO).async { apiService.createTeam(hashMapOf(Pair("name", teamName))) }
            var response = request.await()
            response = when (response.code()) {
                401 -> {
                    refreshToken(context as BottomNavigationActivity, true)
                    request.await()
                }
                else-> response
            }
            endLoading(progressBar)
            when (response.code()) {
                201 -> {
                    withContext(Main) {
                        binding.run{
                            subTitle.text = "초대코드를 공유해주세요"
                            btn.btnRect.text = "초대코드 복사하기"
                            editText.visibility = View.GONE
                            boldText.run {
                                text = response.body()?.teamCode // 초대코드
                                visibility = View.VISIBLE
                            }

                        }
                        teamAdapter.addTeam(response.body()!!) // update team list
                    }
                }
                else -> showToast(R.string.toast_request_error)
            }
        } catch (e: Exception) {
            endLoading(progressBar)
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
            var response = request.await()
            response = when (response.code()) {
                401 -> {
                    refreshToken(context as BottomNavigationActivity, true)
                    request.await()
                }
                else-> response
            }

            when (response.code()) {
                201 -> {
                    withContext(Main) {
                        binding.run{
                            subTitle.text = "팀 스페이스에 참여되었습니다"
                            btn.btnRect.text = "홈으로 이동하기"
                            editText.visibility = View.GONE
                            boldText.run {
                                visibility = View.VISIBLE
                                text = response.body()?.name // 팀 이름
                            }
                        }
                        teamAdapter.addTeam(response.body()!!) // update team list
                    }
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
            val voiceRequest = CoroutineScope(IO).async { apiService.getVoice(Glob.prefs.getCurrentTeam()) }
            var voiceResponse = voiceRequest.await()
            voiceResponse = when (voiceResponse.code()) {
                401 -> {
                    refreshToken(context as BottomNavigationActivity, false)
                    voiceRequest.await()
                }
                else-> voiceResponse
            }
            CoroutineScope(Default).launch {
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
            CoroutineScope(Default).launch {
                val projectRequest = CoroutineScope(IO).async { apiService.getProjectList(Glob.prefs.getCurrentTeam()) }
                val projectResponse = projectRequest.await()
                when (projectResponse.code()) {
                    200 -> {
                        val projects = projectResponse.body()?.get("projects")!!
                        withContext(Main) {
                            binding.run {
                                networkErrorProject.visibility = View.GONE
                                networkErrorTeam.visibility = View.GONE
                                noProject.visibility = when(projects.isEmpty()){
                                    true -> View.VISIBLE
                                    else -> View.GONE
                                }
                            }

                            (binding.projects.adapter as TabAdapter).updateProjectList(projects)
                        }
                        updateLocalDb("project", projectResponse.body()?.get("projects")!!)
                    }
                    else -> {

                    }
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
            var response = request.await()
            response = when (response.code()) {
                401 -> {
                    refreshToken(context as BottomNavigationActivity, false)
                    request.await()
                }
                else-> response
            }

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
            var response = request.await()
            response = when (response.code()) {
                401 -> {
                    refreshToken(context as BottomNavigationActivity, false)
                    request.await()
                }
                else-> response
            }

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
    fun getBlockVoice(textId: Int) = CoroutineScope(Default).launch {
        try{
            val request = CoroutineScope(IO).async { apiService.getBlockVoice(textId) }
            var response = request.await()
            response = when(response.code()){
                401 -> {
                    refreshToken(context as CreateProjectActivity, false)
                    request.await()
                }
                else-> response
            }
            when(response.code()){
                200->{

                }
                404->{


                }
                else->{

                }
            }
        }catch(e: Exception){

        }
    }
    fun createBlock(teamId: Int, projectId: Int, order:Int, progressBar: ProgressBar, blockAdapter: BlockAdapter) = CoroutineScope(Default).launch{
        try{
            val request = CoroutineScope(IO).async{ apiService.createBlock(teamId, projectId, hashMapOf(Pair("order",order),Pair("voiceId",Glob.prefs.getInt("default_voice",0))))}
            var response = request.await()
            response = when (response.code()) {
                401 -> {
                    refreshToken(context as CreateProjectActivity, false)
                    request.await()
                }
                else-> response
            }

            endLoading(progressBar)
            when(response.code()){
                201-> withContext(Main){ blockAdapter.addBlock(response.body()!!, order-1) }
                else-> {
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
    fun updateBlock(editBlock: EditText?, project: Project, block:Block, progressBar: ProgressBar?, blockAdapter: BlockAdapter) = CoroutineScope(Default).launch{
        try{
            val body = ApiData.UpdateBlockRequest(block.id,block.text,block.audioPath,block.interval,block.voiceId,project.language,block.order)
            val request = CoroutineScope(IO).async{ apiService.updateBlock(project.team,project.id,block.id,body)}
            var response = request.await()
            response = when (response.code()) {
                401 -> {
                    refreshToken(context as CreateProjectActivity, false)
                    request.await()
                }
                else-> response
            }
            withContext(Main) {
                if(editBlock != null)  editBlock.isFocusable = true
            }
            if(progressBar != null) {
                endLoading(progressBar)
            }
            when(response.code()){
                200-> withContext(Main) {
                    blockAdapter.updateBlock(response.body()!!)
                }
                else-> {
                    println(response.code())
                    showToast(R.string.toast_request_error)
                }
            }
        }catch (e: Exception) {
            if(progressBar != null) {
                endLoading(progressBar)
            }
            withContext(Main) {
                if(editBlock != null)  editBlock.isFocusable = true
            }
            showToast(R.string.toast_network_error)
        }
    }
    fun deleteBlock(teamId:Int, projectId:Int, blockId:Int, progressBar: ProgressBar, blockAdapter: BlockAdapter) = CoroutineScope(Default).launch{
        try{
            val request = CoroutineScope(IO).async{ apiService.deleteBlock(teamId,projectId,blockId)}
            var response = request.await()
            response = when (response.code()) {
                401 -> {
                    refreshToken(context as CreateProjectActivity, false)
                    request.await()
                }
                else-> response
            }

            endLoading(progressBar)
            when(response.code()){
                200->withContext(Main){ blockAdapter.deleteBlock(blockId) }
                else->{
                    println(response.code())
                    showToast(R.string.toast_request_error)
                }
            }
        }catch (e: Exception) {
            endLoading(progressBar)
            showToast(R.string.toast_network_error)
        }
    }

    private fun updateLocalDb(type: String, data: List<Any>) = CoroutineScope(IO).launch {
        when (type) {
            // update project db
            "project" -> {
                val localDao = AppDatabase.getProjectInstance(context)!!.ProjectDao()
                CoroutineScope(IO).async {
                    localDao.deleteAll()
                    localDao.insert(data as List<Project>)
                }
            }
            // update voice db
            "voice" -> {
                val localDao = AppDatabase.getVoiceInstance(context)!!.VoiceDao()
                CoroutineScope(IO).async {
                    localDao.deleteAll()
                    localDao.insert(data as List<Voice>)
                }
                println(localDao.selectAll())
            }
            // update block db
            else -> {
                val localDao = AppDatabase.getBlockInstance(context)!!.BlockDao()
                CoroutineScope(IO).async {
                    localDao.deleteAll()
                    localDao.insert(data as List<Block>)
                }
            }
        }
    }
    private fun startMainActivity(currentActivity: Activity) {
        val intent = Intent(context, BottomNavigationActivity::class.java)
        context.startActivity(intent)
        currentActivity.finish()
    }
    private fun backMainActivity(pageId: Int){
        val intent = Intent(context, BottomNavigationActivity::class.java)
        context.startActivity(intent)
        when(pageId){
            R.id.menu_home->(context as BottomNavigationActivity).finish()
            else-> (context as CreateProjectActivity).finish()
        }
    }
    private fun backActivity(activity: Activity){
        val intent = Intent(context, BottomNavigationActivity::class.java)
        context.startActivity(intent)
        activity.finish()
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
    private fun backLoginActivity(currentActivity: Activity?){
        val intent = Intent(context, LoginActivity::class.java)
        context.startActivity(intent)
        if(currentActivity != null)
            currentActivity.finish()
        else
            ActivityCompat.finishAffinity(context as BottomNavigationActivity)
        Glob.prefs.logout()
    }
    private suspend fun showToast(textId: Int) = withContext(Main) {
        Toast.makeText(context, context.resources.getString(textId), Toast.LENGTH_SHORT).show()
    }
    private suspend fun endLoading(progressBar: ProgressBar) = withContext(Main) {
        progressBar.visibility = View.GONE
    }
}