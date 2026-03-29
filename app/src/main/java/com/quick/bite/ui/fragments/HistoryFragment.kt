package com.quick.bite.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.quick.bite.R
import com.quick.bite.repositories.SessionDataRepository

class HistoryFragment : Fragment() {
    private data class UiRefs(
        val order1: View,
        val order2: View,
        val order3: View,
        val order4: View,
        val order5: View,
        val monthlyTotal: TextView,
        val totalOrders: TextView
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
            order1 = view.findViewById(R.id.order_1),
            order2 = view.findViewById(R.id.order_2),
            order3 = view.findViewById(R.id.order_3),
            order4 = view.findViewById(R.id.order_4),
            order5 = view.findViewById(R.id.order_5),
            monthlyTotal = view.findViewById(R.id.tv_monthly_total),
            totalOrders = view.findViewById(R.id.tv_total_orders)
        )
        setupOrderData()
    }

    private fun setupOrderData() {
        val ui = ui ?: return

        val displayOrders = SessionDataRepository.getOrderLogs().take(5)
        val orderViews = listOf(ui.order1, ui.order2, ui.order3, ui.order4, ui.order5)

        orderViews.forEachIndexed { index, orderView ->
            val order = displayOrders.getOrNull(index)
            if (order == null) {
                setupOrderItem(
                    orderView,
                    getString(R.string.placeholder_empty),
                    getString(R.string.placeholder_empty),
                    getString(R.string.placeholder_empty),
                    getString(R.string.placeholder_empty)
                )
            } else {
                setupOrderItem(orderView, order.date, order.restaurantName, order.details, order.total)
            }
        }

        val allOrders = SessionDataRepository.getOrderLogs()
        val totalAmount = allOrders.sumOf { parseMoney(it.total) }
        ui.monthlyTotal.text = SessionDataRepository.formatMoney(totalAmount)
        ui.totalOrders.text = allOrders.size.toString()
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

    private fun parseMoney(value: String): Double {
        return value.replace("$", "").trim().toDoubleOrNull() ?: 0.0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ui = null
    }
}
