package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1, 
    val username: String = "Robloxian_2026",
    val statusText: String = "Powering imagination with real-time physics simulation!",
    val tokenBalance: Int = 2500, // Token balance for standard rewards
    val robuxBalance: Int = 450, // Premium currency (Robux)
    val joinedGroups: String = "Roblox Developers,Physics Engineers,Bacon Boys,Noob Association",
    val equippedItems: String = "cap:#FF4D4D,shirt:#4D79FF",
    val age: Int = 12, // Age verification level (1-9 kids, 10-17 select, 18+ normal roblox)
    val avatarBodyType: String = "Noob", // "Noob", "Bacon Boy", "Classic Blocks", "Cyber Armor"
    val avatarHair: String = "Bacon Sprouts", // "Bacon Sprouts", "Classic Square", "Cyber Helmet", "Spikey"
    val avatarFace: String = "Classic Smile", // "Classic Smile", "Gamer Red", "High-Tech Shades", "Bacon Wink"
    val avatarClothing: String = "Noob Hoodie", // "Noob Hoodie", "Bacon Jacket", "Dev Blue Tee", "Cyborg Plate"
    val avatarAccessory: String = "Aura Wings", // "Aura Wings", "Admin Cape", "Gold Headphones", "None"
    val unlockedGamepasses: String = "Cyber City Obby:Speed Coil,Brookhaven Mini:VIP Pass" // purchased gamepasses
)
