package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spaces")
data class Space(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val color: Long,
    val isBusiness: Boolean
)

@Entity(tableName = "downloads")
data class Download(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val spaceId: Int,
    val fileName: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val spaceId: Int,
    val url: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)
