package com.example.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.db.AppRepository
import com.example.db.GameEntity
import com.example.db.MarketItemEntity
import com.example.db.MessageEntity
import com.example.db.UserProfileEntity
import com.example.physics.PhysicsObject
import com.example.physics.PhysicsSimulator
import com.example.script.LogEntry
import com.example.script.ScriptInterpreter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BloxViewModel(private val repository: AppRepository) : ViewModel() {

    // === REPETITIVE IN-GAME LOOPS ===
    val physicsSimulator = PhysicsSimulator()
    val scriptInterpreter = ScriptInterpreter()

    // Screen navigation tracking: "discover", "sandbox", "social", "market", "avatar"
    var currentTab by mutableStateOf("discover")
    var isPlayingActiveGame by mutableStateOf(false)
    var activeGameTitle by mutableStateOf("")

    // Score and life counters for active gameplay
    var score by mutableStateOf(0)
    var deathsCount by mutableStateOf(0)
    var physicsFeedbackText by mutableStateOf("Physics engine initialized. Welcome to the sandbox!")
    var canvasWidth by mutableStateOf(800f)
    var canvasHeight by mutableStateOf(500f)

    // === SANDBOX STATE ===
    var sandboxCodeInput by mutableStateOf(
        """// Roblox iOS 26 Developer Script
// Create unique physics models!
gravity 0.5
bounce 0.8
resistance 0.99
spawn circle #00FFCC 300 150
spawn box #FF00FF 400 350
"""
    )
    private val _compilerLogs = MutableStateFlow<List<LogEntry>>(
        listOf(
            LogEntry("INFO", "iOS 26 Compiler ready..."),
            LogEntry("INFO", "Written in OrbitScript 1.0 (LUA-compatible)")
        )
    )
    val compilerLogs = _compilerLogs.asStateFlow()

    // === DATABASE STATE BINDINGS ===
    val gamesList: StateFlow<List<GameEntity>> = repository.allGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messageLogs: StateFlow<List<MessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketplaceItems: StateFlow<List<MarketItemEntity>> = repository.allMarketItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Local cached copy for instant edits
    var testProfile by mutableStateOf(UserProfileEntity())

    // Active physics objects flow to prevent high-frequency compose glitches
    private val _activeObjects = MutableStateFlow<List<PhysicsObject>>(emptyList())
    val activeObjects: StateFlow<List<PhysicsObject>> = _activeObjects.asStateFlow()

    // Simulation toggle
    var isSimulating by mutableStateOf(false)

    init {
        // Pre-populate databases with default user-generated Roblox entries
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                if (profile == null) {
                    val defaultProf = UserProfileEntity()
                    repository.saveUserProfile(defaultProf)
                    testProfile = defaultProf
                } else {
                    testProfile = profile
                }
            }
        }

        viewModelScope.launch {
            repository.allGames.collect { games ->
                if (games.isEmpty()) {
                    // pre-load starter templates
                    repository.insertGame(
                        GameEntity(
                            title = "Cyber City Obby",
                            description = "Leap over neon red laser logs, jump on trampoline pads, hit the target star! Made by MasterDev26.",
                            creatorName = "MasterDev26",
                            plays = 4850,
                            physicsData = "Obby Challenge",
                            scriptCode = ""
                        )
                    )
                    repository.insertGame(
                        GameEntity(
                            title = "Chaotic Bounce Fields",
                            description = "High density bouncy cages with rigid-body elastic physics. Watch spheres fly. Custom script applied.",
                            creatorName = "GravityHacker",
                            plays = 2410,
                            physicsData = "Bouncy Blocks",
                            scriptCode = ""
                        )
                    )
                    repository.insertGame(
                        GameEntity(
                            title = "Adopt Me Lite",
                            description = "Adopt adorable neon blocky pets, custom trade accessories with online players in Roblox Kids!",
                            creatorName = "DreamCraft",
                            plays = 95200,
                            physicsData = "Bouncy Blocks",
                            scriptCode = ""
                        )
                    )
                    repository.insertGame(
                        GameEntity(
                            title = "Brookhaven Mini",
                            description = "Roleplay inside customizable mansions. Drive fast hovercars. Verified for Roblox Select.",
                            creatorName = "Wolfpaq",
                            plays = 87400,
                            physicsData = "Obby Challenge",
                            scriptCode = ""
                        )
                    )
                    repository.insertGame(
                        GameEntity(
                            title = "Piggy Escape",
                            description = "Dodge the eerie pink bat-wielding chaser through locked puzzle doors. Highly interactive survival.",
                            creatorName = "MiniToon",
                            plays = 32100,
                            physicsData = "Obby Challenge",
                            scriptCode = ""
                        )
                    )
                    repository.insertGame(
                        GameEntity(
                            title = "Murder Mystery 26",
                            description = "Decipher the hidden roles: Sheriff, Murderer, or Innocent. Trade cyber knives instantly.",
                            creatorName = "Nikilis",
                            plays = 44500,
                            physicsData = "Bouncy Blocks",
                            scriptCode = ""
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.allMarketItems.collect { items ->
                if (items.isEmpty()) {
                    repository.insertMarketItems(
                        listOf(
                            MarketItemEntity(name = "Cyber Chrono Cap", creator = "DevSpace", price = 350, styleType = "cap", colorHex = "#FF4D4D", purchaseCount = 120),
                            MarketItemEntity(name = "Hologram Aura Wings", creator = "AuraStudios", price = 800, styleType = "wings", colorHex = "#AF52DE", purchaseCount = 95),
                            MarketItemEntity(name = "Aurora Glow Jacket", creator = "VividCorp", price = 450, styleType = "shirt", colorHex = "#00FFCC", purchaseCount = 190),
                            MarketItemEntity(name = "Classic Retro Pants", creator = "System", price = 100, styleType = "pant", colorHex = "#26E6A5", purchaseCount = 310),
                            MarketItemEntity(name = "Futuristic Matrix Mask", creator = "NeonForge", price = 600, styleType = "face", colorHex = "#FFC107", purchaseCount = 74)
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.allMessages.collect { messages ->
                if (messages.isEmpty()) {
                    repository.insertMessage(MessageEntity(sender = "System", recipient = "", text = "Welcome to BloxCraft global developer chatroom!"))
                    repository.insertMessage(MessageEntity(sender = "MasterDev26", recipient = "", text = "Yo, I just uploaded the Cyber City Obby. Try script 'gravity 0.2' to jump super high!"))
                    repository.insertMessage(MessageEntity(sender = "GravityHacker", recipient = "", text = "Who wants to trade retro items? Need the Aura Wings."))
                }
            }
        }

        // Start Physics Engine Update Loop
        startPhysicsGameLoop()
    }

    private fun startPhysicsGameLoop() {
        viewModelScope.launch {
            while (true) {
                if (isSimulating || isPlayingActiveGame) {
                    physicsSimulator.step(canvasWidth, canvasHeight) { eventType, obj ->
                        handlePhysicsEvent(eventType, obj)
                    }
                    _activeObjects.value = physicsSimulator.objects.toList()
                }
                delay(16) // ~60fps
            }
        }
    }

    private fun handlePhysicsEvent(eventType: String, obj: PhysicsObject) {
        when (eventType) {
            "trampoline_launch" -> {
                physicsFeedbackText = "🚀 Rocket Leap! Elastic boost applied by: ${obj.label}"
            }
            "lava_death" -> {
                deathsCount++
                physicsFeedbackText = "💥 Vaporized in lava! Respawned at starting checkpoint."
                resetControllablePlayer()
            }
            "star_collected" -> {
                score++
                physicsFeedbackText = "⭐ Victory! Collected gold Star: ${obj.label}. Score: $score"
            }
            "hit_floor" -> {
                // soft roll
            }
        }
    }

    // Controls character jumping / lateral movement
    fun moveCharacter(dirX: Float, jump: Boolean = false) {
        val player = physicsSimulator.objects.find { it.logicTag == "player" } ?: return
        if (dirX != 0f) {
            player.vx = dirX * 6.5f
        }
        if (jump) {
            // Apply a sudden impulse update on gravity
            player.vy = -12.5f
            physicsFeedbackText = "🏃 Jump executed! Normal gravity: ${physicsSimulator.gravity}"
        }
        _activeObjects.value = physicsSimulator.objects.toList()
    }

    fun stopCharacter() {
        val player = physicsSimulator.objects.find { it.logicTag == "player" } ?: return
        player.vx = 0f
        _activeObjects.value = physicsSimulator.objects.toList()
    }

    fun resetControllablePlayer() {
        val player = physicsSimulator.objects.find { it.logicTag == "player" }
        if (player != null) {
            player.x = 100f
            player.y = 100f
            player.vx = 0f
            player.vy = 0f
        } else {
            physicsSimulator.objects.add(
                PhysicsObject(
                    type = "circle",
                    x = 100f,
                    y = 100f,
                    radius = 28f,
                    colorHex = "#3399FF",
                    label = "Player",
                    logicTag = "player"
                )
            )
        }
        _activeObjects.value = physicsSimulator.objects.toList()
    }

    // === SANDBOX ACTION HANDLERS ===
    fun triggerDeveloperCompile() {
        _compilerLogs.value = listOf(LogEntry("INFO", "Initializing OrbitScript VM..."))
        scriptInterpreter.execute(sandboxCodeInput, physicsSimulator) { log ->
            val updated = _compilerLogs.value.toMutableList()
            updated.add(log)
            _compilerLogs.value = updated
        }
        physicsFeedbackText = "OrbitScript compiled successfully. UGC additions loaded!"
        _activeObjects.value = physicsSimulator.objects.toList()
    }

    fun setSandboxPreset(presetName: String) {
        physicsSimulator.resetToTemplate(presetName)
        resetControllablePlayer()
        _activeObjects.value = physicsSimulator.objects.toList()
        physicsFeedbackText = "Loaded preset template: $presetName"
    }

    fun addCustomUgcObject(type: String, colorHex: String) {
        val newObj = PhysicsObject(
            type = if (type == "trampoline" || type == "lava") "box" else type,
            x = 300f + (0..150).random(),
            y = 120f + (0..100).random(),
            colorHex = colorHex,
            isStatic = type != "circle" && type != "box",
            label = "Placed_${type.uppercase()}",
            logicTag = if (type == "trampoline" || type == "lava" || type == "star") type else ""
        )
        physicsSimulator.objects.add(newObj)
        _activeObjects.value = physicsSimulator.objects.toList()
        physicsFeedbackText = "Added custom placed $type block on canvas!"
    }

    fun publishSandboxAsGame(title: String, description: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            // serialize custom objects
            val serializedList = physicsSimulator.objects.map { it.toDataString() }.joinToString(";")
            repository.insertGame(
                GameEntity(
                    title = title,
                    description = description,
                    creatorName = testProfile.username,
                    physicsData = serializedList,
                    scriptCode = sandboxCodeInput
                )
            )
            physicsFeedbackText = "🎉 Successfully published '$title' globally! Plays set to 0."
        }
    }

    // === GAME DISCOVER PLAY ===
    fun launchGame(game: GameEntity) {
        isPlayingActiveGame = true
        activeGameTitle = game.title
        score = 0
        deathsCount = 0

        if (game.physicsData == "Obby Challenge" || game.physicsData == "Bouncy Blocks") {
            physicsSimulator.resetToTemplate(game.physicsData)
        } else {
            // Deserialization of custom items
            physicsSimulator.objects.clear()
            val objs = game.physicsData.split(";").mapNotNull { PhysicsObject.fromDataString(it) }
            physicsSimulator.objects.addAll(objs)
        }
        resetControllablePlayer()

        if (game.scriptCode.isNotEmpty()) {
            sandboxCodeInput = game.scriptCode
            triggerDeveloperCompile()
        }

        isSimulating = true
        _activeObjects.value = physicsSimulator.objects.toList()
    }

    fun leaveGame() {
        isPlayingActiveGame = false
        isSimulating = false
    }

    // === SOCIAL & MESSAGING ===
    fun sendChatMessage(text: String, recipientName: String = "") {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.insertMessage(
                MessageEntity(
                    sender = testProfile.username,
                    recipient = recipientName,
                    text = text
                )
            )
        }
    }

    fun deleteGame(id: Int) {
        viewModelScope.launch { repository.deleteGame(id) }
    }

    fun clearAllChats() {
        viewModelScope.launch {
            repository.clearMessages()
            repository.insertMessage(MessageEntity(sender = "System", recipient = "", text = "Chat feed flushed by local development sandbox."))
        }
    }

    fun joinSocialGroup(groupName: String) {
        val groups = testProfile.joinedGroups.split(",").toMutableList()
        if (!groups.contains(groupName)) {
            groups.add(groupName)
            val updatedProfile = testProfile.copy(joinedGroups = groups.joinToString(","))
            viewModelScope.launch {
                repository.saveUserProfile(updatedProfile)
                testProfile = updatedProfile
            }
            physicsFeedbackText = "Joined Group: $groupName!"
        }
    }

    // === MARKETPLACE BUY & TRADE ===
    fun buyMarketplaceItem(item: MarketItemEntity): String {
        if (testProfile.tokenBalance < item.price) {
            return "Insufficient funds. Needs ${item.price} BT."
        }

        val balance = testProfile.tokenBalance - item.price
        val equipped = testProfile.equippedItems.split(",").toMutableList()
        // Replace existing type if exists
        equipped.removeAll { it.startsWith("${item.styleType}:") }
        equipped.add("${item.styleType}:${item.colorHex}")

        val updatedProfile = testProfile.copy(
            tokenBalance = balance,
            equippedItems = equipped.joinToString(",")
        )

        viewModelScope.launch {
            repository.saveUserProfile(updatedProfile)
            // Increment seller purchase counter
            repository.insertMarketItem(item.copy(purchaseCount = item.purchaseCount + 1))
            testProfile = updatedProfile
        }

        return "Successfully obtained ${item.name}! Equipped in wardrobe."
    }

    fun sellUgcVirtualItem(name: String, price: Int, type: String, colorHex: String) {
        if (name.isBlank()) return
        val newItem = MarketItemEntity(
            name = name,
            creator = testProfile.username,
            price = price,
            styleType = type,
            colorHex = colorHex,
            isUgc = true
        )
        viewModelScope.launch {
            repository.insertMarketItem(newItem)
        }
        physicsFeedbackText = "Listed custom UGC item '$name' in virtual catalog!"
    }

    fun editProfileState(status: String) {
        val updated = testProfile.copy(statusText = status)
        viewModelScope.launch {
            repository.saveUserProfile(updated)
            testProfile = updated
        }
    }

    // === ROBLOX SPECIFIC ADDITIONS ===

    // Robux package buying
    val robuxPackages = listOf(
        RobuxPackage(80, 0.99),
        RobuxPackage(400, 4.99),
        RobuxPackage(800, 9.99),
        RobuxPackage(2000, 24.99),
        RobuxPackage(4500, 49.99),
        RobuxPackage(10000, 99.99)
    )

    fun buyRobuxPackage(pkg: RobuxPackage) {
        val updated = testProfile.copy(robuxBalance = testProfile.robuxBalance + pkg.robuxAmount)
        viewModelScope.launch {
            repository.saveUserProfile(updated)
            testProfile = updated
            addLocalNotification(
                "Robux Purchased!",
                "Successfully added ${pkg.robuxAmount} Robux. Your balance is now: ${updated.robuxBalance} R$."
            )
            sendChatMessage("I just bought ${pkg.robuxAmount} Robux! 🚀")
        }
    }

    // Avatar configuration updates
    fun updateAvatarPart(partType: String, partValue: String) {
        val updated = when (partType) {
            "body" -> testProfile.copy(avatarBodyType = partValue)
            "hair" -> testProfile.copy(avatarHair = partValue)
            "face" -> testProfile.copy(avatarFace = partValue)
            "clothing" -> testProfile.copy(avatarClothing = partValue)
            "accessory" -> testProfile.copy(avatarAccessory = partValue)
            else -> testProfile
        }
        viewModelScope.launch {
            repository.saveUserProfile(updated)
            testProfile = updated
            physicsFeedbackText = "Equipped $partType: $partValue"
        }
    }

    // Fast Prefabs loader: "noob", "bacon"
    fun applyCharacterPreset(presetName: String) {
        val updated = if (presetName.lowercase() == "noob") {
            testProfile.copy(
                avatarBodyType = "Noob",
                avatarHair = "Classic Square",
                avatarFace = "Classic Smile",
                avatarClothing = "Noob Hoodie",
                avatarAccessory = "None"
            )
        } else {
            // Bacon Boy
            testProfile.copy(
                avatarBodyType = "Bacon Boy",
                avatarHair = "Bacon Sprouts",
                avatarFace = "Classic Smile",
                avatarClothing = "Bacon Jacket",
                avatarAccessory = "Aura Wings"
            )
        }

        viewModelScope.launch {
            repository.saveUserProfile(updated)
            testProfile = updated
            addLocalNotification(
                "Character Character Loaded",
                "Loaded ${presetName.capitalize()} preset styles to database."
            )
            physicsFeedbackText = "Applied Roblox ${presetName.capitalize()} preset configuration."
        }
    }

    // Age restrictions & dynamic filtering rating
    fun updateUserAge(newAge: Int) {
        val fixedAge = newAge.coerceIn(1, 120)
        val updated = testProfile.copy(age = fixedAge)
        viewModelScope.launch {
            repository.saveUserProfile(updated)
            testProfile = updated
            val level = when {
                fixedAge <= 9 -> "Roblox Kids"
                fixedAge in 10..17 -> "Roblox Select"
                else -> "Normal Roblox"
            }
            addLocalNotification(
                "Verification Updated",
                "Your age is verified at $fixedAge. Dynamic routing assigned you to: $level servers."
            )
        }
    }

    // Game Passes list per game (Simulate mapping: gameTitle -> list of passes)
    fun getGamepassesForGame(gameTitle: String): List<GamepassItem> {
        return when (gameTitle) {
            "Cyber City Obby" -> listOf(
                GamepassItem("Speed Coil", "Increases your character's linear walk velocity in-game.", 150, "Cyber City Obby"),
                GamepassItem("High Jump Boot", "Grants jump force upgrade by modifying gravity parameters.", 250, "Cyber City Obby"),
                GamepassItem("Auto Respawn Shield", "Automatically regenerates your character position in real-time.", 350, "Cyber City Obby")
            )
            "Chaotic Bounce Fields" -> listOf(
                GamepassItem("Anti-Gravity Module", "Drastically lowers local system coefficient of gravity.", 200, "Chaotic Bounce Fields"),
                GamepassItem("Explosive Spawner", "Instantly spawn highly elastic boxes on active simulation canvas.", 300, "Chaotic Bounce Fields")
            )
            "Adopt Me Lite" -> listOf(
                GamepassItem("Fly Ride Potion", "Unlocks flight abilities for pets.", 400, "Adopt Me Lite"),
                GamepassItem("VIP Mansion Key", "Unlocks luxury residential templates.", 500, "Adopt Me Lite")
            )
            "Brookhaven Mini" -> listOf(
                GamepassItem("VIP Luxury Pass", "Gives premium badge verification and custom high-speed cars.", 300, "Brookhaven Mini"),
                GamepassItem("Roblox House Permit", "Unlocks unrestricted sandbox residential plots.", 200, "Brookhaven Mini")
            )
            "Piggy Escape" -> listOf(
                GamepassItem("Detector Tool", "Sounds audible radar warnings when entities come nearby.", 100, "Piggy Escape"),
                GamepassItem("Invisibility Cloak", "Renders player immune to damage traps for 10 seconds.", 400, "Piggy Escape")
            )
            "Murder Mystery 26" -> listOf(
                GamepassItem("Sheriff Star Radar", "Highlights the detective role in glowing real-time hues.", 150, "Murder Mystery 26"),
                GamepassItem("Radio Music Boombox", "Play custom sound effects and sound loops globally.", 100, "Murder Mystery 26")
            )
            else -> listOf(
                GamepassItem("Custom Developer Medal", "Displays custom builder badges inside chat channels.", 50, gameTitle)
            )
        }
    }

    // Buying game passes with Roblox
    fun buyGamepass(gameTitle: String, gp: GamepassItem): String {
        if (testProfile.robuxBalance < gp.robuxPrice) {
            return "❌ Critical: Need ${gp.robuxPrice} Robux to buy ${gp.name}."
        }

        val gpId = "${gameTitle}:${gp.name}"
        val currentUnlocks = testProfile.unlockedGamepasses.split(",").toMutableList()
        if (currentUnlocks.contains(gpId)) {
            return "⚠️ Notice: You already own the Game Pass '${gp.name}'."
        }

        currentUnlocks.add(gpId)
        val balance = testProfile.robuxBalance - gp.robuxPrice
        val updated = testProfile.copy(
            robuxBalance = balance,
            unlockedGamepasses = currentUnlocks.joinToString(",")
        )

        viewModelScope.launch {
            repository.saveUserProfile(updated)
            testProfile = updated
            addLocalNotification(
                "Game Pass Unlocked",
                "Paid ${gp.robuxPrice} R$ for '${gp.name}' in $gameTitle. Applied directly!"
            )
            sendChatMessage("Just unlocked the [${gp.name}] dynamic pass inside $gameTitle! 💎")
        }
        return "✅ Bought ${gp.name} successfully! Roblox balance adjusted."
    }

    fun hasGamepass(gameTitle: String, gpName: String): Boolean {
        val target = "$gameTitle:$gpName"
        return testProfile.unlockedGamepasses.split(",").contains(target)
    }

    // Notifications state
    var notificationHistory by mutableStateOf(listOf(
        NotificationItem("Welcome Gear Added", "450 starting Robux were successfully deposited into your wallet.", System.currentTimeMillis() - 50000),
        NotificationItem("Secure Sandbox Verified", "Interactive anti-cheat algorithms successfully loaded into virtual system memory.", System.currentTimeMillis() - 1500000)
    ))

    fun addLocalNotification(title: String, details: String) {
        notificationHistory = listOf(NotificationItem(title, details, System.currentTimeMillis())) + notificationHistory
    }

    // Voice Chat State
    var isVoiceChatMuted by mutableStateOf(false)
    var isVoiceChatConnected by mutableStateOf(false)
    var voiceChatSensitivity by mutableStateOf(0.75f)
    var voiceChatChannelValue by mutableStateOf("Global Voice Server #1")
    var mockSpeakingActivity by mutableStateOf(0.12f)

    var voiceSpeakers by mutableStateOf(listOf(
        VoiceSpeaker("BaconSprouts_Cool", isSpeaking = true, volume = 0.8f),
        VoiceSpeaker("ClassicNoob_99", isSpeaking = false, volume = 0.7f),
        VoiceSpeaker("StudioOwner_26", isSpeaking = true, volume = 0.9f)
    ))

    init {
        // Run interactive background loop to wobble speaking activity
        viewModelScope.launch {
            while (true) {
                if (isVoiceChatConnected && !isVoiceChatMuted) {
                    mockSpeakingActivity = (0.05f + kotlin.random.Random.nextFloat() * 0.8f) * voiceChatSensitivity
                    // Wobble speakers talking state
                    voiceSpeakers = voiceSpeakers.map {
                        it.copy(isSpeaking = kotlin.random.Random.nextInt(100) > 40)
                    }
                } else {
                    mockSpeakingActivity = 0f
                }
                delay(800)
            }
        }
    }

    fun toggleVoiceConnection() {
        isVoiceChatConnected = !isVoiceChatConnected
        physicsFeedbackText = if (isVoiceChatConnected) {
            "Voice Engine Connected to ${voiceChatChannelValue}."
        } else {
            "Voice Engine Disconnected."
        }
    }

    fun toggleVoiceMute() {
        isVoiceChatMuted = !isVoiceChatMuted
    }

    // Anti cheat Hacker Banning System
    var reportedHackers by mutableStateOf(listOf(
        HackerReport("LuaScriptSpammer_00", "Injecting 'infinite jump' via exploit tool", "Active", "Cyber City Obby"),
        HackerReport("BaconFly_36", "Speedhack injection (>150 pixels per tick)", "Active", "Adopt Me Lite"),
        HackerReport("SpeedyGamer_iOS26", "Lava damage bypass (anti-death)", "Active", "Piggy Escape")
    ))

    var isScanningHackers by mutableStateOf(false)
    var anticheatLog by mutableStateOf("Roblox iOS 26 Anticheat standing guard... No active anomalies detected.")

    fun triggerAnticheatSweep() {
        viewModelScope.launch {
            isScanningHackers = true
            anticheatLog = "🚨 Scanning thread #26 starting... Reading TCP server nodes..."
            delay(1000)
            anticheatLog = "🔍 Analyzing player velocity coordinates... High packet frequency found on 'BaconFly_36'."
            delay(1000)
            anticheatLog = "🛡️ Injecting anticheat check payload... Packet signatures mismatched!"
            delay(800)
            anticheatLog = "⚡ Sweep complete. Automatic ban recommendations compiled."
            isScanningHackers = false
        }
    }

    fun banHacker(playerName: String) {
        reportedHackers = reportedHackers.filter { it.username != playerName }
        anticheatLog = "🔨 BANHAMMER DELIVERED: User '$playerName' was permanently banished from all Roblox virtual game instances!"
        addLocalNotification("Hacker Terminated", "User '$playerName' was successfully moderated in real-time.")
        sendChatMessage("Anticheat system banned exploiter player '$playerName'! 🛡️")
    }
}

// === DATA SPECIFICATION SCHEMAS ===

data class VoiceSpeaker(
    val username: String,
    val isSpeaking: Boolean = false,
    val volume: Float = 0.8f
)

data class NotificationItem(
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class HackerReport(
    val username: String,
    val reason: String,
    val status: String,
    val reportingGame: String
)

data class RobuxPackage(
    val robuxAmount: Int,
    val dollarPrice: Double
)

data class GamepassItem(
    val name: String,
    val description: String,
    val robuxPrice: Int,
    val gameTitle: String
)

class BloxViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BloxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BloxViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
