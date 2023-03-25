package com.example.voco.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.R
import com.example.voco.databinding.FragmentTeamBinding
import com.example.voco.login.GlobalApplication

class TeamAdapter(context: Context) : RecyclerView.Adapter<TeamAdapter.ViewHolder>() {
    private lateinit var binding: FragmentTeamBinding
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var teamList :List<Pair<Int,String>> = listOf(Pair(0,"정민정"),Pair(1,"고글"),Pair(2,"GOGGLE"),Pair(3,"GOGGLE"),Pair(4,"GOGGLE"),Pair(5,"GOGGLE"),Pair(6,"GOGGLE"),Pair(7,"GOGGLE"))
    var currentTeam : Int = GlobalApplication.prefs.getString("team","0").toInt()

    override fun getItemCount(): Int = teamList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamAdapter.ViewHolder {
        binding = FragmentTeamBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeamAdapter.ViewHolder, position: Int) {
        holder.bind(teamList[position])
    }

    inner class ViewHolder(private val binding: FragmentTeamBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(team: Pair<Int,String>){
            binding.teamButton.run{
                isSelected = when(adapterPosition){currentTeam->{true} else-> {false}}
                setImageResource(when(adapterPosition){0->{R.drawable.ic_team_one} else -> {R.drawable.ic_team_group}})
            }
            binding.teamName.text = team.second
            binding.teamButton.setOnClickListener {
                if(currentTeam != adapterPosition){
                    updateCurrentTeam(adapterPosition)
                }
            }
        }
    }
    fun updateCurrentTeam(pos: Int){
        notifyItemChanged(currentTeam)
        notifyItemChanged(pos)
        currentTeam = pos
        GlobalApplication.prefs.setString("team","$pos")
    }
}