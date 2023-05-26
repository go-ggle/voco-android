package com.example.voco.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import com.example.voco.api.ApiRepository
import com.example.voco.data.model.Language
import com.example.voco.databinding.DialogCreateProjectBinding

class CreateProjectDialog(val context: Context) {
    private val binding = DialogCreateProjectBinding.inflate(LayoutInflater.from(context))
    private val apiRepository = ApiRepository(context)
    private val dlg = Dialog(context)
    private var selectedLanguage = Language.ENGLISH

    fun show(progressBar: ProgressBar){
        dlg.setContentView(binding.root)
        binding.run {
            dialogButton.setOnClickListener {
                when(val title = binding.titleEdit.text.toString()){
                    "" -> Toast.makeText(context, "제목을 입력해주세요", Toast.LENGTH_SHORT).show() // if no title
                    else -> {
                        progressBar.visibility = View.VISIBLE
                        apiRepository.createProject(title,selectedLanguage, progressBar) // send create project request
                        dlg.dismiss()
                    }
                }
            }
            // choose project's language
            createAmerica.setOnClickListener {
                selectedLanguage = Language.ENGLISH
                binding.row2.clearCheck()
                binding.row3.clearCheck()
                binding.row4.clearCheck()
            }
            createUk.setOnClickListener {
                selectedLanguage = Language.ENGLISH_UK
                binding.row2.clearCheck()
                binding.row3.clearCheck()
                binding.row4.clearCheck()
            }
            createIndia.setOnClickListener {
                selectedLanguage = Language.ENGLISH_INDIA
                binding.row2.clearCheck()
                binding.row3.clearCheck()
                binding.row4.clearCheck()
            }
            createSpain.setOnClickListener {
                selectedLanguage = Language.SPANISH
                binding.row1.clearCheck()
                binding.row3.clearCheck()
                binding.row4.clearCheck()
            }
            createFrance.setOnClickListener {
                selectedLanguage = Language.FRENCH
                binding.row1.clearCheck()
                binding.row3.clearCheck()
                binding.row4.clearCheck()
            }
            createItaly.setOnClickListener {
                selectedLanguage = Language.ITALIAN
                binding.row1.clearCheck()
                binding.row3.clearCheck()
                binding.row4.clearCheck()
            }

            createGermany.setOnClickListener {
                selectedLanguage = Language.GERMAN
                binding.row1.clearCheck()
                binding.row2.clearCheck()
                binding.row4.clearCheck()
            }
            createRussian.setOnClickListener {
                selectedLanguage = Language.RUSSIAN
                binding.row1.clearCheck()
                binding.row2.clearCheck()
                binding.row4.clearCheck()
            }
            createArab.setOnClickListener {
                selectedLanguage = Language.ARABIC
                binding.row1.clearCheck()
                binding.row2.clearCheck()
                binding.row4.clearCheck()
            }

            createChina.setOnClickListener {
                selectedLanguage = Language.CHINESE
                binding.row1.clearCheck()
                binding.row2.clearCheck()
                binding.row3.clearCheck()
            }
            createJapan.setOnClickListener {
                selectedLanguage = Language.JAPANESE
                binding.row1.clearCheck()
                binding.row2.clearCheck()
                binding.row3.clearCheck()
            }
            createIndonesia.setOnClickListener {
                selectedLanguage = Language.INDONESIAN
                binding.row1.clearCheck()
                binding.row2.clearCheck()
                binding.row3.clearCheck()
            }
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

