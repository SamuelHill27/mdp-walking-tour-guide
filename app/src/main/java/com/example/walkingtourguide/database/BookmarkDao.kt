package com.example.walkingtourguide.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmark")
    fun getAll(): List<Bookmark>

    @Insert
    fun insertAll(vararg bookmarks: Bookmark)

    @Delete
    fun delete(bookmark: Bookmark)
}