package com.example.voco.data

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.R
import com.example.voco.databinding.FragmentCountryIconBinding

class ProjectCountryAdapter (context: Context, val countryList : List<Int>) : RecyclerView.Adapter<ProjectCountryAdapter.ViewHolder>() {
    private lateinit var binding: FragmentCountryIconBinding
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getItemCount(): Int = countryList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectCountryAdapter.ViewHolder {
        binding = FragmentCountryIconBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectCountryAdapter.ViewHolder, position: Int) {
        holder.bind(countryList[position])
    }

    inner class ViewHolder(private val binding: FragmentCountryIconBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(country : Int){
            binding.countryIcon.setImageResource(country)
        }
    }
}