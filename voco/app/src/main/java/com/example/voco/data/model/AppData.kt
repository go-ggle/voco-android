package com.example.voco.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

//@Entity(tableName="User")
//data class User(
//    @PrimaryKey @ColumnInfo(name="id") val id: String, // default: snsUser
//    @NotNull @ColumnInfo(name = "pwd") val pwd: String, // default: snsUser
//    @NotNull @ColumnInfo(name = "team") val team: Int, // default: 0
//    @NotNull @ColumnInfo(name = "workSpace") val workSpace: Int, // default: 0
//    @NotNull @ColumnInfo(name = "defaultVoice") val defaultVoice: Int, // default: 0
//    @NotNull @ColumnInfo(name = "sns") val sns: SNS, // default: EMAIL
//    @NotNull @ColumnInfo(name = "token") val token: String, // default: logout
//    @NotNull @ColumnInfo(name = "refreshToken") val refreshToken: String, // default: logout
//)
@Entity(tableName="Country")
data class Country(
    @PrimaryKey @ColumnInfo(name="countryId") val countryId: Int,
    @NotNull @ColumnInfo(name = "countryName") val countryName: String,
)
@Entity(tableName="Project", primaryKeys = ["id"])
data class Project(
    @ColumnInfo(name="id") val id: Int,
    @ColumnInfo(name="team") val team: Int,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "language") val language: Int,
    @ColumnInfo(name = "updatedAt") val updatedAt: String,
    @ColumnInfo(name = "bookmarked") var bookmarked : Boolean,
)
@Entity(tableName="Block", primaryKeys = ["id"])
data class Block(
    @ColumnInfo(name="id") val id: Int,
    @ColumnInfo(name = "text") var text: String,
    @ColumnInfo(name="audioPath") var audioPath: String,
    @ColumnInfo(name="interval") var interval: Int,
    @ColumnInfo(name="voiceId") var voiceId: Int,
    @ColumnInfo(name="order") var order: Int
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
    @ColumnInfo(name="nickname") val nickname: String,
)

