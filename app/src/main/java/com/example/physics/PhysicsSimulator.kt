package com.example.physics

import kotlin.math.sqrt

class PhysicsSimulator {

    var objects = mutableListOf<PhysicsObject>()
    var gravity: Float = 0.5f
    var restitutionDefault: Float = 0.7f
    var airResistance: Float = 0.992f // dynamic friction

    fun resetToTemplate(templateName: String) {
        objects.clear()
        when (templateName) {
            "Obby Challenge" -> {
                gravity = 0.5f
                // Character sphere (controllable)
                objects.add(
                    PhysicsObject(
                        type = "circle",
                        x = 100f,
                        y = 100f,
                        radius = 28f,
                        mass = 1.0f,
                        colorHex = "#3399FF",
                        label = "Player",
                        logicTag = "player"
                    )
                )
                // Platforms
                objects.add(
                    PhysicsObject(
                        type = "box",
                        x = 150f,
                        y = 400f,
                        width = 160f,
                        height = 30f,
                        isStatic = true,
                        colorHex = "#26E6A5",
                        label = "Starting Platform"
                    )
                )
                // Lava Trap
                objects.add(
                    PhysicsObject(
                        type = "box",
                        x = 380f,
                        y = 420f,
                        width = 100f,
                        height = 20f,
                        isStatic = true,
                        colorHex = "#FF3366",
                        label = "Lava Hazard",
                        logicTag = "lava"
                    )
                )
                // Dynamic bouncing trampoline
                objects.add(
                    PhysicsObject(
                        type = "box",
                        x = 600f,
                        y = 350f,
                        width = 120f,
                        height = 25f,
                        isStatic = true,
                        colorHex = "#FFD700",
                        label = "Mega Boost Pad",
                        logicTag = "trampoline"
                    )
                )
                // Collectible star
                objects.add(
                    PhysicsObject(
                        type = "circle",
                        x = 600f,
                        y = 120f,
                        radius = 20f,
                        isStatic = true,
                        colorHex = "#FF66FF",
                        label = "Victory Star",
                        logicTag = "star"
                    )
                )
            }
            "Bouncy Blocks" -> {
                gravity = 0.3f
                // Player ball
                objects.add(
                    PhysicsObject(
                        type = "circle",
                        x = 200f,
                        y = 100f,
                        radius = 30f,
                        mass = 1.2f,
                        colorHex = "#FF4444",
                        label = "Bouncer",
                        logicTag = "player"
                    )
                )
                // Dynamic heavy cubes
                objects.add(
                    PhysicsObject(
                        type = "circle",
                        x = 240f,
                        y = 220f,
                        radius = 25f,
                        vy = 4f,
                        colorHex = "#E040FB",
                        label = "Cube 1"
                    )
                )
                objects.add(
                    PhysicsObject(
                        type = "circle",
                        x = 450f,
                        y = 150f,
                        radius = 35f,
                        vx = -2f,
                        colorHex = "#00E5FF",
                        label = "Cube 2"
                    )
                )
                // Slanted static base
                objects.add(
                    PhysicsObject(
                        type = "box",
                        x = 250f,
                        y = 500f,
                        width = 300f,
                        height = 40f,
                        isStatic = true,
                        restitution = 0.9f,
                        colorHex = "#76FF03"
                    )
                )
                objects.add(
                    PhysicsObject(
                        type = "box",
                        x = 550f,
                        y = 400f,
                        width = 250f,
                        height = 40f,
                        isStatic = true,
                        restitution = 1.0f,
                        colorHex = "#76FF03",
                        label = "Ultra Bounce"
                    )
                )
            }
            else -> { // Custom / Empty
                gravity = 0.5f
                objects.add(
                    PhysicsObject(
                        type = "circle",
                        x = 100f,
                        y = 100f,
                        radius = 25f,
                        colorHex = "#AF52DE",
                        label = "Developer Ball",
                        logicTag = "player"
                    )
                )
            }
        }
    }

    fun step(width: Float, height: Float, onEvent: (String, PhysicsObject) -> Unit) {
        val dt = 1.0f

        // 1. Apply gravity & velocities
        for (obj in objects) {
            if (obj.isCollected) continue

            if (!obj.isStatic) {
                // Apply global gravity
                obj.vy += gravity * dt
                // Apply drag
                obj.vx *= airResistance
                obj.vy *= airResistance
            }

            // Update position
            obj.x += obj.vx * dt
            obj.y += obj.objBoundY(dt)
        }

        // Help functions inside loop
        val player = objects.find { it.logicTag == "player" }

        // 2. Handle Boundary Collisions (Screen/Canvas limits)
        for (obj in objects) {
            if (obj.isCollected) continue

            val r = if (obj.type == "circle") obj.radius else obj.width / 2f
            val h = if (obj.type == "circle") obj.radius else obj.height / 2f

            // Floor boundary
            if (obj.y + h > height) {
                obj.y = height - h
                if (!obj.isStatic) {
                    obj.vy = -obj.vy * obj.restitution
                    // stop slides when super small
                    if (Math.abs(obj.vy) < 0.2f) obj.vy = 0f
                }
                if (obj.logicTag == "player") {
                    onEvent("hit_floor", obj)
                }
            }
            // Ceiling boundary
            if (obj.y - h < 0f) {
                obj.y = h
                if (!obj.isStatic) {
                    obj.vy = -obj.vy * obj.restitution
                }
            }
            // Left wall boundary
            if (obj.x - r < 0f) {
                obj.x = r
                if (!obj.isStatic) {
                    obj.vx = -obj.vx * obj.restitution
                }
            }
            // Right wall boundary
            if (obj.x + r > width) {
                obj.x = width - r
                if (!obj.isStatic) {
                    obj.vx = -obj.vx * obj.restitution
                }
            }
        }

        // 3. Resolve Sphere-to-Sphere & Box Overlaps & Rigid Collisions
        for (i in 0 until objects.size) {
            val objA = objects[i]
            if (objA.isCollected) continue

            for (j in i + 1 until objects.size) {
                val objB = objects[j]
                if (objB.isCollected) continue

                // Handle collisions based on types
                if (objA.type == "circle" && objB.type == "circle") {
                    resolveCircleToCircle(objA, objB, onEvent)
                } else {
                    // Box to Circle / Box to Box simplified collision normal checks
                    resolveBoxToCircle(objA, objB, onEvent)
                }
            }
        }
    }

    private fun PhysicsObject.objBoundY(dt: Float): Float {
        return vy * dt
    }

    private fun resolveCircleToCircle(
        a: PhysicsObject,
        b: PhysicsObject,
        onEvent: (String, PhysicsObject) -> Unit
    ) {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val distance = sqrt(dx * dx + dy * dy)
        val minDist = a.radius + b.radius

        if (distance < minDist && distance > 0.1f) {
            // Overlap depth
            val overlap = minDist - distance

            // Push apart proportional to static-ness
            if (!a.isStatic && !b.isStatic) {
                val pushX = (dx / distance) * (overlap / 2f)
                val pushY = (dy / distance) * (overlap / 2f)
                a.x -= pushX
                a.y -= pushY
                b.x += pushX
                b.y += pushY
            } else if (!a.isStatic) {
                a.x -= (dx / distance) * overlap
                a.y -= (dy / distance) * overlap
            } else if (!b.isStatic) {
                b.x += (dx / distance) * overlap
                b.y += (dy / distance) * overlap
            }

            // Normal vectors
            val nx = dx / distance
            val ny = dy / distance

            // Relative speed
            val rvx = a.vx - b.vx
            val rvy = a.vy - b.vy
            val velAlongNormal = rvx * nx + rvy * ny

            if (velAlongNormal < 0) {
                // Determine restitution representation
                val e = Math.min(a.restitution, b.restitution)
                var impulse = -(1f + e) * velAlongNormal
                val invMassA = if (a.isStatic) 0f else 1f / a.mass
                val invMassB = if (b.isStatic) 0f else 1f / b.mass

                impulse /= (invMassA + invMassB)

                if (!a.isStatic) {
                    a.vx += impulse * invMassA * nx
                    a.vy += impulse * invMassA * ny
                }
                if (!b.isStatic) {
                    b.vx -= impulse * invMassB * nx
                    b.vy -= impulse * invMassB * ny
                }
            }

            // Logic Check triggers
            checkTriggers(a, b, onEvent)
            checkTriggers(b, a, onEvent)
        }
    }

    private fun resolveBoxToCircle(
        box: PhysicsObject,
        circle: PhysicsObject,
        onEvent: (String, PhysicsObject) -> Unit
    ) {
        // Swap if type order is mismatched
        val b = if (box.type == "box" || box.type == "trampoline" || box.type == "lava") box else circle
        val c = if (box.type == "circle" || box.type == "star") box else circle

        if (b == c) return // Safe validation check

        // AABB - Circle collision math
        val halfW = b.width / 2f
        val halfH = b.height / 2f

        // Closest point on AABB to Circle center
        val closestX = Math.max(b.x - halfW, Math.min(c.x, b.x + halfW))
        val closestY = Math.max(b.y - halfH, Math.min(c.y, b.y + halfH))

        val distX = c.x - closestX
        val distY = c.y - closestY
        val distance = sqrt(distX * distX + distY * distY)

        if (distance < c.radius && distance > 0.01f) {
            val overlap = c.radius - distance

            // Normal direction
            val nx = distX / distance
            val ny = distY / distance

            // Push character away from physical blocker
            if (!c.isStatic) {
                c.x += nx * overlap
                c.y += ny * overlap

                // Bounce speed normal
                val velAlongNormal = c.vx * nx + c.vy * ny
                if (velAlongNormal < 0) {
                    val e = Math.max(b.restitution, c.restitution)
                    c.vx = c.vx - (1f + e) * velAlongNormal * nx
                    c.vy = c.vy - (1f + e) * velAlongNormal * ny
                }
            }

            // Custom Script / Custom Trigger Logic
            checkTriggers(b, c, onEvent)
        }
    }

    private fun checkTriggers(
        trigger: PhysicsObject,
        player: PhysicsObject,
        onEvent: (String, PhysicsObject) -> Unit
    ) {
        if (player.logicTag != "player") return

        if (trigger.logicTag == "trampoline" || trigger.type == "trampoline") {
            player.vy = -18f // Mega impulse upwards!
            onEvent("trampoline_launch", trigger)
        } else if (trigger.logicTag == "lava" || trigger.type == "lava") {
            onEvent("lava_death", trigger)
        } else if (trigger.logicTag == "star" || trigger.type == "star") {
            if (!trigger.isCollected) {
                trigger.isCollected = true
                onEvent("star_collected", trigger)
            }
        }
    }
}
