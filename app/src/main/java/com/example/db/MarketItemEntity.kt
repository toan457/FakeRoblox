package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_items")
data class MarketItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val creator: String,
    val price: Int,
    val styleType: String, // "cap", "shirt", "pant", "wings", "face"
    val colorHex: String,
    val isUgc: Boolean = false,
    val purchaseCount: Int = 0
)
