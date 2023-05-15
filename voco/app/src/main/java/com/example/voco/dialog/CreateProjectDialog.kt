package com.example.voco.dialog

import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import android.widget.Toast
import com.example.voco.api.ApiRepository
import com.example.voco.data.model.Language
import com.example.voco.databinding.DialogCreateProjectBinding
import com.example.voco.ui.BottomNavigationActivity

class CreateProjectDialog(val context: Context) {
    private val binding = DialogCreateProjectBinding.inflate((context as BottomNavigationActivity).layoutInflater)
    private val dlg = Dialog(context)
    private val apiRepository = ApiRepository(context)
    private var language = Language.AMERICA
    fun show(){
        dlg.setContentView(binding.root)

        binding.dialogButton.setOnClickListener {
            when(val title = binding.titleEdit.text.toString()){
                ""->Toast.makeText(context, "제목을 입력해주세요", Toast.LENGTH_SHORT).show()
                else->apiRepository.createProject(title,language)
            }
        }
        binding.createAmerica.setOnClickListener {
            language = Language.AMERICA
            binding.row2.clearCheck()
        }
        binding.createUk.setOnClickListener {
            language = Language.UK
            binding.row2.clearCheck()
        }
        binding.createFrance.setOnClickListener {
            language = Language.FRANCE
            binding.row2.clearCheck()
        }
        binding.createJapan.setOnClickListener {
            language = Language.JAPAN
            binding.row1.clearCheck()
        }
        binding.createChina.setOnClickListener {
            language = Language.CHINA
            binding.row1.clearCheck()
        }
        binding.createGermany.setOnClickListener {
            language = Language.GERMANY
            binding.row1.clearCheck()
        }

        dialogResize()
        dlg.show()
    }
    private fun dialogResize(){
        val window = dlg.window
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
    }
}

