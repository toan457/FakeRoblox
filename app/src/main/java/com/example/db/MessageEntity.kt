package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val recipient: String, // empty if public group chat
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
