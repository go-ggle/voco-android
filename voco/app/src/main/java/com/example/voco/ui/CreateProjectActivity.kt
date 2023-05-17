package com.example.voco.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.voco.R
import com.example.voco.api.ApiRepository
import com.example.voco.data.adapter.BlockAdapter
import com.example.voco.data.adapter.VerticalItemDecoration
import com.example.voco.data.model.AppDatabase
import com.example.voco.data.model.Block
import com.example.voco.data.model.Project
import com.example.voco.databinding.ActivityCreateProjectBinding
import com.example.voco.service.MediaService
import com.example.voco.service.MediaService.initExoPlayer
import com.example.voco.service.MediaService.releaseExoPlayer
import com.example.voco.service.MediaService.setExoPlayerUrl
import com.google.android.exoplayer2.SimpleExoPlayer
import java.util.*
import kotlin.properties.Delegates

class CreateProjectActivity() : AppCompatActivity(), BlockAdapter.IntervalPicker, PopupMenu.OnMenuItemClickListener {
    private var projectId by Delegates.notNull<Int>()
    private lateinit var dubbingUrl : String
    private lateinit var binding: ActivityCreateProjectBinding
    private lateinit var localDb : AppDatabase
    private lateinit var blockAdapter: BlockAdapter
    private lateinit var project: Project
    private lateinit var blockList : ArrayList<Block>
    private lateinit var apiRepository: ApiRepository
    private var intervalBlock : Block? = null
    private var player: SimpleExoPlayer? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        localDb = AppDatabase.getProjectInstance(this)!!
        projectId = intent.getIntExtra("project",0)
        binding = ActivityCreateProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get the project and project's text blocks
        project = localDb.ProjectDao().selectById(projectId)
        blockList = localDb.BlockDao().selectAll() as ArrayList<Block>
        blockAdapter = BlockAdapter(project, blockList)
        apiRepository = ApiRepository(this)
        dubbingUrl = "https://voco-audio.s3.ap-northeast-2.amazonaws.com/${project.team}/${projectId}/0.wav"
        initExoPlayer(this, binding.audioPlayBox)
        setExoPlayerUrl(this, dubbingUrl)

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

        // interval picker
        binding.intervalPicker.run {
            minute.minValue=0
            minute.maxValue=99
            second.minValue =0
            second.maxValue=59

            cancelButton.setOnClickListener {
                closeIntervalPicker(false)
            }
            dialogButton.setOnClickListener {
                closeIntervalPicker(true)
            }
        }

        // back button
        binding.backButton.setOnClickListener {
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
    override fun openIntervalPicker(block: Block, minute:Int, second:Int){
        intervalBlock = block
        binding.intervalPicker.run {
            this.minute.value = minute
            this.second.value = second
            root.visibility = View.VISIBLE
        }
    }
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_download->{
                val regex = Regex("[^A-Za-z_]")
                MediaService.downloadAudio(this,
                    regex.replace(project.title.replace(" ","_"),"").lowercase(Locale.getDefault()),
                    dubbingUrl
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
            val intent = Intent(this, BottomNavigationActivity::class.java)
            startActivity(intent)
            finish()
            return true
        }
        return false
    }
    private fun closeIntervalPicker(isUpdate: Boolean) {
        if(isUpdate) {
            apiRepository.updateBlock(project, intervalBlock!!,null, binding.addprojectList.adapter as BlockAdapter)
        }
        binding.intervalPicker.root.visibility = View.GONE
        intervalBlock = null
    }

}