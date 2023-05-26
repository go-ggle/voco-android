package com.example.voco.data.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.data.model.Dto
import com.example.voco.databinding.FragmentRecordBinding

class RecordAdapter(private val sentenceList: List<Dto.SentenceResponse>): RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {
    private lateinit var binding: FragmentRecordBinding

    override fun getItemCount(): Int = sentenceList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = FragmentRecordBinding.inflate(inflater, parent, false)

        return RecordViewHolder(binding)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecordAdapter.RecordViewHolder, position: Int) {
        holder.bind(sentenceList[position])
    }
    inner class RecordViewHolder(private val binding: FragmentRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(sentence: Dto.SentenceResponse) {
            val recordProgress = (1.25*sentence.textId).toFloat()

            binding.run{
                recordText.text = sentence.text
                recordPercent.setProgress(recordProgress, false)
            }
        }
    }
}