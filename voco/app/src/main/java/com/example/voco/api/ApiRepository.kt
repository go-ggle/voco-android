package com.example.voco.api

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import com.example.voco.R
import com.example.voco.data.adapter.HorizontalItemDecoration
import com.example.voco.data.adapter.TabAdapter
import com.example.voco.data.adapter.TeamAdapter
import com.example.voco.data.model.*
import com.example.voco.databinding.ActivityRecordBinding
import com.example.voco.databinding.BottomSheetTeamBinding
import com.example.voco.databinding.FragmentHomeBinding
import com.example.voco.databinding.FragmentMypageBinding
import com.example.voco.login.Glob
import com.example.voco.ui.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.*
import java.util.*

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
                    showToast(R.string.toast_signup_success)
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as SignupActivity).finish()
                }
                409 -> showToast(R.string.toast_already_signup)
                else ->showToast(R.string.toast_request_error)
            }
        }catch (e: Exception){
            showToast(R.string.toast_network_error)
        }
    }
    // login request coroutine
    suspend fun emailLogin(user: ApiData.LoginRequest) = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java)
        try{
            val request = CoroutineScope(IO).async { apiService.login(user) }
            val response = request.await()
            when(response.code()) {
                200 -> {
                    withContext(IO) {
                        Glob.prefs.setString("token", response.body()?.get("accessToken")!!)
                        Glob.prefs.setInt("workspace",response.body()?.get("privateTeamId")!!.toInt())
                        Glob.prefs.setIdAndPwd(user)
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
    fun snsLogin(accessToken:String, type:String) = CoroutineScope(Default).launch {
        apiService = RetrofitClient.getRetrofitClient(false).create(Api::class.java)
        try{
            val request = CoroutineScope(IO).async { apiService.kakaoLogin(hashMapOf(Pair("accessToken",accessToken))) }
            val response = request.await()

            when(response.code()) {
                200 -> {
                    withContext(IO) {
                        Glob.prefs.setString("token", response.body()?.get("accessToken")!!)
                        Glob.prefs.setInt("workspace",response.body()?.get("privateTeamId")!!.toInt())
                        Glob.prefs.setString("id", "sns_user")
                        Glob.prefs.setString("sns", type)
                    }
                    goToMainPage()
                }
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
            val request = CoroutineScope(IO).async { when(Glob.prefs.loginMode()){
                "kakao" -> {
                    val refreshToken = Glob.prefs.getString("refresh_token","logout")
                    apiService.kakaoLogin(hashMapOf(Pair("accessToken",refreshToken)))
                }
                else -> apiService.login(Glob.prefs.getIdAndPwd())
            }}
            val response = request.await()
            when(response.code()) {
                200 -> withContext(IO){ Glob.prefs.setToken(response.body()?.get("accessToken")!!) }
            }
        }catch(e: Exception){
            return@launch
        }
    }
    // create new project request
    fun createProject(title:String, language: Int) = CoroutineScope(Default).launch {
        try{
            val request = CoroutineScope(IO).async { apiService.createProject(Glob.prefs.getCurrentTeam(),
                ApiData.CreateProjectRequest(title, language)
            ) }
            val response = request.await()
            when(response.code()){
                200 -> {
                    val localDao = AppDatabase.getProjectInstance(context)!!.ProjectDao()
                    val blockLocalDao = AppDatabase.getBlockInstance(context)!!.BlockDao()
                    val initialBlock = Block(1,"",Glob.prefs.getInt("default_voice",0),"",0,0.01)
                    val newProject : Project = response.body()!!
                    val newProjectId = response.body()!!.id

                    localDao.insert(listOf(newProject)) // save new project
                    blockLocalDao.insert(listOf(initialBlock)) // save initial block
                    goToBlockPage(newProjectId) // move to create project page
                }
            }
        }catch (e:Exception){
            showToast(R.string.toast_network_error)
        }
    }
    // get training sentence request
    fun getSentence(binding: FragmentMypageBinding) = CoroutineScope(Default).launch {
        try{
            val request = CoroutineScope(IO).async{ apiService.getSentence()}
            val response = request.await()
            when (response.code()) {
                200 ->{
                    val sentenceList = response.body()?.get("trainData")!! as Serializable
                    val intent = Intent(context, RecordActivity::class.java)
                    intent.putExtra("sentences",sentenceList)
                    context.startActivity(intent)

                    withContext(Main){binding.progressBar.visibility = View.GONE}
                }
            }
        }catch (e:Exception){
            println(e)
            showToast(R.string.toast_request_error)
            withContext(Main){
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    public fun InputStream.readBytes(): ByteArray {
        val buffer = ByteArrayOutputStream(maxOf(DEFAULT_BUFFER_SIZE, this.available()))
        copyTo(buffer)
        return buffer.toByteArray()
    }


    public fun InputStream.copyTo(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
        var bytesCopied: Long = 0
        val buffer = ByteArray(bufferSize)
        var bytes = read(buffer)
        while (bytes >= 0) {
            out.write(buffer, 0, bytes)
            bytesCopied += bytes
            bytes = read(buffer)
        }
        return bytesCopied
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun setVoice(textId: Int, audioPath: String, binding: ActivityRecordBinding) = CoroutineScope(Default).launch{
        try{
            withContext(Main){
                binding.recordProgressBar.visibility = View.VISIBLE
                binding.recordButton.alpha = 0.3F
            }
            val file = File(audioPath)
            val requestFile = file.asRequestBody("audio/wav".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("audio", file.name, requestFile)
            val request = CoroutineScope(IO).async { apiService.setVoice(textId, body) }
            val response = request.await()
            withContext(Main){
                binding.recordProgressBar.visibility = View.GONE
                binding.recordButton.alpha = 1F
            }
            when(response.code()){
                200 ->{
//                    val accuracy = response.body()?.get("accuracy")
                    val accuracy = 0.9
                    if (accuracy != null) {
                        if(accuracy < 0.9){
                            withContext(Main) {
                                binding.recordWarning.run {
                                    visibility = View.VISIBLE
                                    text = "지정된 문장을 제대로 읽어주세요."
                                }
                            }
                            file.delete()
                        }
                        else{
                            withContext(Main) {
                                binding.recordWarning.visibility = View.GONE
                                binding.nextRecord.alpha = 1F
                            }
                        }
                    }

                }
                else->{
                    showToast(R.string.toast_request_error)
                }

            }
        }catch (e:Exception){
            println(e)
            showToast(R.string.toast_network_error)
            File(audioPath).delete()
        }
    }
    // get project list request
    fun getProject(binding: FragmentHomeBinding, fm: FragmentManager) = CoroutineScope(Default).launch {
        try{
            val projectRequest = CoroutineScope(IO).async { apiService.getProjectList(Glob.prefs.getCurrentTeam())}
            val projectResponse : Response<HashMap<String, List<Project>>>
            val response = projectRequest.await()
            // if login session is finished
            projectResponse = when (response.code()){
                401 ->{
                    refreshToken() // refresh token
                    projectRequest.await() // request again
                }
                else-> response
            }
            withContext(Main){
                binding.progressBar.visibility = View.GONE
            }
            when(projectResponse.code()) {
                200 ->{
                    val projects =  projectResponse.body()?.get("projects")!!
                    // connect with project adapter
                    withContext(Main){
                        binding.networkErrorProject.visibility = View.GONE
                        binding.networkErrorTeam.visibility = View.GONE
                        if(projects.isEmpty()){
                            binding.noProject.visibility = View.VISIBLE
                        }else{
                            binding.noProject.visibility = View.GONE
                        }
                        binding.projects.run{
                            adapter = TabAdapter(fm,projects)
                            binding.menu.setupWithViewPager(binding.projects)
                        }
                    }
                    // save project list in local DB
                    updateLocalDb("project",projects)
                }
            }
        }catch(e:Exception){
            withContext(Main){
                binding.progressBar.visibility = View.GONE
                binding.networkErrorProject.visibility = View.VISIBLE
                binding.networkErrorTeam.visibility = View.VISIBLE
            }
            showToast(R.string.toast_request_error)
        }
    }
    // get dubbing voice list request
    fun getVoice() = CoroutineScope(Default).launch {
        try{
            val voiceRequest = CoroutineScope(IO).async{ apiService.getVoice(Glob.prefs.getCurrentTeam())}
            val voiceResponse = voiceRequest.await()
            when (voiceResponse.code()) {
                200 ->{
                    when (voiceResponse.body()?.isEmpty()) {
                        true -> {
                            // if no available dubbing voice
                            withContext(IO) { Glob.prefs.setInt("default_voice", 0) }
                        }
                        else -> {
                            updateLocalDb("voice", voiceResponse.body()!!)
                            // set default dubbing voice
                            withContext(IO){Glob.prefs.setInt("default_voice", voiceResponse.body()!![0].id)}
                        }
                    }
                }
            }
        }catch (e:Exception){
            //showToast(R.string.toast_request_error)
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
                            adapter = TeamAdapter(binding, teamResponse.body() as ArrayList<Team>)
                            addItemDecoration(HorizontalItemDecoration(12))
                        }
                    }
                }
            }
        }catch (e:Exception){
            //showToast(R.string.toast_request_error)
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
            val projectRequest = CoroutineScope(IO).async { apiService.getProjectList(Glob.prefs.getCurrentTeam()) }
            val voiceRequest = CoroutineScope(IO).async{ apiService.getVoice(Glob.prefs.getCurrentTeam())}
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
                        val projects =  projectResponse.body()?.get("projects")!!
                        withContext(Main){
                            binding.networkErrorProject.visibility = View.GONE
                            binding.networkErrorTeam.visibility = View.GONE
                            (binding.projects.adapter as TabAdapter).updateProjectList(projects)
                            if(projects.isEmpty()){
                                binding.noProject.visibility = View.VISIBLE
                            }else{
                                binding.noProject.visibility = View.GONE
                            }
                        }
                        updateLocalDb("project",projectResponse.body()?.get("projects")!!)
                    }
                    else -> {
                        showToast(R.string.toast_request_error)
                        binding.networkErrorProject.visibility = View.VISIBLE
                        binding.networkErrorTeam.visibility = View.VISIBLE
                    }
                }
            }
            CoroutineScope(Default).launch {
                val voiceResponse = voiceRequest.await()
                when(voiceResponse.code()){
                    200 -> {
                        when(voiceResponse.body()!!.isEmpty()){
                            true -> {
                                // if no available dubbing voice
                                withContext(IO){ Glob.prefs.setInt("default_voice", 0) }
                            }
                            else ->{
                                updateLocalDb("voice",voiceResponse.body()!!)
                                withContext(IO) { Glob.prefs.setInt("default_voice", voiceResponse.body()!![0].id) }
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
    private suspend fun showToast(textId: Int) = withContext(Main){
        Toast.makeText(context, context.resources.getString(textId), Toast.LENGTH_SHORT).show()
    }
    private fun updateLocalDb(type:String, data:List<Any>)= CoroutineScope(IO).launch{
        when(type){
            // update project db
            "project" -> {
                val localDao = AppDatabase.getProjectInstance(context)!!.ProjectDao()
                CoroutineScope(IO).async {
                    localDao.deleteAll()
                    localDao.insert(data as List<Project>) }.onAwait
            }
            // update voice db
            "voice" -> {
                val localDao = AppDatabase.getVoiceInstance(context)!!.VoiceDao()
                CoroutineScope(IO).async {
                    localDao.deleteAll()
                    localDao.insert(data as List<Voice>) }.onAwait
            }
            // update block db
            else -> {
                val localDao = AppDatabase.getBlockInstance(context)!!.BlockDao()
                CoroutineScope(IO).async {
                    localDao.deleteAll()
                    localDao.insert(data as List<Block>) }.onAwait
            }
        }
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
}