package com.example.voco.data.adapter

import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.net.Uri
import android.os.Build
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.regions.Regions
import com.example.voco.R
import com.example.voco.api.ApiRepository
import com.example.voco.data.model.Block
import com.example.voco.data.model.Project
import com.example.voco.data.model.Voice
import com.example.voco.databinding.ActivityCreateProjectBinding
import com.example.voco.databinding.FragmentBlockBinding
import com.example.voco.dialog.IntervalDialog
import com.example.voco.service.MediaService
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import okhttp3.internal.userAgent


class BlockAdapter (val project: Project, var blocks : ArrayList<Block>, val voices : List<Voice>, private val playView: PlayerControlView) : RecyclerView.Adapter<BlockAdapter.ViewHolder>() {
    private lateinit var binding: FragmentBlockBinding
    private lateinit var parentBinding: ActivityCreateProjectBinding
    private lateinit var apiRepository : ApiRepository
    private lateinit var themeWrapper : ContextThemeWrapper
    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var clipBoard: ClipboardManager
    private lateinit var keyboard : InputMethodManager
    private lateinit var dlg : IntervalDialog
    companion object{
        var player: SimpleExoPlayer? = null // make player nullable
    }

    override fun getItemCount(): Int = blocks.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        binding = FragmentBlockBinding.inflate(inflater, parent, false)
        parentBinding = ActivityCreateProjectBinding.inflate(inflater)
        apiRepository = ApiRepository(parent.context)
        themeWrapper = ContextThemeWrapper(parent.context, R.style.PopupMenuTheme)
        trackSelector = DefaultTrackSelector(parent.context)
        clipBoard = parent.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        keyboard = parent.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        dlg = IntervalDialog(parent.context)
        blocks.sortBy { it.order } // id가 아닌 order 순으로 정렬

        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(blocks[position])
    }

    inner class ViewHolder(private val binding: FragmentBlockBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.M)
        fun bind(block: Block) {
            keyboard.showSoftInput(binding.projectEditText, 0)
            binding.projectEditText.run {
                setText(block.text)
                setOnFocusChangeListener { v, hasFocus ->
                    when (hasFocus) {
                        true -> {
                            if(binding.progressBar.visibility == View.GONE){
                                keyboard.showSoftInput(v, 0)
                            }
                            else{
                                clearFocus()
                            }
                        } // keyboard up
                        false -> {
                            keyboard.hideSoftInputFromWindow(binding.root.windowToken, 0) // keyboard down
                            stopStreamBlock(binding.projectPlayButton)

                            val prevText = block.text
                            val updatedText = binding.projectEditText.text.trim().toString()
                            block.text = updatedText
                            if(block.text != "" && prevText != updatedText) {
                                binding.progressBar.visibility = View.VISIBLE
                                // create block's dubbing request
                                apiRepository.updateBlock(project, block, binding.progressBar,this@BlockAdapter)
                            }

                        }
                    }
                }
                setOnLongClickListener {
                    if(binding.progressBar.visibility != View.VISIBLE){
                        if (clipBoard.hasPrimaryClip()){
                            showContextMenu()
                            if(clipBoard.primaryClipDescription!!.hasMimeType(MIMETYPE_TEXT_PLAIN)){
                                setText(block.text+clipBoard.text) // paste text
                            }
                        }
                        setSelection(binding.projectEditText.length())
                    }
                    true
                }
            }
            binding.projectDownloadButton.setOnClickListener{
                if(binding.progressBar.visibility == View.VISIBLE){
                    Toast.makeText(it.context, R.string.toast_please_wait_block, Toast.LENGTH_SHORT).show()
                }
                else if(block.audioPath==""){
                    Toast.makeText(it.context, R.string.toast_please_create_dubbing, Toast.LENGTH_SHORT).show()
                }
                else if(it.alpha == 1F){
                    it.alpha = 0.3F
                    MediaService.downloadAudio(
                        it,
                        "ap-northeast-2:3fb11ae4-58dc-46ba-be51-7aeb9b20f0c2",
                        Regions.AP_NORTHEAST_2,
                        "voco-audio",
                        project,
                        block,
                        "${project.team}/${project.id}/${block.id}.wav"
                    )
                }
            }
            binding.projectIntervalButton.run {
                val intervalMinute = block.interval/60
                val intervalSecond = block.interval%60

                text = when (intervalMinute) {
                    0 -> "인터벌 ${intervalSecond}초"
                    else -> "인터벌 ${intervalMinute}분 ${intervalSecond}초"
                }
                // choose interval
                setOnClickListener {
                    if(binding.progressBar.visibility == View.VISIBLE){
                        Toast.makeText(it.context, R.string.toast_please_wait_block, Toast.LENGTH_SHORT).show()
                    }
                    else{
                        dlg.show(project, block, binding.progressBar, this@BlockAdapter, intervalMinute, intervalSecond)
                    }
                }
            }
            binding.voiceName.text = voices.find{it.id == block.voiceId}!!.nickname
            binding.popupNickname.setOnClickListener {
                if(binding.progressBar.visibility == View.GONE){
                    // choose voice pop up menu
                    val popup = PopupMenu(themeWrapper, it)
                    voices.forEachIndexed { index, voice ->
                        // add pop up menu item
                        popup.menu.add(0, voice.id, index, voice.nickname)
                    }
                    popup.run{
                        gravity = Gravity.END
                        setOnMenuItemClickListener { item ->
                            block.voiceId = item.itemId
                            binding.voiceName.text = item.title
                            binding.progressBar.visibility = View.VISIBLE
                            apiRepository.updateBlock(project, block, binding.progressBar, this@BlockAdapter)

                            false
                        }
                        show()
                    }
                }else{
                    Toast.makeText(it.context, R.string.toast_please_wait_block, Toast.LENGTH_SHORT).show()
                }
            }
            binding.projectAddButton.setOnClickListener {
                when(parentBinding.progressBar.visibility){
                    View.VISIBLE -> Toast.makeText(it.context, R.string.toast_please_wait_block, Toast.LENGTH_SHORT).show()
                    else -> {
                        if (adapterPosition >= 0 && blocks[adapterPosition].text == "" || adapterPosition < 0 && blocks[0].text == ""
                            || (blocks.size > adapterPosition+1 && blocks[adapterPosition+1].text=="") ){
                            showToast(this.itemView.context, "내용을 작성해주세요")
                        }
                        else {
                            parentBinding.progressBar.visibility = View.VISIBLE
                            // add block button
                            apiRepository.createBlock(
                                project.team,
                                project.id,
                                adapterPosition + 2,
                                parentBinding.progressBar,
                                this@BlockAdapter,
                                null
                            )
                        }
                    }
                }
            }
            // delete block button
            binding.projectDeleteButton.setOnClickListener {
                if(blocks.size==1){
                    // cannot delete first block
                    showToast(this.itemView.context, "첫번째 텍스트입니다")
                }
                else {
                    if(binding.progressBar.visibility == View.VISIBLE){
                        Toast.makeText(it.context, R.string.toast_please_wait_block, Toast.LENGTH_SHORT).show()
                    }
                    else {
                        binding.progressBar.visibility = View.VISIBLE
                        apiRepository.deleteBlock(
                            project.team,
                            project.id,
                            block.id,
                            binding.progressBar,
                            this@BlockAdapter
                        )
                    }
                }
            }
            // play block button
            binding.projectPlayButton.run{
                setOnClickListener {
                    if(binding.progressBar.visibility == View.VISIBLE){
                        Toast.makeText(it.context, R.string.toast_please_wait_block, Toast.LENGTH_SHORT).show()
                    }
                    else if(block.audioPath == ""){
                        Toast.makeText(it.context, R.string.toast_please_create_dubbing, Toast.LENGTH_SHORT).show()
                    }
                    else if(player==null){
                        // change to stop button
                        this.setImageResource(R.drawable.ic_substop)
                        // start streaming
                        streamBlock(it.context, block.audioPath, binding.projectPlayButton)
                    }
                    else{
                        // stop streaming
                        stopStreamBlock(binding.projectPlayButton)
                        this.setImageResource(R.drawable.ic_subplay)
                    }
                }
            }
        }
    }
    // stream block dubbing audio
    fun streamBlock(context: Context, mediaUrl: String, playButton: ImageButton){
        stopStreamBlock(null)
        val dubbingUrl = "https://voco-audio.s3.ap-northeast-2.amazonaws.com/${project.team}/${project.id}/0.wav"
        if(playView.player!= null && playView.player!!.isPlaying)
            MediaService.setExoPlayerUrl(playView.context, playView, dubbingUrl)

        val dataSourceFactory = DefaultDataSourceFactory(context, userAgent)
        val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
        val mediaSource = mediaSourceFactory.createMediaSource(Uri.parse(mediaUrl)) // create media source

        player = SimpleExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()
        player!!.run{
            prepare(mediaSource, true, false)
            playWhenReady = true // play the media source
            addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    if(!isPlaying){
                        // change to play button
                        stopStreamBlock(playButton)
                    }
                }
            })
        }
    }
    fun stopStreamBlock(playButton: ImageButton?){
        playButton?.setImageResource(R.drawable.ic_subplay)
        player?.stop(true)
        player?.release()
        player = null
    }
    // add block
    fun addBlock(block: Block, pos: Int, introView: View?){
        blocks.run{
            add(pos, block)
            notifyItemInserted(pos)
            if(pos+1 != blocks.size) {
                forEachIndexed { index, block ->
                    block.order = index + 1
                }
                notifyItemRangeChanged(pos + 1, blocks.size - pos - 1)
            }
        }
        if(pos==0){
            introView!!.visibility = View.GONE
        }
    }
    // delete block
    fun deleteBlock(blockId: Int){
        val pos = blocks.indexOf(blocks.find{it.id == blockId})
        blocks.run{
            removeAt(pos)
            notifyItemRemoved(pos)
            forEachIndexed { index, block ->
                block.order = index + 1
            }
            notifyItemRangeChanged(pos, blocks.size-pos)
        }
        val dubbingUrl = "https://voco-audio.s3.ap-northeast-2.amazonaws.com/${project.team}/${project.id}/0.wav"
        MediaService.setExoPlayerUrl(playView.context, playView, dubbingUrl)
    }
    // update block's interval
    fun updateBlock(block:Block){
        val pos = blocks.indexOf(blocks.find{it.id == block.id})
        blocks[pos].run{
            text = block.text
            interval = block.interval
            voiceId = block.voiceId
        }
        if(blocks[pos].audioPath == "") {
            blocks[pos].audioPath = block.audioPath
        }
        val dubbingUrl = "https://voco-audio.s3.ap-northeast-2.amazonaws.com/${project.team}/${project.id}/0.wav"
        MediaService.setExoPlayerUrl(playView.context, playView, dubbingUrl)
        notifyItemChanged(pos)
    }
    fun showToast(context: Context, string: String){
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }
}

