package com.example.voco.data.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.voco.component.TabFragment
import com.example.voco.data.model.Project

class TabAdapter (fm : FragmentManager, projectList: List<Project>): FragmentStatePagerAdapter(fm) {
    private val tabFragment1 = TabFragment(projectList)
    private val tabFragment2 = TabFragment(projectList.filter{ it.bookmarked } as ArrayList<Project>)

    override fun getCount(): Int = 2
    override fun getItem(position: Int): Fragment =  when(position) {
        0-> tabFragment1
        else-> tabFragment2
    }
    override fun getPageTitle(position: Int): CharSequence = when(position) {
        0->"전체"
        else->"즐겨찾기"
    }
    fun updateProjectList(newProjectList: List<Project>){
        tabFragment1.updateProjectList(newProjectList)
        tabFragment2.updateProjectList(newProjectList.filter{ it.bookmarked } as ArrayList<Project>)
    }
}