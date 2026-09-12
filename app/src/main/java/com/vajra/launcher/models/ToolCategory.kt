package com.vajra.launcher.models

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class ToolCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    @DrawableRes val iconRes: Int,
    @ColorRes val accentColorRes: Int
)
