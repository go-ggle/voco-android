package com.example.voco.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.voco.data.model.Block
import com.example.voco.data.model.Country
import com.example.voco.data.model.Project
import com.example.voco.data.model.Voice

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
    @Query("SELECT * FROM Project WHERE id = :projectId")
    fun selectById(projectId: Int): Project
}
@Dao
interface BlockDao {
    @Insert
    fun insert(blockList: List<Block>)
    @Query("SELECT * FROM Block")
    fun selectAll(): List<Block>
    @Query("DELETE FROM Block")
    fun deleteAll()
}
@Dao
interface VoiceDao {
    @Insert
    fun insert(voiceList: List<Voice>)
    @Query("SELECT * FROM Voice")
    fun selectAll(): List<Voice>
    @Query("DELETE FROM Voice")
    fun deleteAll()
}