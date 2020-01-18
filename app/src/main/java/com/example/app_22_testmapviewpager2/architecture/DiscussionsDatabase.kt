package com.example.app_22_testmapviewpager2.architecture

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DiscussionsEntity::class], version = 1)
abstract class DiscussionsDatabase : RoomDatabase() {

    abstract fun getDao() : DiscussionsDao

    companion object {
        private lateinit var instance : DiscussionsDatabase

        @Synchronized fun getInstance(context : Context) : DiscussionsDatabase {
            if (!this::instance.isInitialized) {
                instance = Room.databaseBuilder(context.applicationContext, DiscussionsDatabase::class.java, "Discussions database")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance
        }

    }

}