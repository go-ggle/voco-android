package com.example.voco.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.voco.data.model.Country

@Dao // Data Access Object
interface CountryDao {
    @Insert
    fun insert(country: Country)

    @Query("SELECT * FROM Country WHERE countryId = :countryId")
    fun selectById(countryId: Int): Country
}