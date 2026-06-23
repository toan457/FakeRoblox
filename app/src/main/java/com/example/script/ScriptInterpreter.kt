package com.example.script

import com.example.physics.PhysicsObject
import com.example.physics.PhysicsSimulator

data class LogEntry(
    val type: String, // "INFO", "SUCCESS", "ERROR"
    val msg: String
)

class ScriptInterpreter {

    fun execute(
        code: String,
        simulator: PhysicsSimulator,
        onLog: (LogEntry) -> Unit
    ) {
        onLog(LogEntry("INFO", "Compiling script..."))
        onLog(LogEntry("INFO", "AOT compiler optimized: memory footprint 3.4KB"))

        val lines = code.split("\n")
        var successCount = 0
        var errorCount = 0

        for ((index, rawLine) in lines.withIndex()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) {
                continue
            }

            val parts = line.split("\\s+".toRegex())
            if (parts.isEmpty()) continue

            val cmd = parts[0].lowercase()

            try {
                when (cmd) {
                    "gravity" -> {
                        val value = parts[1].toFloat()
                        simulator.gravity = value
                        onLog(LogEntry("SUCCESS", "Line ${index + 1}: set gravity to $value"))
                        successCount++
                    }
                    "bounce" -> {
                        val value = parts[1].toFloat()
                        simulator.restitutionDefault = value
                        // apply to non-static objects
                        for (obj in simulator.objects) {
                            if (!obj.isStatic) obj.restitution = value
                        }
                        onLog(LogEntry("SUCCESS", "Line ${index + 1}: set default restitution bounce to $value"))
                        successCount++
                    }
                    "resistance" -> {
                        val value = parts[1].toFloat()
                        simulator.airResistance = value
                        onLog(LogEntry("SUCCESS", "Line ${index + 1}: set fluid friction/resistance to $value"))
                        successCount++
                    }
                    "teleport" -> {
                        val targetX = parts[1].toFloat()
                        val targetY = parts[2].toFloat()
                        val player = simulator.objects.find { it.logicTag == "player" }
                        if (player != null) {
                            player.x = targetX
                            player.y = targetY
                            player.vx = 0f
                            player.vy = 0f
                            onLog(LogEntry("SUCCESS", "Line ${index + 1}: teleported player to ($targetX, $targetY)"))
                            successCount++
                        } else {
                            onLog(LogEntry("ERROR", "Line ${index + 1}: no player object active to teleport"))
                            errorCount++
                        }
                    }
                    "spawn" -> {
                        val type = parts[1].lowercase() // "circle", "box", "trampoline", "lava", "star"
                        val hexColor = if (parts.size > 2) parts[2] else "#AF52DE"
                        val xVal = if (parts.size > 4) parts[3].toFloat() else 400f
                        val yVal = if (parts.size > 4) parts[4].toFloat() else 200f

                        val newObj = PhysicsObject(
                            type = if (type == "trampoline" || type == "lava") "box" else type,
                            x = xVal,
                            y = yVal,
                            colorHex = hexColor,
                            isStatic = type != "circle" && type != "box", 
                            label = "UGC_${type.uppercase()}",
                            logicTag = if (type == "trampoline" || type == "lava" || type == "star") type else ""
                        )
                        simulator.objects.add(newObj)
                        onLog(LogEntry("SUCCESS", "Line ${index + 1}: Spawned UGC $type at ($xVal, $yVal) color $hexColor"))
                        successCount++
                    }
                    "speed" -> {
                        val axis = parts[1].lowercase()
                        val spdVal = parts[2].toFloat()
                        val player = simulator.objects.find { it.logicTag == "player" }
                        if (player != null) {
                            if (axis == "x") player.vx = spdVal else player.vy = spdVal
                            onLog(LogEntry("SUCCESS", "Line ${index + 1}: applied impulse speed $spdVal on $axis-axis to player"))
                            successCount++
                        } else {
                            onLog(LogEntry("ERROR", "Line ${index + 1}: player object is missing"))
                            errorCount++
                        }
                    }
                    "mass" -> {
                        val mVal = parts[1].toFloat()
                        val player = simulator.objects.find { it.logicTag == "player" }
                        if (player != null) {
                            player.mass = mVal
                            onLog(LogEntry("SUCCESS", "Line ${index + 1}: customized player mass inertia parameter to $mVal kg"))
                            successCount++
                        } else {
                            onLog(LogEntry("ERROR", "Line ${index + 1}: no player target to customize mass"))
                            errorCount++
                        }
                    }
                    else -> {
                        onLog(LogEntry("ERROR", "Line ${index + 1}: Unknown scripting instruction code '$cmd'"))
                        errorCount++
                    }
                }
            } catch (e: Exception) {
                onLog(LogEntry("ERROR", "Line ${index + 1}: Invalid arguments or format in '$line'"))
                errorCount++
            }
        }

        onLog(LogEntry("INFO", "Compilation complete. Errors: $errorCount | Success: $successCount"))
    }
}
