package com.example.walkingtourguide.data

import java.io.Serializable

data class BookmarkItem(
    val uid: Int,
    val fileItem: FileItem,
    val timeStamp: Int
) : Serializable
