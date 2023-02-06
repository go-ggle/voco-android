package com.example.voco

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.voco.databinding.ActivityBottomNavigationBinding


class BottomNavigationActivity : AppCompatActivity() {
    private val viewBinding: ActivityBottomNavigationBinding by lazy {
        ActivityBottomNavigationBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    }
                    R.id.menu_board ->{
                        supportFragmentManager
                            .beginTransaction()
                            .replace(viewBinding.navContainer.id, ProjectFragment())
                            .commitAllowingStateLoss()
                    }
                    R.id.menu_calendar ->{
                        supportFragmentManager
                            .beginTransaction()
                            .replace(viewBinding.navContainer.id, FavoritesFragment())
                            .commitAllowingStateLoss()
                    }
                    R.id.menu_setting ->{
                        supportFragmentManager
                            .beginTransaction()
                            .replace(viewBinding.navContainer.id, MypageFragment())
                            .commitAllowingStateLoss()
                    }
                }
                // 리턴값을 true와 false로 받음. 일반적으로는 true로 바로 변경되도록 하면 됨
                true
            }
            // 함수지만 변수처럼 쓸 수 있음. 현재 선택한 item을 알려줄 수 있음
            selectedItemId = R.id.menu_home
        }
    }
}