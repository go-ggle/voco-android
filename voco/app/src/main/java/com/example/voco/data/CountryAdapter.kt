package com.example.voco.data

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.R
import com.example.voco.data.model.Country
import com.example.voco.databinding.FragmentCountryBinding

class CountryAdapter(context: Context) : RecyclerView.Adapter<CountryAdapter.ViewHolder>() {
    private lateinit var binding: FragmentCountryBinding
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var countryList : List<Country> = arrayListOf(Country("영어(미국)", R.drawable.ic_america),
        Country("영어(영국)", R.drawable.ic_united_kingdom),
        Country("한국어", R.drawable.ic_south_korea),
        Country("중국어",R.drawable.ic_china),
        Country("일본어",R.drawable.ic_japan),
        Country("프랑스어",R.drawable.ic_france),
        Country("독일어",R.drawable.ic_germany),)
    override fun getItemCount(): Int = countryList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryAdapter.ViewHolder {
        binding = FragmentCountryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryAdapter.ViewHolder, position: Int) {
        holder.bind(countryList[position])
    }

    inner class ViewHolder(private val binding: FragmentCountryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(country : Country){
            binding.countryButton.setImageResource(country.countryIcon)
            binding.countryText.text = country.countryName
        }
    }
}