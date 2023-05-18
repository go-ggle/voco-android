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
    private val apiRepository = ApiRepository(context)
    private val dlg = Dialog(context)
    private var selectedLanguage = Language.AMERICA
    
    fun show(){
        dlg.setContentView(binding.root)
        binding.run {
            dialogButton.setOnClickListener {
                when(val title = binding.titleEdit.text.toString()){
                    "" -> Toast.makeText(context, "제목을 입력해주세요", Toast.LENGTH_SHORT).show() // if no title
                    else -> apiRepository.createProject(title,selectedLanguage) // send create project request
                }
            }
            // choose project's language
            createAmerica.setOnClickListener {
                selectedLanguage = Language.AMERICA
                binding.row2.clearCheck()
            }
            createUk.setOnClickListener {
                selectedLanguage = Language.UK
                binding.row2.clearCheck()
            }
            createFrance.setOnClickListener {
                selectedLanguage = Language.FRANCE
                binding.row2.clearCheck()
            }
            createJapan.setOnClickListener {
                selectedLanguage = Language.JAPAN
                binding.row1.clearCheck()
            }
            createChina.setOnClickListener {
                selectedLanguage = Language.CHINA
                binding.row1.clearCheck()
            }
            createGermany.setOnClickListener {
                selectedLanguage = Language.GERMANY
                binding.row1.clearCheck()
            }
        }
        dialogResize()
        dlg.show()
    }
    private fun dialogResize(){
        val window = dlg.window
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
    }
}

