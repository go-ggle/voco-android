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
import com.example.voco.login.GlobalApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeamAdapter(context: Context, private val parentBinding: FragmentHomeBinding, var teamList :ArrayList<Team>) : RecyclerView.Adapter<TeamAdapter.ViewHolder>() {
    private lateinit var binding: FragmentTeamBinding
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var currentPos : Int = 0
    private val apiRepository = ApiRepository(context)

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
            when(team.id){
                GlobalApplication.prefs.getString("team",GlobalApplication.prefs.getString("workspace","1")).toInt()->{
                    binding.teamButton.run{
                        isSelected = true
                    }
                    currentPos = adapterPosition
                }
                else ->{
                    binding.teamButton.run{
                        isSelected = false
                    }
                }
            }
            when(adapterPosition){
                0 -> binding.teamButton.setImageResource(R.drawable.ic_team_one)
                else -> binding.teamButton.setImageResource(R.drawable.ic_team_group)
            }
            binding.teamName.text = team.name
            binding.teamButton.setOnClickListener {
                if(adapterPosition != currentPos){
                    updateCurrentTeam(adapterPosition, team.id)
                }
            }
        }
    }
    fun updateCurrentTeam(new_pos: Int, id: Int){
        notifyItemChanged(currentPos)
        notifyItemChanged(new_pos)
        currentPos = new_pos
        GlobalApplication.prefs.setString("team","$id")
        apiRepository.updateProjectList(parentBinding)
    }
    fun addTeam(newTeam: Team){
        teamList.add(newTeam)
        notifyItemChanged(teamList.size)
    }
}