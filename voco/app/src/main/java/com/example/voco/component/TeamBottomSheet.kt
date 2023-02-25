package com.example.voco.component

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.voco.databinding.BottomSheetTeamBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TeamBottomSheet() : BottomSheetDialogFragment() {
    private lateinit var viewBinding: BottomSheetTeamBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        viewBinding = BottomSheetTeamBinding.inflate(layoutInflater)
        viewBinding.closeBtn.setOnClickListener{
            dismiss()
        }
        viewBinding.option1.setOnClickListener {
            viewBinding.subTitle.text= "생성할 팀 스페이스의 이름을 입력해주세요"
            viewBinding.editText.hint = "이름을 입력해주세요"
            viewBinding.btn.btnRect.text = "다음"
            viewBinding.editText.setText("")

            viewBinding.optionButtonContainer.visibility = View.GONE
            viewBinding.optionContainer.visibility = View.GONE
            viewBinding.editText.visibility = View.VISIBLE
            viewBinding.btn.root.visibility = View.VISIBLE
        }
        viewBinding.option2.setOnClickListener {
            viewBinding.subTitle.text= "초대받은 팀 스페이스에 참여하기"
            viewBinding.editText.hint = "초대코드를 입력해주세요"
            viewBinding.btn.btnRect.text = "다음"
            viewBinding.editText.setText("")

            viewBinding.optionButtonContainer.visibility = View.GONE
            viewBinding.optionContainer.visibility = View.GONE
            viewBinding.editText.visibility = View.VISIBLE
            viewBinding.btn.root.visibility = View.VISIBLE
        }
        viewBinding.btn.root.setOnClickListener {
            when(viewBinding.subTitle.text){
                "생성할 팀 스페이스의 이름을 입력해주세요"->{
                    // 팀 생성 api
                    if(viewBinding.editText.text.toString().trim() == "")
                        Toast.makeText(requireContext(),"이름을 입력해주세요",Toast.LENGTH_SHORT).show()
                    else {
                        viewBinding.subTitle.text = "초대코드를 공유해주세요"
                        viewBinding.boldText.text = "XJ56JK7" // 초대코드
                        viewBinding.btn.btnRect.text = "초대코드 복사하기"
                        viewBinding.editText.visibility = View.GONE
                        viewBinding.boldText.visibility = View.VISIBLE
                    }
                }
                "초대받은 팀 스페이스에 참여하기"->{
                    if(viewBinding.editText.text.toString().trim() == "")
                        Toast.makeText(requireContext(),"초대코드를 입력해주세요",Toast.LENGTH_SHORT).show()
                    else {
                        // 초대코드로 팀 검색 api
                        viewBinding.subTitle.text = "팀 스페이스"
                        viewBinding.boldText.text = "고글" // 팀 이름
                        viewBinding.btn.btnRect.text = "참여하기"
                        viewBinding.editText.visibility = View.GONE
                        viewBinding.boldText.visibility = View.VISIBLE
                    }
                }
                "초대코드를 공유해주세요"->{
                    val clip : ClipData = ClipData.newPlainText("invitation code",viewBinding.boldText.text)
                    clipboardManager.setPrimaryClip(clip)
                    Toast.makeText(requireContext(),"초대코드가 복사되었습니다",Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                else ->{
                    // 팀 참여 api
                    dismiss()
                }
            }
        }
        return viewBinding.root

    }

}