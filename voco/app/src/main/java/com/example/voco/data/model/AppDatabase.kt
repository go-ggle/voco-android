package com.example.voco.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.voco.data.CountryDao

@Database(entities = [Country::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun CountryDao() : CountryDao

    companion object{
        private var appDatabase : AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase?{
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
    }
}