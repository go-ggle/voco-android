package com.example.voco.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.voco.data.model.Country
import com.example.voco.data.model.Project

@Dao // Data Access Object
interface CountryDao {
    @Query("SELECT * FROM Country WHERE countryId = :countryId")
    fun selectById(countryId: Int): Country
}
@Dao
interface ProjectDao {
    @Insert
    fun insert(projectList: List<Project>)
    @Query("UPDATE Project SET bookmarked = :isChecked WHERE id = :projectId")
    fun updateBookmark(projectId: Int, isChecked: Boolean)
    @Query("DELETE FROM Project")
    fun deleteAll()
    @Query("SELECT * FROM Project")
    fun selectAll(): List<Project>
}