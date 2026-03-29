package com.quick.bite.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.quick.bite.R

class HistoryFragment : Fragment() {
    private data class UiRefs(
        val tabAllOrders: View,
        val tabCompleted: View,
        val tabCancelled: View,
        val tvTabAll: TextView,
        val tvTabCompleted: TextView,
        val tvTabCancelled: TextView,
        val indicatorAll: View,
        val indicatorCompleted: View,
        val indicatorCancelled: View,
        val order1: View,
        val order2: View,
        val order3: View,
        val order4: View,
        val order5: View
    )

    private var ui: UiRefs? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ui = UiRefs(
            tabAllOrders = view.findViewById(R.id.tab_all_orders),
            tabCompleted = view.findViewById(R.id.tab_completed),
            tabCancelled = view.findViewById(R.id.tab_cancelled),
            tvTabAll = view.findViewById(R.id.tv_tab_all),
            tvTabCompleted = view.findViewById(R.id.tv_tab_completed),
            tvTabCancelled = view.findViewById(R.id.tv_tab_cancelled),
            indicatorAll = view.findViewById(R.id.indicator_all),
            indicatorCompleted = view.findViewById(R.id.indicator_completed),
            indicatorCancelled = view.findViewById(R.id.indicator_cancelled),
            order1 = view.findViewById(R.id.order_1),
            order2 = view.findViewById(R.id.order_2),
            order3 = view.findViewById(R.id.order_3),
            order4 = view.findViewById(R.id.order_4),
            order5 = view.findViewById(R.id.order_5)
        )
        setupOrderData()
        setupClickListeners()
    }

    private fun setupOrderData() {
        val ui = ui ?: return

        setupOrderItem(
            ui.order1,
            getString(R.string.order_date_1),
            getString(R.string.order_restaurant_1),
            getString(R.string.order_details_1),
            getString(R.string.order_total_1)
        )

        setupOrderItem(
            ui.order2,
            getString(R.string.order_date_2),
            getString(R.string.order_restaurant_2),
            getString(R.string.order_details_2),
            getString(R.string.order_total_2)
        )

        setupOrderItem(
            ui.order3,
            getString(R.string.order_date_3),
            getString(R.string.order_restaurant_3),
            getString(R.string.order_details_3),
            getString(R.string.order_total_3)
        )

        setupOrderItem(
            ui.order4,
            getString(R.string.order_date_4),
            getString(R.string.order_restaurant_4),
            getString(R.string.order_details_4),
            getString(R.string.order_total_4)
        )

        setupOrderItem(
            ui.order5,
            getString(R.string.order_date_5),
            getString(R.string.order_restaurant_5),
            getString(R.string.order_details_5),
            getString(R.string.order_total_5)
        )
    }

    private fun setupOrderItem(
        itemView: View,
        date: String,
        restaurant: String,
        details: String,
        total: String
    ) {
        itemView.findViewById<TextView>(R.id.tv_order_date).text = date
        itemView.findViewById<TextView>(R.id.tv_restaurant_name).text = restaurant
        itemView.findViewById<TextView>(R.id.tv_order_details).text = details
        itemView.findViewById<TextView>(R.id.tv_order_total).text = total

        itemView.setOnClickListener {
            Toast.makeText(requireContext(), "Order: $restaurant", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        val ui = ui ?: return
        ui.tabAllOrders.setOnClickListener { selectTab(0) }
        ui.tabCompleted.setOnClickListener { selectTab(1) }
        ui.tabCancelled.setOnClickListener { selectTab(2) }
    }

    private fun selectTab(tabIndex: Int) {
        val ui = ui ?: return
        val grey = ContextCompat.getColor(requireContext(), R.color.grey_text)
        val primary = ContextCompat.getColor(requireContext(), R.color.primary)

        ui.tvTabAll.setTextColor(grey)
        ui.tvTabCompleted.setTextColor(grey)
        ui.tvTabCancelled.setTextColor(grey)

        ui.indicatorAll.visibility = View.INVISIBLE
        ui.indicatorCompleted.visibility = View.INVISIBLE
        ui.indicatorCancelled.visibility = View.INVISIBLE

        when (tabIndex) {
            0 -> {
                ui.tvTabAll.setTextColor(primary)
                ui.indicatorAll.visibility = View.VISIBLE
            }
            1 -> {
                ui.tvTabCompleted.setTextColor(primary)
                ui.indicatorCompleted.visibility = View.VISIBLE
            }
            2 -> {
                ui.tvTabCancelled.setTextColor(primary)
                ui.indicatorCancelled.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ui = null
    }
}
