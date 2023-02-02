package com.example.voco.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="Country")
data class Country(
    @ColumnInfo(name = "countryName") val countryName: String,
    @ColumnInfo(name = "countryIcon") val countryIcon: Int,
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name="countryId") val countryId: Int = 0,
)
@Entity(tableName="Project")
data class Project(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "content") val content: String,
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name="projectId") val projectId: Int = 0,
)
