package com.example.voco.data.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.voco.data.model.Project
import com.example.voco.component.TabFragment
import com.example.voco.data.model.AppDatabase
import okhttp3.internal.notifyAll

class TabAdapter (fm : FragmentManager, private var projectList: List<Project>): FragmentStatePagerAdapter(fm) {
    val tabFragment1 = TabFragment(projectList)
    val tabFragment2 = TabFragment(projectList.filter{ it.bookmarked } as ArrayList<Project>)
    //position 에 따라 원하는 Fragment로 이동시키기
    override fun getItem(position: Int): Fragment =  when(position)
    {
        0-> tabFragment1
        else-> tabFragment2
    }
    //tab의 개수만큼 return
    override fun getCount(): Int = 2

    //tab의 이름 fragment마다 바꾸게 하기
    override fun getPageTitle(position: Int): CharSequence = when(position)
    {
        0->"전체"
        else->"즐겨찾기"
    }
    fun updateProjectList(newProjectList: List<Project>){
        tabFragment1.updateProjectList(newProjectList)
        tabFragment2.updateProjectList(newProjectList.filter{ it.bookmarked } as ArrayList<Project>)
    }

}