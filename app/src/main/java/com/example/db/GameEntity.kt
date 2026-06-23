package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val creatorName: String,
    val plays: Int = 0,
    val physicsData: String, // "type,x,y,size,restitution,vx,vy;..."
    val scriptCode: String,
    val createdAt: Long = System.currentTimeMillis()
)
