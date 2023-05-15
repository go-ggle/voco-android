package com.example.voco.data.adapter

import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.data.model.Block
import com.example.voco.data.model.Project
import android.view.inputmethod.InputMethodManager
import com.example.voco.api.ApiRepository
import com.example.voco.databinding.FragmentBlockBinding
import com.example.voco.databinding.ActivityCreateProjectBinding
import com.example.voco.login.Glob

class BlockAdapter (val context: Context, val project: Project, var blockList : ArrayList<Block>) : RecyclerView.Adapter<BlockAdapter.ViewHolder>() {
    private lateinit var binding: FragmentBlockBinding
    private lateinit var parentBinding: ActivityCreateProjectBinding
    private lateinit var intervalPicker: IntervalPicker
    private lateinit var clipBoard: ClipboardManager
    private lateinit var keyboard : InputMethodManager
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val apiRepository = ApiRepository(context)
    private val defaultVoiceId = Glob.prefs.getInt("defaultVoiceId", 0)
    private var isLongclick = false

    override fun getItemCount(): Int = blockList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = FragmentBlockBinding.inflate(inflater, parent, false)
        parentBinding = ActivityCreateProjectBinding.inflate(inflater, parent, false)
        isLongclick = false
        clipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        keyboard = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        intervalPicker = context as IntervalPicker
        holder.bind(blockList[position])
    }

    inner class ViewHolder(private val binding: FragmentBlockBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(block: Block) {
            val intervalMinute = block.interval/60
            val intervalSecond = block.interval%60
            binding.projectEditText.run {
                setText(block.text)
                requestFocus()

                setOnFocusChangeListener { v, hasFocus ->
                    when (hasFocus) {
                        true -> {
                            if(!isLongclick){
                                // keyboard up
                                keyboard.showSoftInput(v, 0)
                            }
                        }
                        false -> {
                            isLongclick = false
                            block.text += binding.projectEditText.text.trim().toString()
                            keyboard.hideSoftInputFromWindow(binding.root.windowToken, 0)
                            // create block's dubbing request
                        }
                    }
                }
                setOnLongClickListener {
                    isLongclick = true
                    // paste text
                    if (clipBoard.hasPrimaryClip()){
                        showContextMenu()
                        if(clipBoard.primaryClipDescription!!.hasMimeType(MIMETYPE_TEXT_PLAIN)){
                            setText(clipBoard.text)
                        }
                    }
                    true
                }
            }
            keyboard.showSoftInput(binding.projectEditText, 0)
            binding.projectIntervalButton.run {
                text = when (intervalMinute) {
                    0 -> "인터벌 ${intervalSecond}초"
                    else -> "인터벌 ${intervalMinute}분 ${intervalSecond}초"
                }
                // choose interval
                setOnClickListener {
                    intervalPicker.openIntervalPicker(
                        adapterPosition,
                        intervalMinute,
                        intervalSecond
                    )
                }
            }
            // add block button
            binding.projectAddButton.setOnClickListener {
                val position = adapterPosition + 1
                if(position-1>=0 && blockList[position-1].text=="" || position-1<0 && blockList[0].text=="") {
                    Toast.makeText(context, "내용을 작성해주세요", Toast.LENGTH_SHORT).show()
                }else{
                    apiRepository.createBlock(project.team,  project.id, position+1, parentBinding)
                }
            }
            // delete block button
            binding.projectDeleteButton.setOnClickListener {
                if(adapterPosition == 0){
                    // cannot delete first block
                    Toast.makeText(context, "첫번째 텍스트입니다", Toast.LENGTH_SHORT).show()
                }
                else
                    apiRepository.deleteBlock(project.team, project.id, block.id, parentBinding)
            }
            // choose voice
            binding.menuLanguage.setOnClickListener {

            }
        }
    }
    // 프로젝트 block 추가
    fun addBlock(block: Block, pos: Int){
        blockList.add(pos, block)
        this@BlockAdapter.notifyItemInserted(pos)
    }
    // delete block
    fun deleteBlock(blockId: Int){
        val idx = blockList.indexOf(blockList.find{it.id == blockId})
        blockList.removeAt(idx)
        blockList.forEachIndexed{i,b->
            b.order = i+1
        }
        this@BlockAdapter.notifyItemRangeChanged(idx, itemCount-idx)
    }
    // update block's interval
    fun updateInterval(position: Int, minute:Int, second: Int){
        blockList[position].interval = minute*60+second
        this@BlockAdapter.notifyItemChanged(position)
    }
    interface IntervalPicker{
        fun openIntervalPicker(position: Int, minute:Int, second:Int)
    }
}

