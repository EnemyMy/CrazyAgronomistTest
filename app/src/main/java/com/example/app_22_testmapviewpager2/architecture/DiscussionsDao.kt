package com.example.app_22_testmapviewpager2.architecture

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DiscussionsDao {

    @Insert
    fun insertDiscussions(discussionsEntity: DiscussionsEntity)

    @Query("DELETE FROM DiscussionsEntity")
    fun deleteDiscussions()

    @Query("SELECT * FROM DiscussionsEntity")
    fun getDiscussions() : LiveData<DiscussionsEntity>

}