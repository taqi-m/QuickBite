package com.quick.bite.repositories

import com.quick.bite.MainActivity

object DummyDashboardRepository {

    fun getDashboardWidgets(): List<MainActivity.DashboardWidget> {
        return listOf(
            MainActivity.DashboardWidget(
                icon = "hot",
                title = "Live Promotions",
                mainText = "50% OFF",
                subText = "Burgers and Wings - Ends in 2h",
                actionText = "CLAIM",
                backgroundType = MainActivity.WidgetBackgroundType.GRADIENT
            )
        )
    }
}

