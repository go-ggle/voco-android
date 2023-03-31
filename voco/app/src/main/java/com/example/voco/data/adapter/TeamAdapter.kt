package com.example.voco.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.R
import com.example.voco.data.model.Team
import com.example.voco.databinding.FragmentTeamBinding
import com.example.voco.login.GlobalApplication

class TeamAdapter(context: Context, var teamList :List<Team>) : RecyclerView.Adapter<TeamAdapter.ViewHolder>() {
    private lateinit var binding: FragmentTeamBinding
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var currentTeam : Int = GlobalApplication.prefs.getString("team", GlobalApplication.prefs.getString("workspace", "")).toInt()

    override fun getItemCount(): Int = teamList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamAdapter.ViewHolder {
        binding = FragmentTeamBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeamAdapter.ViewHolder, position: Int) {
        holder.bind(teamList[position])
    }

    inner class ViewHolder(private val binding: FragmentTeamBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(team: Team){
            binding.teamButton.run{
                isSelected = when(adapterPosition){currentTeam->{true} else-> {false}}
                setImageResource(when(adapterPosition){0->{R.drawable.ic_team_one} else -> {R.drawable.ic_team_group}})
            }
            binding.teamName.text = team.name
            binding.teamButton.setOnClickListener {
                if(currentTeam != adapterPosition){
                    updateCurrentTeam(team.id)
                }
            }
        }
    }
    fun updateCurrentTeam(id: Int){
        notifyItemChanged(currentTeam)
        notifyItemChanged(id)
        currentTeam = id
        GlobalApplication.prefs.setString("team","$id")
    }
}