package com.example.voco.dialog

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import com.example.voco.api.ApiRepository
import com.example.voco.data.adapter.BlockAdapter
import com.example.voco.data.model.Block
import com.example.voco.data.model.Project
import com.example.voco.databinding.DialogIntervalPickerBinding
import com.example.voco.ui.CreateProjectActivity

class IntervalDialog(val context: Context) {
    private lateinit var intervalBlock : Block
    private val dlg = Dialog(context)
    private val apiRepository = ApiRepository(context)
    private val binding = DialogIntervalPickerBinding.inflate((context as CreateProjectActivity).layoutInflater)

    fun show(project: Project, block: Block, progressBar: ProgressBar, blockAdapter: BlockAdapter, minute:Int, second:Int){
        intervalBlock = block
        binding.run {
            this.minute.run{
                minValue=0
                maxValue=99
                value = minute
            }
            this.second.run {
                minValue=0
                maxValue=59
                value = second
            }
            root.visibility = View.VISIBLE
        }
        dlg.setContentView(binding.root)

        binding.minute.setOnValueChangedListener { picker, oldVal, newVal ->
            intervalBlock.interval = newVal * 60 + binding.second.value
        }
        binding.second.setOnValueChangedListener { picker, oldVal, newVal ->
            intervalBlock.interval = binding.minute.value * 60 + newVal
        }
        binding.cancelButton.setOnClickListener {
            dlg.dismiss()
        }
        binding.dialogButton.setOnClickListener {
            if(intervalBlock.text == ""){
                Toast.makeText(context, "텍스트를 입력해주세요",Toast.LENGTH_SHORT).show()
            }
            else {
                progressBar.visibility = View.VISIBLE
                apiRepository.updateBlock(project, block, progressBar, blockAdapter)
            }
            dlg.dismiss()
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
