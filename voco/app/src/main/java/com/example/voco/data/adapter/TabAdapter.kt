package com.example.voco.data.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.voco.data.model.Project
import com.example.voco.component.TabFragment

class TabAdapter (fm : FragmentManager, private val projectList: List<Project>): FragmentStatePagerAdapter(fm) {
    //position 에 따라 원하는 Fragment로 이동시키기
    override fun getItem(position: Int): Fragment =  when(position)
    {
        0-> TabFragment(projectList)
        else-> TabFragment(projectList.filter{ it.isFavorites } as ArrayList<Project>)
    }
    //tab의 개수만큼 return
    override fun getCount(): Int = 2

    //tab의 이름 fragment마다 바꾸게 하기
    override fun getPageTitle(position: Int): CharSequence = when(position)
    {
        0->"전체"
        else->"즐겨찾기"
    }

}