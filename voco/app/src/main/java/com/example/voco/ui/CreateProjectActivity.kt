package com.example.voco.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.amazonaws.regions.Regions
import com.example.voco.R
import com.example.voco.api.ApiRepository
import com.example.voco.data.adapter.BlockAdapter
import com.example.voco.data.adapter.VerticalItemDecoration
import com.example.voco.data.model.AppDatabase
import com.example.voco.data.model.Block
import com.example.voco.data.model.Project
import com.example.voco.databinding.ActivityCreateProjectBinding
import com.example.voco.service.MediaService
import com.example.voco.service.MediaService.releaseExoPlayer
import com.example.voco.service.MediaService.setExoPlayerUrl
import com.google.android.exoplayer2.SimpleExoPlayer
import java.util.*
import kotlin.properties.Delegates
import kotlin.system.exitProcess

class CreateProjectActivity() : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {
    private var projectId by Delegates.notNull<Int>()
    private lateinit var dubbingUrl : String
    private lateinit var binding: ActivityCreateProjectBinding
    private lateinit var localDb : AppDatabase
    private lateinit var blockDb : AppDatabase
    private lateinit var voiceDb : AppDatabase
    private lateinit var blockAdapter: BlockAdapter
    private lateinit var project: Project
    private lateinit var blockList : ArrayList<Block>
    private lateinit var apiRepository: ApiRepository
    private var player: SimpleExoPlayer? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        localDb = AppDatabase.getProjectInstance(this)!!
        blockDb = AppDatabase.getBlockInstance(this)!!
        voiceDb = AppDatabase.getVoiceInstance(this)!!
        projectId = intent.getIntExtra("project",0)
        binding = ActivityCreateProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get the project and project's text blocks
        project = localDb.ProjectDao().selectById(projectId)
        blockList = blockDb.BlockDao().selectAll() as ArrayList<Block>
        blockAdapter = BlockAdapter(project, blockList, voiceDb.VoiceDao().selectAll(), binding.audioPlayBox)
        apiRepository = ApiRepository(this)

        if(blockAdapter.itemCount==0){
            binding.noProject.visibility = View.VISIBLE
        }

        getPermission()

        dubbingUrl = "https://voco-audio.s3.ap-northeast-2.amazonaws.com/${project.team}/${projectId}/0.wav"
        //initExoPlayer(this, binding.audioPlayBox)
        setExoPlayerUrl(this, binding.audioPlayBox, dubbingUrl) // prepare dubbing audio source

        val window = window
        window.setBackgroundDrawableResource(R.color.light_purple)
        // set project's title and date
        binding.date.text = project.updatedAt
        binding.title.setText(project.title)

        // text block recycler view
        binding.addprojectList.run {
            adapter = blockAdapter
            addItemDecoration(VerticalItemDecoration(20))
        }

        // back button
        binding.backButton.setOnClickListener {
            player?.stop()
            player?.release()
            player = null
            val intent = Intent(this, BottomNavigationActivity::class.java)
            startActivity(intent)
            finish()
        }

        // add new text block at last index
        binding.addprojectAddButton.setOnClickListener {
            if(binding.progressBar.visibility == View.GONE) {
                val position = binding.addprojectList.childCount
                if (position - 1 >= 0 && (blockList[position - 1].text == "" || position - 1 < 0 && blockList[0].text == "")) {
                    Toast.makeText(this, "내용을 작성해주세요", Toast.LENGTH_SHORT).show()
                } else {
                    binding.progressBar.visibility = View.VISIBLE
                    apiRepository.createBlock(project.team, project.id, position + 1, binding.progressBar, binding.addprojectList.adapter as BlockAdapter)
                }
            }else{
                Toast.makeText(this, "블럭 생성중입니다",Toast.LENGTH_SHORT).show()
            }
        }
        binding.title.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                apiRepository.updateProjectTitle(project.team, projectId, s.toString())
            }

        })
        binding.projectPageMenu.setOnClickListener {
            // show pop up menu
            val themeWrapper = ContextThemeWrapper(this, R.style.PopupMenuTheme)
            val popup = PopupMenu(themeWrapper, it)
            popup.menuInflater.inflate(R.menu.menu_project, popup.menu)
            popup.setOnMenuItemClickListener(this)
            popup.gravity = Gravity.END
            popup.setForceShowIcon(true)
            popup.show()
        }

        // player with ExoPlayer
//        binding.audioPlayBox.setOnClickListener {
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseExoPlayer()
    }
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        binding.addprojectList.clearFocus()
        return super.dispatchTouchEvent(ev)
    }
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_download->{

                MediaService.downloadAudio(LayoutInflater.from(this).inflate(R.layout.activity_create_project, null),
                    "ap-northeast-2:3fb11ae4-58dc-46ba-be51-7aeb9b20f0c2",
                    Regions.AP_NORTHEAST_2,
                    "voco-audio",
                    project,
                    null,
                    "${project.team}/${projectId}/0.wav"
                )
            }
            R.id.menu_delete->{
                binding.progressBar.visibility = View.VISIBLE
                apiRepository.deleteProject(project.team, projectId,-1,-1,null)
            }
            R.id.menu_update_language->{

            }
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            player?.stop()
            player?.release()
            player = null
            val intent = Intent(this, BottomNavigationActivity::class.java)
            startActivity(intent)
            finish()
            return true
        }
        return false
    }
    private fun getPermission(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ||ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            val permissions = arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
            )
            ActivityCompat.requestPermissions(this, permissions, 100)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty()) {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED)
                        exitProcess(0)
                }
            }
        }
    }

}