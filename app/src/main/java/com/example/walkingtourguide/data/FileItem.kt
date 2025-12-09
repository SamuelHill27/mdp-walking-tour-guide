package com.example.walkingtourguide.data

import java.io.Serializable

data class FileItem(
    val filename: String,
    val duration: Int,
    var prevFile: FileItem? = null,
    var nextFile: FileItem? = null
) : Serializable
