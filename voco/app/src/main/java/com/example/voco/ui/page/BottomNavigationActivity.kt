package com.example.voco.ui.page

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.voco.R
import com.example.voco.databinding.ActivityBottomNavigationBinding

class BottomNavigationActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityBottomNavigationBinding

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        supportFragmentManager
            .beginTransaction()
            .replace(viewBinding.navContainer.id, HomeFragment())
            .commitAllowingStateLoss()

        // run을 쓰면 연결된 요소에 코드를 바로 작성 가능
        viewBinding.bottomNav.run{
            setOnItemSelectedListener {
                when(it.itemId){
                    R.id.menu_home -> {
                        supportFragmentManager
                            .beginTransaction()
                            .replace(viewBinding.navContainer.id, HomeFragment())
                            .commitAllowingStateLoss()
                        window.setBackgroundDrawableResource(R.color.pure_white)
                    }
                    R.id.menu_board ->{
                        try {
                            supportFragmentManager
                                .beginTransaction()
                                .replace(viewBinding.navContainer.id, SearchFragment())
                                .commitAllowingStateLoss()
                            window.setBackgroundDrawableResource(R.drawable.background_search)
                        }catch(e:Exception){
                            println(e)
                        }
                    }
                    R.id.menu_setting ->{
                        supportFragmentManager
                            .beginTransaction()
                            .replace(viewBinding.navContainer.id, MypageFragment())
                            .commitAllowingStateLoss()
                        window.setBackgroundDrawableResource(R.color.light_purple)
                    }
                }
                // 리턴값을 true와 false로 받음. 일반적으로는 true로 바로 변경되도록 하면 됨
                true
            }
            // 함수지만 변수처럼 쓸 수 있음. 현재 선택한 item을 알려줄 수 있음
            selectedItemId = R.id.menu_home
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            val dlg = AlertDialog.Builder(this,android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
            dlg.run{
                setTitle("VOCO 종료")
                setMessage("VOCO를 종료하시겠습니까?")
                setNegativeButton("아니요", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
                setPositiveButton("종료할게요", DialogInterface.OnClickListener { dialog, which ->
                    ActivityCompat.finishAffinity(this@BottomNavigationActivity)
                })
                show()
            }
            return true
        }
        return false
    }
}