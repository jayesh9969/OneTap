package com.onetap

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.onetap.service.FloatingButtonService
import com.onetap.service.VoiceService
import com.onetap.service.CommandResult
import com.onetap.service.CommandType
import com.onetap.ui.theme.OneTapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneTapTheme {
                OneTapScreen()
            }
        }
    }
}

data class QuickApp(
    val name: String,
    val icon: ImageVector,
    val packageName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneTapScreen() {
    val context = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var voiceText by remember { mutableStateOf("") }

    // Voice service
    val voiceService = remember {
        VoiceService(context).apply {
            onResult = { result ->
                isListening = false
                voiceText = result
                // Parse and launch
                val command = parseCommand(result)
                if (command.packageName != null) {
                    launchApp(context, command.packageName)
                }
            }
            onError = { error ->
                isListening = false
                voiceText = error
            }
        }
    }

    // Default apps to show
    val quickApps = remember {
        listOf(
            QuickApp("Phone", Icons.Default.Phone, "com.android.contacts"),
            QuickApp("WhatsApp", Icons.Default.Chat, "com.whatsapp"),
            QuickApp("Camera", Icons.Default.CameraAlt, "com.android.camera"),
            QuickApp("Messages", Icons.Default.Message, "com.android.mms"),
            QuickApp("Calculator", Icons.Default.Calculate, "com.android.calculator2"),
            QuickApp("Settings", Icons.Default.Settings, "com.android.settings"),
            QuickApp("WhatsApp", Icons.Default.Chat, "com.whatsapp"),
            QuickApp("Phone", Icons.Default.Phone, "com.android.contacts")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "OneTap", 
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (showSettings) {
            SettingsScreen(
                onBack = { showSettings = false },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tap to open",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // App Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickApps) { app ->
                        AppIconButton(
                            app = app,
                            onClick = {
                                launchApp(context, app.packageName)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Voice Button
                VoiceButton(
                    isListening = isListening,
                    voiceText = voiceText,
                    onClick = {
                        if (isListening) {
                            voiceService.stopListening()
                            isListening = false
                        } else {
                            voiceText = ""
                            voiceService.startListening()
                            isListening = true
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Emergency Button
                EmergencyButton(
                    onClick = {
                        // Open emergency dialer
                        launchApp(context, "com.android.contacts")
                    }
                )
            }
        }
    }
}

@Composable
fun AppIconButton(
    app: QuickApp,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 4.dp,
            onClick = onClick
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = app.icon,
                    contentDescription = app.name,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = app.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun EmergencyButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE53935)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Emergency",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun VoiceButton(
    isListening: Boolean,
    voiceText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Show recognized text
        if (voiceText.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = "\"$voiceText\"",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Voice button
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) Color(0xFFE53935) else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isListening) "Listening..." else "Voice Command",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Text(
            text = "Say: \"Open WhatsApp\", \"Play music\", \"Call mom\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

fun parseCommand(speech: String): CommandResult {
    val lowerSpeech = speech.lowercase()
    
    // App patterns
    val appPatterns = mapOf(
        "whatsapp" to "com.whatsapp",
        "message" to "com.android.mms",
        "messages" to "com.android.mms",
        "phone" to "com.android.contacts",
        "call" to "com.android.contacts",
        "camera" to "com.android.camera",
        "photo" to "com.google.android.apps.photos",
        "photos" to "com.google.android.apps.photos",
        "youtube" to "com.google.android.youtube",
        "music" to "com.google.android.music",
        "spotify" to "com.spotify.music",
        "gaana" to "com.gaana",
        "jiosaavn" to "com.jio.media.jiobeats",
        "calculator" to "com.android.calculator2",
        "settings" to "com.android.settings",
        "chrome" to "com.android.chrome",
        "browser" to "com.android.chrome",
        "maps" to "com.google.android.apps.maps",
        "calendar" to "com.google.android.calendar",
        "gmail" to "com.google.android.gm",
        "mail" to "com.google.android.gm",
        "instagram" to "com.instagram.android",
        "facebook" to "com.facebook.katana",
        "twitter" to "com.twitter.android",
        "telegram" to "org.telegram.messenger",
        "zoom" to "us.zoom.videomeetings",
        "meet" to "com.google.android.apps.tachyon",
        "paytm" to "com.paytm",
        "gpay" to "com.google.android.apps.nbu.paisa.user",
        "phonepe" to "com.phonepe.app",
        "amazon" to "com.amazon.mShop.android.shopping",
        "flipkart" to "com.flipkart.android",
        "vlc" to "org.videolan.vlc",
        "mx player" to "com.mxtech.videoplayer.ad",
        "file manager" to "com.estrongs.android.pop",
        "terminal" to "com.termux"
    )
    
    for ((name, packageName) in appPatterns) {
        if (lowerSpeech.contains(name)) {
            return CommandResult(
                type = CommandType.OPEN_APP,
                packageName = packageName,
                displayText = name.replaceFirstChar { it.uppercase() }
            )
        }
    }
    
    // Special commands
    when {
        lowerSpeech.contains("play") && lowerSpeech.contains("song") -> {
            return CommandResult(
                type = CommandType.OPEN_APP,
                packageName = "com.google.android.music",
                displayText = "Music"
            )
        }
        lowerSpeech.contains("play") && lowerSpeech.contains("youtube") -> {
            return CommandResult(
                type = CommandType.OPEN_APP,
                packageName = "com.google.android.youtube",
                displayText = "YouTube"
            )
        }
    }
    
    return CommandResult(
        type = CommandType.UNKNOWN,
        packageName = null,
        displayText = speech
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var floatingEnabled by remember { mutableStateOf(false) }

    // Check if overlay permission is granted
    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Floating Button Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Floating Button",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Show bubble to open OneTap from any screen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = floatingEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (hasOverlayPermission()) {
                                    FloatingButtonService.start(context)
                                    floatingEnabled = true
                                } else {
                                    // Request overlay permission
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                }
                            } else {
                                FloatingButtonService.stop(context)
                                floatingEnabled = false
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Other Settings (Coming Soon)
            Text(
                text = "Coming Soon",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• Add/remove apps\n• Change trigger method\n• Customize icons\n• Emergency contacts",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun launchApp(context: android.content.Context, packageName: String) {
    try {
        val intent: Intent? = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        // App not found or permission denied
    }
}
