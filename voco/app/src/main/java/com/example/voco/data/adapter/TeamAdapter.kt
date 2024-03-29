package com.example.voco.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.voco.R
import com.example.voco.api.ApiRepository
import com.example.voco.data.model.Team
import com.example.voco.databinding.FragmentHomeBinding
import com.example.voco.databinding.FragmentTeamBinding
import com.example.voco.login.Glob

class TeamAdapter(private val parentBinding: FragmentHomeBinding, var teamList :ArrayList<Team>) : RecyclerView.Adapter<TeamAdapter.ViewHolder>() {
    private lateinit var binding: FragmentTeamBinding
    private lateinit var apiRepository : ApiRepository
    private var currentPos : Int = 0

    override fun getItemCount(): Int = teamList.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamAdapter.ViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        apiRepository = ApiRepository(parent.context)
        binding = FragmentTeamBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }
    override fun onBindViewHolder(holder: TeamAdapter.ViewHolder, position: Int) {
        holder.bind(teamList[position])
    }
    inner class ViewHolder(private val binding: FragmentTeamBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(team: Team){
            binding.teamName.text = team.name
            when(team.id){
                Glob.prefs.getCurrentTeam()->{
                    binding.teamButton.isSelected = true
                    currentPos = adapterPosition
                }
                else ->{
                    binding.teamButton.isSelected = false
                }
            }
            binding.teamButton.run {
                setImageResource(
                    when (adapterPosition) {
                        0 -> R.drawable.ic_team_one
                        else -> R.drawable.ic_team_group
                    }
                )
                setOnClickListener {
                    if(adapterPosition != currentPos){
                        updateCurrentTeam(adapterPosition, team.id)
                    }
                }
            }
        }
    }
    fun updateCurrentTeam(new_pos: Int, id: Int){
        notifyItemChanged(currentPos)
        notifyItemChanged(new_pos)
        currentPos = new_pos
        Glob.prefs.setInt("team",id)
        apiRepository.updateCurrentTeam(parentBinding)
    }
    fun addTeam(newTeam: Team){
        teamList.add(newTeam)
        notifyItemChanged(teamList.size)
    }
}