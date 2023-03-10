package com.example.voco.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName="Country")
data class Country(
    @PrimaryKey @ColumnInfo(name="countryId") val countryId: Int,
    @NotNull @ColumnInfo(name = "countryName") val countryName: String,
)
@Entity(tableName="Project")
data class Project(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "countryId") val countryId: Int,
    @ColumnInfo(name = "isFavorites") var isFavorites : Boolean,
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name="projectId") val projectId: Int,
)
@Entity(tableName="ProjectInfo", primaryKeys = ["projectId","order"])
data class ProjectInfo(
    @ColumnInfo(name="projectId") val projectId: Int,
    @ColumnInfo(name="order") val order: Int,
    @ColumnInfo(name="voiceId") val voiceId: Int,
    @ColumnInfo(name = "content") var content: String,
    @ColumnInfo(name="intervalMinute") var intervalMinute: Int,
    @ColumnInfo(name="intervalSecond") var intervalSecond: Double,
)
