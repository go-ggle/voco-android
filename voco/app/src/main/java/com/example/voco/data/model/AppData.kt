package com.example.voco.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Entity(tableName="Country")
data class Country(
    @PrimaryKey @ColumnInfo(name="countryId") val countryId: Int,
    @NotNull @ColumnInfo(name = "countryName") val countryName: String,
)
@Entity(tableName="Project", primaryKeys = ["id"])
data class Project(
    @ColumnInfo(name="id") val id: Int,
    @ColumnInfo(name="team") val team: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "language") val language: Int,
    @ColumnInfo(name = "updatedAt") val updatedAt: String,
    @ColumnInfo(name = "bookmarked") var bookmarked : Boolean,
)
@Entity(tableName="Block", primaryKeys = ["order"])
data class Block(
    @ColumnInfo(name="order") val order: Int,
    @ColumnInfo(name="audioPath") val audioPath: String,
    @ColumnInfo(name="voiceId") val voiceId: Int,
    @ColumnInfo(name = "text") var text: String,
    @ColumnInfo(name="intervalMinute") var intervalMinute: Int,
    @ColumnInfo(name="intervalSecond") var intervalSecond: Double,
)
@Entity(tableName="Team", primaryKeys = ["id"])
data class Team(
    @ColumnInfo(name="id") val id: Int,
    @ColumnInfo(name="name") val name: String,
    @Nullable @ColumnInfo(name="teamCode") val teamCode: String,
    @ColumnInfo(name = "private") var private: Boolean,
)
@Entity(tableName="Voice", primaryKeys = ["id"])
data class Voice(
    @ColumnInfo(name="id") val id: Int,
    @ColumnInfo(name="nickname") val name: String,
)

