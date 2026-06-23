package com.example.physics

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class PhysicsObject(
    val id: String = UUID.randomUUID().toString(),
    var type: String, // "circle", "box", "trampoline", "lava", "star", "portal"
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var radius: Float = 25f,
    var width: Float = 60f,
    var height: Float = 40f,
    var restitution: Float = 0.7f,
    var isStatic: Boolean = false,
    var mass: Float = 1.0f,
    var colorHex: String = "#FF5722",
    var label: String = "",
    var isCollected: Boolean = false,
    var logicTag: String = "" // "lava", "trampoline", "star", "boost"
) {
    fun toDataString(): String {
        return "$type,$x,$y,$vx,$vy,$radius,$width,$height,$restitution,${if (isStatic) 1 else 0},$mass,$colorHex,$label,${if (isCollected) 1 else 0},$logicTag"
    }

    companion object {
        fun fromDataString(str: String): PhysicsObject? {
            return try {
                val parts = str.split(",")
                if (parts.size < 15) return null
                PhysicsObject(
                    type = parts[0],
                    x = parts[1].toFloat(),
                    y = parts[2].toFloat(),
                    vx = parts[3].toFloat(),
                    vy = parts[4].toFloat(),
                    radius = parts[5].toFloat(),
                    width = parts[6].toFloat(),
                    height = parts[7].toFloat(),
                    restitution = parts[8].toFloat(),
                    isStatic = parts[9].toInt() == 1,
                    mass = parts[10].toFloat(),
                    colorHex = parts[11],
                    label = parts[12],
                    isCollected = parts[13].toInt() == 1,
                    logicTag = parts[14]
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
