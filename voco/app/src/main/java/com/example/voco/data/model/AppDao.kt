package com.example.voco.data.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

//@Dao
//interface UserDao{
//
//}
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
    @Query("UPDATE Project SET title = :title WHERE id = :projectId")
    fun updateTitle(projectId: Int, title: String)
    @Query("DELETE FROM Project WHERE id = :projectId")
    fun delete(projectId: Int)
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