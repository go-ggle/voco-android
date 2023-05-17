package com.example.voco.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.voco.api.ApiRepository
import com.example.voco.data.adapter.TeamAdapter
import com.example.voco.databinding.BottomSheetTeamBinding
import com.example.voco.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TeamBottomSheet(private val parentBinding: FragmentHomeBinding, private val apiRepository : ApiRepository) : BottomSheetDialogFragment() {
  private lateinit var viewBinding: BottomSheetTeamBinding
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    viewBinding = BottomSheetTeamBinding.inflate(layoutInflater)
    // close bottom sheet
    viewBinding.closeBtn.setOnClickListener{
      dismiss()
    }
    // create team option button
    viewBinding.option1.setOnClickListener {
      viewBinding.optionContainer.visibility = View.GONE
      viewBinding.optionButtonContainer.visibility = View.GONE
      viewBinding.subTitle.text= "생성할 팀 스페이스의 이름을 입력해주세요"

      viewBinding.editText.run {
        hint = "이름을 입력해주세요"
        visibility = View.VISIBLE
        setText("")
      }
      viewBinding.btn.run{
        btnRect.text = "다음"
        root.visibility = View.VISIBLE
      }

    }
    // join team option button
    viewBinding.option2.setOnClickListener {
      viewBinding.optionButtonContainer.visibility = View.GONE
      viewBinding.optionContainer.visibility = View.GONE
      viewBinding.subTitle.text= "초대받은 팀 스페이스에 참여하기"

      viewBinding.editText.run {
        hint = "초대코드를 입력해주세요"
        visibility = View.VISIBLE
        setText("")
      }
      viewBinding.btn.run{
        btnRect.text = "다음"
        root.visibility = View.VISIBLE
      }
    }
    viewBinding.btn.root.setOnClickListener {
      when(viewBinding.subTitle.text){
        // create team workspace
        "생성할 팀 스페이스의 이름을 입력해주세요"->{
          when {
            // if team name is empty string
            viewBinding.editText.text.toString().trim() == "" -> Toast.makeText(requireContext(),"이름을 입력해주세요",Toast.LENGTH_SHORT).show()
            // if team name length is not 2..8
            viewBinding.editText.text?.length !in 2..8 -> {
              Toast.makeText(requireContext(), "2자에서 8자 사이로 입력해주세요", Toast.LENGTH_SHORT).show()
            }
            else -> {
              // send create team request
              apiRepository.createTeam(viewBinding, viewBinding.editText.text.toString(), parentBinding.teams.adapter as TeamAdapter)
            }
          }
        }
        "초대받은 팀 스페이스에 참여하기"->{
          if(viewBinding.editText.text.toString().trim() == "")
            Toast.makeText(requireContext(),"초대코드를 입력해주세요",Toast.LENGTH_SHORT).show()
          else {
            // send join team request
            apiRepository.joinTeam(viewBinding, viewBinding.editText.text.toString(), parentBinding.teams.adapter as TeamAdapter)
          }
        }
        "초대코드를 공유해주세요"->{
          // copy team code
          val clip : ClipData = ClipData.newPlainText("invitation code",viewBinding.boldText.text)
          clipboardManager.setPrimaryClip(clip)
          Toast.makeText(requireContext(),"초대코드가 복사되었습니다",Toast.LENGTH_SHORT).show()
          dismiss()
        }
        else ->{
          // after join team request is success
          dismiss()
        }
      }
    }
    return viewBinding.root

  }

}