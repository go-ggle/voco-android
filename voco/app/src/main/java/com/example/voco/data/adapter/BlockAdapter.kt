package com.example.voco.data.adapter

import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.net.Uri
import android.os.Build
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
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import okhttp3.internal.userAgent

class BlockAdapter (val project: Project, var blocks : ArrayList<Block>) : RecyclerView.Adapter<BlockAdapter.ViewHolder>() {
    private lateinit var binding: FragmentBlockBinding
    private lateinit var parentBinding: ActivityCreateProjectBinding
    private lateinit var apiRepository : ApiRepository
    private lateinit var intervalPicker: IntervalPicker
    private lateinit var themeWrapper : ContextThemeWrapper
    private lateinit var trackSelector: DefaultTrackSelector
    private lateinit var dataSourceFactory: DataSource.Factory
    private lateinit var clipBoard: ClipboardManager
    private lateinit var keyboard : InputMethodManager
    private var player: SimpleExoPlayer? = null // make player nullable

    override fun getItemCount(): Int = blocks.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        binding = FragmentBlockBinding.inflate(inflater, parent, false)
        parentBinding = ActivityCreateProjectBinding.inflate(inflater)
        apiRepository = ApiRepository(parent.context)
        intervalPicker = parent.context as IntervalPicker
        themeWrapper = ContextThemeWrapper(parent.context, R.style.PopupMenuTheme)
        trackSelector = DefaultTrackSelector(parent.context)
        dataSourceFactory = DefaultDataSourceFactory(parent.context, userAgent)
        clipBoard = parent.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        keyboard = parent.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        player = SimpleExoPlayer.Builder(parent.context)
            .setTrackSelector(trackSelector)
            .build()

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
                requestFocus()
                setText(block.text)
                setOnFocusChangeListener { v, hasFocus ->
                    when (hasFocus) {
                        true -> keyboard.showSoftInput(v, 0) // keyboard up
                        false -> {
                            keyboard.hideSoftInputFromWindow(binding.root.windowToken, 0) // keyboard down

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
                    intervalPicker.openIntervalPicker(
                        block,
                        intervalMinute,
                        intervalSecond
                    )
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
                    View.VISIBLE -> showToast(this.itemView.context, "블럭 생성중입니다")
                    else -> {
                        if (adapterPosition >= 0 && blocks[adapterPosition].text == "" || adapterPosition < 0 && blocks[0].text == "") {
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
                if(adapterPosition == 0){
                    // cannot delete first block
                    showToast(this.itemView.context, "첫번째 텍스트입니다")
                }
                else {
                    binding.progressBar.visibility = View.VISIBLE
                    apiRepository.deleteBlock(project.team, project.id, block.id, binding.progressBar, this@BlockAdapter)
                }
            }
            // play block button
            binding.projectPlayButton.setOnClickListener {
                if(block.audioPath != ""){
                    streamBlock(block.audioPath)
                }
            }
        }
    }
    // stream block dubbing audio
    fun streamBlock(mediaUrl: String){
        val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
        val mediaSource = mediaSourceFactory.createMediaSource(Uri.parse(mediaUrl)) // create media source

        player!!.run{
            prepare(mediaSource)
            playWhenReady = true // play the media source
        }
    }
    // add block
    fun addBlock(block: Block, pos: Int){
        blocks.add(pos, block)
        notifyItemInserted(pos)
    }
    // delete block
    fun deleteBlock(blockId: Int){
        val pos = blocks.indexOf(blocks.find{it.id == blockId})
        blocks.removeAt(pos)
        blocks.forEachIndexed { index, block ->
            block.order = index+1
        }
        notifyItemRemoved(pos)
    }
    // update block's interval
    fun updateBlock(block:Block){
        val pos = blocks.indexOf(blocks.find{it.id == block.id})
        blocks[pos].text = block.text
        blocks[pos].interval = block.interval
        if(blocks[pos].audioPath == "")
            blocks[pos].audioPath = block.audioPath
        notifyItemChanged(pos)
    }
    fun showToast(context: Context, string: String){
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }
    interface IntervalPicker{
        fun openIntervalPicker(block: Block, minute:Int, second:Int)
    }
}

