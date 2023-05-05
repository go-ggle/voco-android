package com.example.voco.data.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.api.ApiData
import com.example.voco.databinding.FragmentRecordBinding

class RecordAdapter(private val sentenceList: List<ApiData.SentenceResponse>): RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {
    private lateinit var binding: FragmentRecordBinding

    inner class RecordViewHolder(private val binding: FragmentRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(sentence: ApiData.SentenceResponse) {
            val recordProgress = (1.25*sentence.textId).toFloat()

            binding.recordText.text = sentence.text
            binding.recordPercent.setProgress(recordProgress, false)
        }
    }
    // Called when RecyclerView needs a new RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = FragmentRecordBinding.inflate(inflater, parent, false)
        return RecordViewHolder(binding)
    }
    // Called by RecyclerView to display the data at the specified position.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecordAdapter.RecordViewHolder, position: Int) {
        holder.bind(sentenceList[position])
    }

    override fun getItemCount(): Int = sentenceList.size

    public fun getSentence(position: Int): String{
        return sentenceList[position].text
    }
}