package com.example.voco.data.adapter

import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.net.Uri
import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.R
import com.example.voco.api.ApiRepository
import com.example.voco.data.model.Block
import com.example.voco.data.model.Project
import com.example.voco.databinding.ActivityCreateProjectBinding
import com.example.voco.databinding.FragmentBlockBinding
import com.example.voco.dialog.IntervalDialog
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import okhttp3.internal.userAgent


class BlockAdapter (val project: Project, var blocks : ArrayList<Block>) : RecyclerView.Adapter<BlockAdapter.ViewHolder>() {
    private lateinit var binding: FragmentBlockBinding
    private lateinit var parentBinding: ActivityCreateProjectBinding
    private lateinit var apiRepository : ApiRepository
    private lateinit var themeWrapper : ContextThemeWrapper
    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var clipBoard: ClipboardManager
    private lateinit var keyboard : InputMethodManager
    private lateinit var dlg : IntervalDialog
    private var player: SimpleExoPlayer? = null // make player nullable

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
                            keyboard.showSoftInput(v, 0)
                            if(binding.progressBar.visibility == View.VISIBLE){
                                binding.progressBar.visibility == View.GONE
                            }
                        } // keyboard up
                        false -> {
                            keyboard.hideSoftInputFromWindow(binding.root.windowToken, 0) // keyboard down
                            player = null

                            val prevText = block.text
                            val updatedText = binding.projectEditText.text.trim().toString()
                            block.text = updatedText
                            if(block.text != "" && prevText != updatedText) {
                                binding.progressBar.visibility = View.VISIBLE
                                // create block's dubbing request
                                apiRepository.updateBlock(project, block, binding.progressBar, this@BlockAdapter)
                            }
                        }
                    }
                }
                setOnLongClickListener {
                    if (clipBoard.hasPrimaryClip()){
                        showContextMenu()
                        if(clipBoard.primaryClipDescription!!.hasMimeType(MIMETYPE_TEXT_PLAIN)){
                            setText(block.text+clipBoard.text) // paste text
                        }
                    }
                    true
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
                    dlg.show(project, block, binding.progressBar, this@BlockAdapter, intervalMinute, intervalSecond)
                }
            }
            binding.popupNickname.setOnClickListener {
                // choose voice pop up menu
                val popup = PopupMenu(themeWrapper, it)
                popup.run{
                    gravity = Gravity.END
                    menuInflater.inflate(R.menu.menu_voice, popup.menu)
                    setOnMenuItemClickListener { item ->
                        when (item?.itemId) {

                        }
                        false
                    }
                    show()
                }
            }
            binding.projectAddButton.setOnClickListener {
                when(parentBinding.progressBar.visibility){
                    View.VISIBLE -> Toast.makeText(it.context, R.string.toast_please_wait, Toast.LENGTH_SHORT).show()
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
                                this@BlockAdapter
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
                        Toast.makeText(it.context, R.string.toast_please_wait, Toast.LENGTH_SHORT).show()
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
            binding.projectPlayButton.setOnClickListener {
                if(block.audioPath != "" && player==null){
                    streamBlock(it.context,block.audioPath, it)
                }else{
                    println(block.audioPath)
                }
            }
        }
    }
    // stream block dubbing audio
    fun streamBlock(context: Context, mediaUrl: String, view: View){

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
                    when(playbackState){
                        PlaybackStateCompat.STATE_PLAYING ->{
                            view.alpha = 0.3F
                        }
                        else ->{
                            view.alpha = 1F
                            release()
                            player = null
                        }
                    }
                }
            })
        }
    }
    // add block
    fun addBlock(block: Block, pos: Int){
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
    }
    // update block's interval
    fun updateBlock(block:Block){
        val pos = blocks.indexOf(blocks.find{it.id == block.id})
        blocks[pos].run{
            text = block.text
            interval = block.interval
            if(blocks[pos].audioPath == "")
                audioPath = block.audioPath
        }
        notifyItemChanged(pos)
    }
    fun showToast(context: Context, string: String){
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }
}

