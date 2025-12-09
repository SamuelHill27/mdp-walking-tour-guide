package com.example.walkingtourguide.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "filename") val filename: String,
    @ColumnInfo(name = "timestamp") val timestamp: Int
)
