package com.onetap.ui.components

import android.graphics.drawable.Drawable

/**
 * Represents an app/shortcut in the OneTap grid
 */
data class AppItem(
    val id: String,
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val position: Int = 0
)
