package com.example.voco.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.voco.R
import com.example.voco.api.ApiRepository
import com.example.voco.databinding.FragmentMypageBinding
import com.example.voco.login.Glob
import com.kakao.sdk.user.UserApiClient

class MypageFragment : Fragment() {
    private lateinit var binding: FragmentMypageBinding
    private lateinit var apiRepository : ApiRepository
    override fun onAttach(context: Context) {
        super.onAttach(context)
        apiRepository = ApiRepository(context)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMypageBinding.inflate(inflater)
        binding.logout.setOnClickListener {
            when(Glob.prefs.loginMode()){
                "kakao"->{
                    UserApiClient.instance.logout { error ->
                        when(error){
                            null ->{
                                val intent = Intent(context, LoginActivity::class.java)
                                startActivity(intent)
                                ActivityCompat.finishAffinity(requireActivity())
                                Glob.prefs.logout()
                            }
                            else -> {
                                Toast.makeText(context, R.string.toast_request_error, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
        binding.voiceRecord.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            apiRepository.getSentence(binding)
        }

        return binding.root
    }
}
