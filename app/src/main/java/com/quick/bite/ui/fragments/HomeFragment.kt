package com.quick.bite.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.quick.bite.ActiveKitchenAdapter
import com.quick.bite.DashboardWidgetAdapter
import com.quick.bite.MainActivity
import com.quick.bite.R
import com.quick.bite.ui.checkout.CheckoutActivity
import com.quick.bite.ui.history.OrderHistoryActivity

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDashboardWidgets(view)
        setupActiveKitchens(view)
        setupClickListeners(view)
        setupCartButton(view)
    }


    private fun setupDashboardWidgets(view: View) {
        val dashboardWidgets = createDashboardWidgets()
        val rvDashboard = view.findViewById<RecyclerView>(R.id.rv_dashboard_widgets)

        // Grid Layout System for dashboard widgets
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                // Live Promotions widget spans 2 columns (featured)
                return if (position == 0) 2 else 1
            }
        }

        rvDashboard.layoutManager = gridLayoutManager
        rvDashboard.adapter = DashboardWidgetAdapter(dashboardWidgets)
    }

    private fun setupActiveKitchens(view: View) {
        val activeKitchens = createActiveKitchens()
        val rvKitchens = view.findViewById<RecyclerView>(R.id.rv_active_kitchens)

        rvKitchens.layoutManager = LinearLayoutManager(requireContext())
        rvKitchens.adapter = ActiveKitchenAdapter(activeKitchens)
    }

    private fun setupClickListeners(view: View) {
        // Setup manage reorder button to navigate to Order History
        /*view.findViewById<Button>(R.id.btn_manage_reorder)?.setOnClickListener {
            val intent = Intent(requireContext(), OrderHistoryActivity::class.java)
            startActivity(intent)
        }*/
    }

    private fun createDashboardWidgets(): List<MainActivity.DashboardWidget> {
        return listOf(
            MainActivity.DashboardWidget(
                icon = "🔥",
                title = "Live Promotions",
                mainText = "50% OFF",
                subText = "Burgers & Wings • Ends in 2h",
                actionText = "CLAIM",
                backgroundType = MainActivity.WidgetBackgroundType.GRADIENT
            ),
            /*MainActivity.DashboardWidget(
                icon = "📂",
                title = "My Flavors",
                mainText = "Healthy",
                subText = "Asian, +2 more",
                actionText = ""
            ),
            MainActivity.DashboardWidget(
                icon = "❤️",
                title = "Nearby Gems",
                mainText = "Morning Harvest",
                subText = "1.2 km away",
                actionText = ""
            ),
            MainActivity.DashboardWidget(
                icon = "📊",
                title = "Consumption Metrics",
                mainText = "1,240 QB",
                subText = "Points Earned",
                actionText = "REDEEM REWARDS",
                backgroundType = MainActivity.WidgetBackgroundType.DARK
            )*/
        )
    }

    private fun setupCartButton(view: View) {
        view.findViewById<MaterialButton>(R.id.btn_cart)?.setOnClickListener {
            val intent = Intent(activity, CheckoutActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createActiveKitchens(): List<MainActivity.ActiveKitchen> {
        return listOf(
            MainActivity.ActiveKitchen(
                icon = "🍜",
                name = "Umami House",
                category = "JAPANESE • $$$",
                rating = "4.9 ★",
                deliveryTime = "20m",
                deliveryFee = "FREE"
            ),
            MainActivity.ActiveKitchen(
                icon = "🧁",
                name = "Sweet Delights",
                category = "BAKERY • $$",
                rating = "4.5 ★",
                deliveryTime = "35m",
                deliveryFee = "$2.99"
            )
        )
    }
}

