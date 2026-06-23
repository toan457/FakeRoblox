package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.db.GameEntity
import com.example.db.MarketItemEntity
import com.example.db.MessageEntity
import com.example.db.UserProfileEntity
import com.example.physics.PhysicsObject
import com.example.viewmodel.BloxViewModel
import kotlinx.coroutines.launch

@Composable
fun MainLayout(viewModel: BloxViewModel) {
    val activeTab = viewModel.currentTab
    val activeProfile by viewModel.activeProfile.collectAsState()

    var showVerificationDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    // Retrieve generated image resources
    val homeBannerRes = com.example.R.drawable.img_home_banner_1782191396398
    val gameHobbyRes = com.example.R.drawable.img_game_hobby_1782191412027
    val avatarRoboRes = com.example.R.drawable.img_avatar_robo_1782191423320
    val marketplaceBadgeRes = com.example.R.drawable.img_marketplace_badge_1782191435011

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080D))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Futuristic Aurora Orbs drawn on background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1B00FFCC), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = 600f
                ),
                radius = 600f,
                center = Offset(0f, 0f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1B980FEE), Color.Transparent),
                    center = Offset(size.width, size.height * 0.6f),
                    radius = 700f
                ),
                radius = 700f,
                center = Offset(size.width, size.height * 0.6f)
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // === iOS 26 TOP DYNAMIC STATUS ISLANDBAR ===
            DynamicStatusIslandBar(
                viewModel = viewModel,
                activeProfile = activeProfile,
                onTriggerVerify = { showVerificationDialog = true },
                onTriggerNotif = { showNotificationDialog = true }
            )

            // Dynamic Main Tab Router Frame with slide in animations
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    "discover" -> DiscoverTab(viewModel, homeBannerRes, gameHobbyRes)
                    "sandbox" -> SandboxTab(viewModel, avatarRoboRes)
                    "social" -> SocialTab(viewModel)
                    "market" -> MarketplaceTab(viewModel, marketplaceBadgeRes)
                    "avatar" -> AvatarTab(viewModel, avatarRoboRes)
                }
            }

            // === iOS 26 GLASS bottom navigation bar ===
            GlassBottomNavigation(viewModel)
        }

        // Fullscreen Overlay Play Mode (When playing an active user sandbox obby)
        AnimatedVisibility(
            visible = viewModel.isPlayingActiveGame,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            FullscreenPhysicsGameWindow(viewModel)
        }

        // === OVERLAY DIALOG: AGE VERIFICATION SYSTEM ===
        if (showVerificationDialog && activeProfile != null) {
            var tempAge by remember { mutableStateOf(activeProfile!!.age) }
            AlertDialog(
                onDismissRequest = { showVerificationDialog = false },
                title = { Text("🛡️ Roblox ID Age Verification", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Roblox curates multiplayer lobbies and limits scripts based on your age category rating to ensure secure sandbox play.",
                            color = Color(0xFFA5A9BC),
                            fontSize = 12.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Current Settings Age:", color = Color.LightGray, fontSize = 13.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { if (tempAge > 1) tempAge-- },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text("-", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "$tempAge",
                                    color = Color(0xFF00FFCC),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                )
                                Button(
                                    onClick = { if (tempAge < 100) tempAge++ },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Target Routing display
                        val targetServer = when {
                            tempAge <= 9 -> "Roblox Kids Server"
                            tempAge in 10..17 -> "Roblox Select Server"
                            else -> "Normal Roblox General Server"
                        }
                        val targetColor = when {
                            tempAge <= 9 -> Color(0xFF00FFCC)
                            tempAge in 10..17 -> Color(0xFFFF9500)
                            else -> Color(0xFFBF5AF2)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(targetColor.copy(alpha = 0.15f))
                                .border(1.dp, targetColor, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text("Assigned Channel: $targetServer", color = targetColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.updateUserAge(tempAge)
                            showVerificationDialog = false
                        }
                    ) {
                        Text("SAVE & CONFIRM", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showVerificationDialog = false }) {
                        Text("CANCEL", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF131622)
            )
        }

        // === OVERLAY DIALOG: NOTIFICATION HUB ===
        if (showNotificationDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationDialog = false },
                title = { Text("🔔 Systems Notification Logs", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Recent platform system logs and real-time moderator notifications:", color = Color.Gray, fontSize = 11.sp)
                        Box(
                            modifier = Modifier
                                .height(220.dp)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(viewModel.notificationHistory) { alert ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1D2132))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(alert.title, color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text("Live", color = Color(0xFFFF3366), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(alert.text, color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNotificationDialog = false }) {
                        Text("DISMISS", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color(0xFF10131E)
            )
        }
    }
}

@Composable
fun DynamicStatusIslandBar(
    viewModel: BloxViewModel,
    activeProfile: UserProfileEntity?,
    onTriggerVerify: () -> Unit,
    onTriggerNotif: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xCC0E1019))
            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onTriggerVerify() }) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00FFCC))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = activeProfile?.username ?: "Robloxian_2026",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Age routing displays badge
                    val age = activeProfile?.age ?: 13
                    val ageLevel = when {
                        age <= 9 -> "Roblox Kids"
                        age in 10..17 -> "Roblox Select"
                        else -> "Normal Roblox"
                    }
                    Text(
                        text = "Age: $age ($ageLevel)",
                        color = Color(0xFF00FFCC),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // High Tech wallet & notification bell
            Row(verticalAlignment = Alignment.CenterVertically) {
                // R$ Robux count display Capsule
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x40FFD60A))
                        .border(1.dp, Color(0xFFFFD60A).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.currentTab = "market" }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Robux Vault",
                        tint = Color(0xFFFFD60A),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${activeProfile?.robuxBalance ?: 450} R$",
                        color = Color(0xFFFFD60A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Notification Bell with Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x1AFFFFFF))
                        .clickable { onTriggerNotif() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Bell Logs",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    // Badge circle notification trigger
                    if (viewModel.notificationHistory.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF2D55))
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoverTab(viewModel: BloxViewModel, bannerRes: Int, hobbyRes: Int) {
    val games by viewModel.gamesList.collectAsState()
    var selectedGameForDetails by remember { mutableStateOf<GameEntity?>(null) }
    val activeProfile by viewModel.activeProfile.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // High Resolution Cyber banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(id = bannerRes),
                        contentDescription = "Roblox iOS 26 Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // glass gradient panel on top
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xDD07080D))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF2D55))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("MULTIPLAYER 2.0", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Roblox: Cloud Engine 2026",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Featured Roblox Multiplayers",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(games) { game ->
                GlassGameCard(game = game, itemImage = hobbyRes) {
                    selectedGameForDetails = game
                }
            }

            item {
                MultiplayerCrossplayStatusCard()
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // === OVERLAY SHEET: GAME DETAILS & SELECTABLE GAME PASS SHOP ===
        if (selectedGameForDetails != null) {
            val game = selectedGameForDetails!!
            val passes = viewModel.getGamepassesForGame(game.title)
            val age = activeProfile?.age ?: 13
            val userClass = when {
                age <= 9 -> "Roblox Kids Server"
                age in 10..17 -> "Roblox Select Server"
                else -> "Normal Roblox Server"
            }
            val serverColor = when {
                age <= 9 -> Color(0xFF00FFCC)
                age in 10..17 -> Color(0xFFFF9500)
                else -> Color(0xFFBF5AF2)
            }

            AlertDialog(
                onDismissRequest = { selectedGameForDetails = null },
                title = {
                    Column {
                        Text(game.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("Interactive Sandbox Simulation", color = Color.Gray, fontSize = 11.sp)
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        // Game Description
                        Text(game.description, color = Color.LightGray, fontSize = 12.sp)

                        // Age Routing Server Indicator
                        Text("Live Server Assignment Status", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(serverColor.copy(alpha = 0.15f))
                                .border(1.dp, serverColor, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(serverColor))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Connected to: $userClass (Level Verification Clear)",
                                    color = serverColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Game Pass Shop title
                        Text("🎟️ Roblox Game Passes Shop (At least 1 per Game)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        for (pass in passes) {
                            val isUnlocked = viewModel.hasGamepass(game.title, pass.name)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF1D2132))
                                    .border(1.dp, if (isUnlocked) Color(0xFF00FFCC).copy(alpha = 0.4f) else Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pass.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(pass.description, color = Color(0xFFA5A9BC), fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (isUnlocked) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0x3300FFCC))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Unlocked ✓", color = Color(0xFF00FFCC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.buyGamepass(game.title, pass) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD60A)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("${pass.robuxPrice} R$", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.launchGame(game)
                            selectedGameForDetails = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, "Start", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("LAUNCH SERVER", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedGameForDetails = null }) {
                        Text("CLOSE", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                },
                containerColor = Color(0xFF131622)
            )
        }
    }
}

@Composable
fun GlassGameCard(game: GameEntity, itemImage: Int, onPlay: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x66161A26))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
            .clickable { onPlay() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = itemImage),
                contentDescription = "Game preview",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = game.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "by ${game.creatorName}",
                    color = Color(0xFFA2A5B5),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = game.description,
                    color = Color(0xFFC0C3CC),
                    fontSize = 11.sp,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Plays",
                        tint = Color(0xFF00FFCC),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${game.plays} Virtual Sessions",
                        color = Color(0xFF00FFCC),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x3300FFCC))
                    .border(1.dp, Color(0xFF00FFCC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color(0xFF00FFCC)
                )
            }
        }
    }
}

@Composable
fun MultiplayerCrossplayStatusCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x331E1E1E))
            .border(1.dp, Color(0x1F980FEE), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFBF5AF2))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cross-Platform Infrastructure Status",
                    color = Color(0xFFBF5AF2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                ServerNodeCapsule("iOS/Android Host", "14ms", Color(0xFF00FFCC))
                ServerNodeCapsule("Windows Desktop", "16ms", Color(0xFF00FFCC))
                ServerNodeCapsule("macOS client", "22ms", Color(0xFFBF5AF2))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cloud databases synchronization validated via SHA-512 TCP Handshake security protocol with JWT key management.",
                color = Color(0xFFA2A5B5),
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
fun ServerNodeCapsule(platform: String, ping: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x1F2B2B38))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(platform, fontSize = 9.sp, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(ping, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SandboxTab(viewModel: BloxViewModel, avatarRes: Int) {
    var title by remember { mutableStateOf("My Epic Obby") }
    var desc by remember { mutableStateOf("Enter and bounce past lava loops!") }
    val compilerLogs by viewModel.compilerLogs.collectAsState()

    var showPublishSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "UGC Physics Creator Studio",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Build elements, adjust physical bounciness & script triggers in real-time.",
                color = Color(0xFFA2A5B5),
                fontSize = 11.sp
            )
        }

        item {
            // Preset selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.setSandboxPreset("Obby Challenge") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF5AF2)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Obby Challenge Preset", fontSize = 11.sp)
                }
                Button(
                    onClick = { viewModel.setSandboxPreset("Bouncy Blocks") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Bouncy Blocks", color = Color.Black, fontSize = 11.sp)
                }
            }
        }

        item {
            // Script code editing terminal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0B0D14))
                    .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📜 Lua-Script: physics_engine.lua",
                            color = Color(0xFF00FFCC),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33FFD60A))
                                .clickable {
                                    viewModel.sandboxCodeInput = """// Reset gravity values
gravity 0.25
bounce 0.95
resistance 0.995
spawn trampoline #FFFF00 500 400
spawn lava #FF1493 350 450
"""
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Default Script", color = Color(0xFFFFD60A), fontSize = 9.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = viewModel.sandboxCodeInput,
                        onValueChange = { viewModel.sandboxCodeInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0E1019),
                            unfocusedContainerColor = Color(0xFF0E1019),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        placeholder = { Text("Write logic script here...", color = Color.Gray) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.triggerDeveloperCompile() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("COMPILE & EXECUTE SCRIPTS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            // Compiler Logs Panel (Terminal)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black)
                    .padding(8.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            "--- iOS 26 DECOMPILER TERMINAL INPUT ---",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    items(compilerLogs) { log ->
                        val color = when (log.type) {
                            "ERROR" -> Color(0xFFFF2D55)
                            "SUCCESS" -> Color(0xFF00FFCC)
                            else -> Color(0xFFCBD2E1)
                        }
                        Text(
                            text = "[${log.type}] ${log.msg}",
                            color = color,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        item {
            // Spawning Blocks Quickly area
            Text("Instant Block Spawner Panel", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BoxSpawnerButton("Trampoline", "#FFC107", Modifier.weight(1f)) {
                    viewModel.addCustomUgcObject("trampoline", "#FFC107")
                }
                BoxSpawnerButton("Lava Block", "#FF3366", Modifier.weight(1f)) {
                    viewModel.addCustomUgcObject("lava", "#FF3366")
                }
                BoxSpawnerButton("Bonus Sphere", "#00FFCC", Modifier.weight(1f)) {
                    viewModel.addCustomUgcObject("circle", "#00FFCC")
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play and Publish tools
                Button(
                    onClick = {
                        viewModel.isSimulating = true
                        viewModel.isPlayingActiveGame = true
                        viewModel.activeGameTitle = "UGC Sandbox Session"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, "Simulate", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("TEST GAME", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showPublishSheet = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF5AF2)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, "Cloud", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PUBLISH UGC", fontWeight = FontWeight.Bold)
                }

            }
        }

        if (showPublishSheet) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141724))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "☁️ Publish UGC Game to Global Server",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("UGC Map Title", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00FFCC)
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = desc,
                            onValueChange = { desc = it },
                            label = { Text("Level Description", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00FFCC)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Button(
                                onClick = {
                                    viewModel.publishSandboxAsGame(title, desc)
                                    showPublishSheet = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Upload Live", color = Color.Black)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            TextButton(onClick = { showPublishSheet = false }) {
                                Text("Cancel", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        item {
            // === ROBLOX HIGH-SECURE GUARDIAN SUITE ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F111D))
                    .border(1.dp, Color(0xFFFF2D55).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Security Guard",
                                tint = Color(0xFFFF2D55),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Roblox Live Anticheat Console",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (viewModel.isScanningHackers) Color(0x33FFD60A) else Color(0x3300FFCC))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (viewModel.isScanningHackers) "DEEP SWEEP RUNNING" else "GUARDIAN STANDBY",
                                color = if (viewModel.isScanningHackers) Color(0xFFFFD60A) else Color(0xFF00FFCC),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Scans multiplayer node logs for packet injection, coordinate hacks, infinite jumps, or script intrusions.",
                        color = Color(0xFFA2A5B5),
                        fontSize = 11.sp
                    )

                    // Real-time terminal styling console log
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(1.dp, Color(0xFF1F2232), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = viewModel.anticheatLog,
                            color = if (viewModel.isScanningHackers) Color(0xFFFFD60A) else Color(0xFF00FFCC),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }

                    // Sweep launch controls
                    Button(
                        onClick = { viewModel.triggerAnticheatSweep() },
                        enabled = !viewModel.isScanningHackers,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF2D55),
                            disabledContainerColor = Color(0x33FF2D55)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, "Scan", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (viewModel.isScanningHackers) "ANALYZING TCP FLUX NODE LOBBIES..." else "TRIGGER ANTICHEAT SWEEP",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Exploit Queue list
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "⚠️ Suspected Exploiter Queue:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    if (viewModel.reportedHackers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF131722))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No exploiters detected in the server nodes. Clean play ✓", color = Color.Gray, fontSize = 11.sp)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (hacker in viewModel.reportedHackers) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF181C2C))
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = hacker.username,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Reason: ${hacker.reason}",
                                            color = Color(0xFFFF9500),
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = "Game: ${hacker.reportingGame}",
                                            color = Color.Gray,
                                            fontSize = 9.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.banHacker(hacker.username) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("DELIVER BAN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun BoxSpawnerButton(label: String, colorHex: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x33FFFFFF))
            .border(1.dp, Color(android.graphics.Color.parseColor(colorHex)).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(android.graphics.Color.parseColor(colorHex)))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, color = Color.White, fontSize = 10.sp)
        }
    }
}

@Composable
fun SocialTab(viewModel: BloxViewModel) {
    var textMessage by remember { mutableStateOf("") }
    val chats by viewModel.messageLogs.collectAsState()

    val quickChats = listOf(
        "Who wants to team up in Cyber City?",
        "Check my newly published sandbox game!",
        "Need Aura wings. Offering retrograde cap!",
        "Gravity in our node is set to zero!"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Roblox Live Social Dashboard",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Interact with nodes chatroom, join community groups and build developers networks.",
            color = Color(0xFFA2A5B5),
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Groups join section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CommunityGroupPill(viewModel, "Roblox Developers", "12k members", Color(0xFF00FFCC))
            CommunityGroupPill(viewModel, "Physics Engineers", "8.5k members", Color(0xFFBF5AF2))
            CommunityGroupPill(viewModel, "iOS 26 Fans", "22k members", Color(0xFFFF2D55))
            CommunityGroupPill(viewModel, "Sandbox Guild", "1.1k members", Color(0xFFFFD60A))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF0C0E17))
                .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(18.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💬 MULTIPLAYER LOBBY CHAT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Flush Chats",
                        color = Color(0xFFFF2D55),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.clearAllChats() }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chats) { chat ->
                        ChatMessageItem(chat = chat, activeUsername = viewModel.testProfile.username)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick phrase developer buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (phrase in quickChats) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33FFFFFF))
                        .clickable { viewModel.sendChatMessage(phrase) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(phrase, color = Color(0xFFC4C9D3), fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Text sender bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = textMessage,
                onValueChange = { textMessage = it },
                placeholder = { Text("Type custom message...", color = Color.Gray, fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF161A26),
                    unfocusedContainerColor = Color(0xFF161A26),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color(0xFF00FFCC)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00FFCC))
                    .clickable {
                        viewModel.sendChatMessage(textMessage)
                        textMessage = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Send, "Send", tint = Color.Black, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun CommunityGroupPill(viewModel: BloxViewModel, name: String, text: String, accent: Color) {
    val profile = viewModel.testProfile
    val alreadyJoined = profile.joinedGroups.split(",").contains(name)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (alreadyJoined) Color(0x33FFFFFF) else Color(0x66161A26))
            .border(1.dp, if (alreadyJoined) accent else Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .clickable { viewModel.joinSocialGroup(name) }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text, color = Color(0xFFA2A5B5), fontSize = 9.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (alreadyJoined) "Joined ✓" else "Join Guild",
                    color = accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(chat: MessageEntity, activeUsername: String) {
    val isMyMessage = chat.sender == activeUsername
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start
    ) {
        Text(
            text = chat.sender,
            color = if (isMyMessage) Color(0xFF00FFCC) else Color(0xFFBF5AF2),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMyMessage) 12.dp else 0.dp,
                        bottomEnd = if (isMyMessage) 0.dp else 12.dp
                    )
                )
                .background(if (isMyMessage) Color(0x2200FFCC) else Color(0x33FFFFFF))
                .padding(8.dp)
        ) {
            Text(chat.text, color = Color.White, fontSize = 12.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
fun MarketplaceTab(viewModel: BloxViewModel, badgeRes: Int) {
    val items by viewModel.marketplaceItems.collectAsState()
    var displayUgcSheet by remember { mutableStateOf(false) }

    var ugcName by remember { mutableStateOf("Gold Matrix Cloak") }
    var ugcCost by remember { mutableStateOf("450") }
    var ugcType by remember { mutableStateOf("wings") }
    var ugcStyleHex by remember { mutableStateOf("#FF9500") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Roblox Live Catalog & Economy",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "iOS 26 fully encrypted virtual trades. Trade rare items or purchase Robux instantly.",
            color = Color(0xFFA2A5B5),
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // === PREMIER ROBUX USD STORE STOREFRONT ===
        Text("💎 BUY ROBUX ONLINE PORTAL (USD Packages)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(viewModel.robuxPackages) { pkg ->
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E2135))
                        .border(1.dp, Color(0xFFFFD60A).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.buyRobuxPackage(pkg) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Robux pkg",
                            tint = Color(0xFFFFD60A),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${pkg.robuxAmount} R$",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x3300FFCC))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$${pkg.dollarPrice}",
                                color = Color(0xFF00FFCC),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Security indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x2226E6A5))
                .border(1.dp, Color(0xFF26E6A5), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, "Shield", tint = Color(0xFF26E6A5), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Secure SSL payments verified. Direct sandbox coin minting protocols verified.",
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Marketplace actions
        Row {
            Button(
                onClick = { displayUgcSheet = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Design custom virtual item & list", color = Color.Black, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // list items list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (displayUgcSheet) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1A1D2D))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text("🎨 Design UGC Virtual Accessory", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = ugcName,
                                onValueChange = { ugcName = it },
                                label = { Text("Item Name", color = Color.Gray, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = ugcCost,
                                    onValueChange = { ugcCost = it },
                                    label = { Text("Price (R$)", color = Color.Gray) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                )
                                OutlinedTextField(
                                    value = ugcStyleHex,
                                    onValueChange = { ugcStyleHex = it },
                                    label = { Text("Accent Color Hex", color = Color.Gray) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Category Types: cap, shirt, wings, face, pant", color = Color.Gray, fontSize = 10.sp)
                            OutlinedTextField(
                                value = ugcType,
                                onValueChange = { ugcType = it },
                                label = { Text("Category Style Label", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                Button(
                                    onClick = {
                                        val cost = ugcCost.toIntOrNull() ?: 200
                                        viewModel.sellUgcVirtualItem(ugcName, cost, ugcType.lowercase(), ugcStyleHex)
                                        displayUgcSheet = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("List & Mint", color = Color.Black)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                TextButton(onClick = { displayUgcSheet = false }) {
                                    Text("Cancel", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            items(items) { item ->
                MarketListItemRow(item = item, badgeRes = badgeRes) {
                    val status = viewModel.buyMarketplaceItem(item)
                    viewModel.physicsFeedbackText = status
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun MarketListItemRow(item: MarketItemEntity, badgeRes: Int, onBuy: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131724))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Item visual placeholder icon
                Image(
                    painter = painterResource(id = badgeRes),
                    contentDescription = "Badge item",
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x33FFFFFF)),
                    contentScale = ContentScale.Inside
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (item.isUgc) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFBF5AF2))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("UGC MINTED", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text("Designer: ${item.creator} | Style: ${item.styleType}", color = Color.Gray, fontSize = 11.sp)
                    Text("Bought: ${item.purchaseCount} times", color = Color(0xFF00FFCC), fontSize = 10.sp)
                }
            }

            // Buy button
            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFD60A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("${item.price} R$", color = Color(0xFFFFD60A), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun AvatarTab(viewModel: BloxViewModel, avatarRes: Int) {
    val activeProfile by viewModel.activeProfile.collectAsState()
    var statusText by remember { mutableStateOf(viewModel.testProfile.statusText) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Roblox iOS 26 Wardrobe Closets",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Your futuristic blocky avatar customizer. Accessories are automatically secured in Room local cache DB.",
            color = Color(0xFFA2A5B5),
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Avatar Preview Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F121C))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Interactive Profile Face
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = "Avatar Visor",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = activeProfile?.username ?: "RobitPlayer_26",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Status: \"${activeProfile?.statusText ?: ""}\"",
                    color = Color(0xFF00FFCC),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Wardrobe Equipped Details
        Text("Equipped Accessories Outfit", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        val equipped = activeProfile?.equippedItems?.split(",") ?: emptyList()
        if (equipped.isEmpty() || equipped.all { it.isEmpty() }) {
            Text("No active wearables. Access marketplace to buy accessory items!", color = Color.Gray, fontSize = 11.sp)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (item in equipped) {
                    if (item.isEmpty()) continue
                    val parts = item.split(":")
                    if (parts.size >= 2) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33FFFFFF))
                                .border(1.dp, Color(android.graphics.Color.parseColor(parts[1])), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text("${parts[0].uppercase()} ${parts[1]}", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Edit profile status
        Text("Update Profile Status Message", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = statusText,
            onValueChange = { statusText = it },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            trailingIcon = {
                IconButton(onClick = { viewModel.editProfileState(statusText) }) {
                    Icon(Icons.Default.Check, "Save", tint = Color(0xFF00FFCC))
                }
            }
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun GlassBottomNavigation(viewModel: BloxViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xE60E1019))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(28.dp))
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Discover",
                active = viewModel.currentTab == "discover"
            ) {
                viewModel.currentTab = "discover"
            }
            BottomNavItem(
                icon = Icons.Default.Build,
                label = "Studio",
                active = viewModel.currentTab == "sandbox"
            ) {
                viewModel.currentTab = "sandbox"
            }
            BottomNavItem(
                icon = Icons.Default.Email,
                label = "Chat",
                active = viewModel.currentTab == "social"
            ) {
                viewModel.currentTab = "social"
            }
            BottomNavItem(
                icon = Icons.Default.ShoppingCart,
                label = "Shop",
                active = viewModel.currentTab == "market"
            ) {
                viewModel.currentTab = "market"
            }
            BottomNavItem(
                icon = Icons.Default.Face,
                label = "Avatar",
                active = viewModel.currentTab == "avatar"
            ) {
                viewModel.currentTab = "avatar"
            }
        }
    }
}

@Composable
fun BottomNavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    val activeColor = Color(0xFFBF5AF2)
    val inactiveColor = Color(0xFF8E8E93)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = if (active) activeColor else inactiveColor,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun FullscreenPhysicsGameWindow(viewModel: BloxViewModel) {
    val objects by viewModel.activeObjects.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()

    // Extract equipped configurations for drawing character body
    var capColor = "#FF4D4D"
    var shirtColor = "#4D79FF"
    val equips = activeProfile?.equippedItems?.split(",") ?: emptyList()
    for (eq in equips) {
        if (eq.startsWith("cap:")) capColor = eq.substringAfter("cap:")
        if (eq.startsWith("shirt:")) shirtColor = eq.substringAfter("shirt:")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1016))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = viewModel.activeGameTitle,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Physics tick rate: 60Hz | Server node synchronized",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
                IconButton(onClick = { viewModel.leaveGame() }) {
                    Icon(Icons.Default.Close, "Exit", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }

            // High priority hud statistics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x3326E6A5))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("⭐ Collected Stars: ${viewModel.score}", color = Color(0xFFFFD60A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("💀 Respawn Counts: ${viewModel.deathsCount}", color = Color(0xFFFF3366), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // Main simulator bounds (Container Canvas)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(2.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val w = constraints.maxWidth.toFloat()
                    val h = constraints.maxHeight.toFloat()
                    viewModel.canvasWidth = w
                    viewModel.canvasHeight = h

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF07080D))
                    ) {
                        // Drawing grid lines to display high-fidelity depth
                        val spaceY = size.height / 10f
                        val spaceX = size.width / 10f
                        for (i in 0..10) {
                            drawLine(
                                color = Color(0x0EFFFFFF),
                                start = Offset(0f, i * spaceY),
                                end = Offset(size.width, i * spaceY)
                            )
                            drawLine(
                                color = Color(0x0EFFFFFF),
                                start = Offset(i * spaceX, 0f),
                                end = Offset(i * spaceX, size.height)
                            )
                        }

                        // Draw obstacles, players, systems
                        for (obj in objects) {
                            if (obj.isCollected) continue

                            val colorParsed = try {
                                Color(android.graphics.Color.parseColor(obj.colorHex))
                            } catch (e: Exception) {
                                Color(0xFF76FF03)
                            }

                            if (obj.type == "circle") {
                                if (obj.logicTag == "player") {
                                    // Custom draw beautiful block character
                                    drawCircle(
                                        color = colorParsed,
                                        center = Offset(obj.x, obj.y),
                                        radius = obj.radius
                                    )
                                    // Draw cap visor
                                    drawRect(
                                        color = Color(android.graphics.Color.parseColor(capColor)),
                                        topLeft = Offset(obj.x - obj.radius, obj.y - obj.radius - 8f),
                                        size = Size(obj.radius * 2f, 10f)
                                    )
                                    // Draw glowing cyber eyes
                                    drawCircle(
                                        color = Color(0xFF00FFCC),
                                        radius = 4f,
                                        center = Offset(obj.x - 6f, obj.y - 4f)
                                    )
                                    drawCircle(
                                        color = Color(0xFF00FFCC),
                                        radius = 4f,
                                        center = Offset(obj.x + 6f, obj.y - 4f)
                                    )
                                } else {
                                    // Default UGC Sphere elements
                                    drawCircle(
                                        color = colorParsed,
                                        center = Offset(obj.x, obj.y),
                                        radius = obj.radius
                                    )
                                }
                            } else if (obj.type == "box" || obj.type == "trampoline" || obj.type == "lava") {
                                val topL = Offset(obj.x - obj.width / 2f, obj.y - obj.height / 2f)
                                drawRoundRect(
                                    color = colorParsed,
                                    topLeft = topL,
                                    size = Size(obj.width, obj.height),
                                    cornerRadius = CornerRadius(10f, 10f)
                                )

                                // Trampoline indicators (neon gold sparks)
                                if (obj.logicTag == "trampoline") {
                                    drawRect(
                                        color = Color(0xFFFFD60A),
                                        topLeft = Offset(obj.x - obj.width / 2f + 4f, obj.y - obj.height / 2f + 2f),
                                        size = Size(obj.width - 8f, 4f)
                                    )
                                }
                                // Lava indicators (cyber glowing laser warning)
                                if (obj.logicTag == "lava") {
                                    drawRect(
                                        color = Color(0xFFFF2D55),
                                        topLeft = Offset(obj.x - obj.width / 2f, obj.y - obj.height / 2f),
                                        size = Size(obj.width, 3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive feedback text panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF05060A))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "📡 System Feed: ${viewModel.physicsFeedbackText}",
                    color = Color(0xFF00FFCC),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }

            // Bottom Player controller panel (Haptic Keys for movement)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lateral directionals
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PlayerControlButton(Icons.Default.ArrowBack, "Left") {
                        viewModel.moveCharacter(-1f)
                    }
                    PlayerControlButton(Icons.Default.ArrowForward, "Right") {
                        viewModel.moveCharacter(1f)
                    }
                }

                // Action controls: JUMP and Respawn Checkpoint
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { viewModel.resetControllablePlayer() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("RESPAWN", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = { viewModel.moveCharacter(0f, jump = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF5AF2)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Text("JUMP UP", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerControlButton(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x33FFFFFF))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = desc, tint = Color.White, modifier = Modifier.size(28.dp))
    }
}
