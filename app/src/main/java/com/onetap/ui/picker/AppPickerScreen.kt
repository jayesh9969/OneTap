package com.onetap.ui.picker

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Data class representing an installed app
 */
data class InstalledApp(
    val packageName: String,
    val appName: String,
    val icon: androidx.compose.ui.graphics.ImageBitmap?,
    val isSelected: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerScreen(
    onBack: () -> Unit,
    selectedApps: List<String>,
    onAppsSelected: (List<String>) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var selectedPackages by remember { mutableStateOf(selectedApps.toSet()) }

    // Load installed apps
    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { app ->
                // Filter out system apps unless they're commonly used
                val isLaunchable = pm.getLaunchIntentForPackage(app.packageName) != null
                val isNotSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                isLaunchable && (isNotSystemApp || isCommonApp(app.packageName))
            }
            .map { app ->
                InstalledApp(
                    packageName = app.packageName,
                    appName = pm.getApplicationLabel(app).toString(),
                    icon = null, // Would load icon here in production
                    isSelected = selectedApps.contains(app.packageName)
                )
            }
            .sortedBy { it.appName.lowercase() }
        
        apps = installedApps
    }

    val filteredApps = apps.filter { 
        searchQuery.isEmpty() || it.appName.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Apps") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onAppsSelected(selectedPackages.toList())
                            onBack()
                        }
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search apps...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            // App list
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredApps) { app ->
                    val isSelected = selectedPackages.contains(app.packageName)
                    
                    ListItem(
                        headlineContent = { Text(app.appName) },
                        supportingContent = { Text(app.packageName, style = MaterialTheme.typography.bodySmall) },
                        leadingContent = {
                            Icon(
                                Icons.Default.Android,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedPackages = if (checked) {
                                        selectedPackages + app.packageName
                                    } else {
                                        selectedPackages - app.packageName
                                    }
                                }
                            )
                        },
                        modifier = Modifier.clickable {
                            selectedPackages = if (isSelected) {
                                selectedPackages - app.packageName
                            } else {
                                selectedPackages + app.packageName
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 *判断是否为常用应用
 */
private fun isCommonApp(packageName: String): Boolean {
    val commonApps = listOf(
        "com.whatsapp",
        "com.android.contacts",
        "com.android.phone",
        "com.android.mms",
        "com.google.android.apps.photos",
        "com.google.android.gm",
        "com.google.android.calendar",
        "com.android.camera2",
        "com.google.android.apps.maps",
        "com.google.android.youtube",
        "com.instagram.android",
        "com.facebook.katana",
        "com.twitter.android",
        "com.zhihu.android",
        "in.zaloapp",
        "com.flipkart.android",
        "com.amazon.mShop.android.shopping",
        "com.paytm",
        "com.google.android.apps.nbu.paisa.user",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.termux"
    )
    return commonApps.any { packageName.startsWith(it) }
}
