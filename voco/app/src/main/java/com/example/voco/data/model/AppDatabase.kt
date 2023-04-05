package com.example.voco.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.voco.data.CountryDao
import com.example.voco.data.ProjectDao

@Database(entities = [Country::class, Project::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun CountryDao() : CountryDao
    abstract fun ProjectDao() : ProjectDao

    companion object{
        private var appDatabase : AppDatabase? = null

        @Synchronized
        fun getCountryInstance(context: Context): AppDatabase?{
            if(appDatabase == null){
                synchronized(AppDatabase::class.java){
                    appDatabase = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "country"
                    ).allowMainThreadQueries()
                        .createFromAsset("database/country.db")
                        .build()
                }
            }
            return  appDatabase
        }
        @Synchronized
        fun getProjectInstance(context: Context): AppDatabase?{
            if(appDatabase == null){
                synchronized(AppDatabase::class.java){
                    appDatabase = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "project"
                    ).allowMainThreadQueries().build()
                }
            }
            return  appDatabase
        }
    }
}