package com.quick.bite.model

import com.quick.bite.MainActivity

// Dashboard Widget Data Models
data class DashboardWidget(
    val icon: String,
    val title: String,
    val mainText: String,
    val subText: String,
    val actionText: String,
    val backgroundType: MainActivity.WidgetBackgroundType = MainActivity.WidgetBackgroundType.NORMAL
)