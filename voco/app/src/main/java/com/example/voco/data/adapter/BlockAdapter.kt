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
import com.example.voco.databinding.FragmentBlockBinding
import com.example.voco.login.Glob

class BlockAdapter (val context: Context, val project: Project, var blockList : ArrayList<Block>) : RecyclerView.Adapter<BlockAdapter.ViewHolder>() {
    private lateinit var binding: FragmentBlockBinding
    private lateinit var intervalPicker: IntervalPicker
    private lateinit var clipBoard: ClipboardManager
    private lateinit var keyboard : InputMethodManager
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val defaultVoiceId = Glob.prefs.getInt("defaultVoiceId", 0)
    private var isLongclick = false

    override fun getItemCount(): Int = blockList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = FragmentBlockBinding.inflate(inflater, parent, false)
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
                text = when (block.intervalMinute) {
                    0 -> "인터벌 ${block.intervalSecond}초"
                    else -> "인터벌 ${block.intervalMinute}분 ${block.intervalSecond}초"
                }
                // choose interval
                setOnClickListener {
                    intervalPicker.openIntervalPicker(
                        adapterPosition,
                        block.intervalMinute,
                        block.intervalSecond.toInt(),
                        ((block.intervalSecond - block.intervalSecond.toInt()) * 100).toInt()
                    )
                }
            }
            // add block button
            binding.projectAddButton.setOnClickListener {
                addBlock(adapterPosition + 1)
            }
            // delete block button
            binding.projectDeleteButton.setOnClickListener {
                if(adapterPosition == 0){
                    // cannot delete first block
                    Toast.makeText(context, "첫번째 텍스트입니다", Toast.LENGTH_SHORT).show()
                }
                else
                    deleteBlock(adapterPosition)
            }
            // choose voice
            binding.menuLanguage.setOnClickListener {

            }
        }
    }
    // 프로젝트 block 추가
    fun addBlock(position: Int){
        if(position-1>=0 && blockList[position-1].text=="" || position-1<0 && blockList[0].text==""){
            Toast.makeText(context,"내용을 작성해주세요",Toast.LENGTH_SHORT).show()
        }
        else {
            blockList.add(position,Block(position, "", defaultVoiceId, "", 0, 0.01))
            this@BlockAdapter.notifyItemInserted(position)
        }
    }
    // delete block
    fun deleteBlock(position: Int){
        blockList.removeAt(position)
        this@BlockAdapter.notifyItemRemoved(position)
    }
    // update block's interval
    fun updateInterval(position: Int, minute:Int, second: Int, msecond: Int){
        blockList[position].intervalMinute = minute
        blockList[position].intervalSecond = second+msecond*0.01
        this@BlockAdapter.notifyItemChanged(position)
    }
    interface IntervalPicker{
        fun openIntervalPicker(position: Int, minute:Int, second:Int, msecond:Int)
    }
}

