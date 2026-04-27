package com.quick.bite.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.ChipGroup
import com.quick.bite.R
import com.quick.bite.api.RetrofitClient
import com.quick.bite.data.db.QuickBiteDatabaseManager
import com.quick.bite.data.repository.QuickBiteRepository
import com.quick.bite.model.MasterOrder
import kotlinx.coroutines.launch
import java.util.Locale

class HistoryFragment : Fragment() {
    private lateinit var progressBar: ProgressBar
    private lateinit var orderViews: List<View>
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvMonthlyTotal: TextView

    private lateinit var repository: QuickBiteRepository
    private val currentUserEmail = "tech@example.com"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = QuickBiteRepository(RetrofitClient.getApiService(), QuickBiteDatabaseManager(requireContext()))

        tvTotalOrders = view.findViewById(R.id.tv_total_orders)
        tvMonthlyTotal = view.findViewById(R.id.tv_monthly_total)
        progressBar = view.findViewById(R.id.pb_history_loading)

        orderViews = listOf(
            view.findViewById(R.id.order_1),
            view.findViewById(R.id.order_2),
            view.findViewById(R.id.order_3),
            view.findViewById(R.id.order_4),
            view.findViewById(R.id.order_5)
        )
    }

    override fun onResume() {
        super.onResume()
        loadOrderHistory()
    }

    private fun loadOrderHistory() {
        progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getOrderHistory(currentUserEmail)
            progressBar.visibility = View.GONE

            result.onSuccess { masterOrders ->
                renderMasterOrders(masterOrders)
                updateSummary(masterOrders)
            }.onFailure { error ->
                Toast.makeText(context, "Sync Error: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderMasterOrders(orders: List<MasterOrder>) {
        orderViews.forEachIndexed { index, view ->
            val order = orders.getOrNull(index)
            if (order == null) {
                view.visibility = View.GONE
            } else {
                view.visibility = View.VISIBLE
                bindOrderView(view, order)
            }
        }
    }

    private fun bindOrderView(view: View, order: MasterOrder) {
        view.findViewById<TextView>(R.id.tv_restaurant_name).text = "Order #${order.masterID}"
        view.findViewById<TextView>(R.id.tv_order_total).text = formatCurrency(order.grandtotal)
        view.findViewById<TextView>(R.id.tv_order_details).text = "Restaurant ID: ${order.restaurantID}"

        view.findViewById<Button>(R.id.btn_cancel_order).setOnClickListener {
            cancelOrder(order.masterID)
        }
    }

    private fun cancelOrder(masterId: Int) {
        progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.cancelOrder(masterId)
            progressBar.visibility = View.GONE

            result.onSuccess {
                Toast.makeText(context, "Order cancelled successfully", Toast.LENGTH_SHORT).show()
                loadOrderHistory() // Refresh the UI after successful deletion
            }.onFailure {
                Toast.makeText(context, "Failed to cancel order.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSummary(orders: List<MasterOrder>) {
        tvTotalOrders.text = orders.size.toString()
        val totalAmount = orders.sumOf { it.grandtotal }
        tvMonthlyTotal.text = formatCurrency(totalAmount)
    }

    private fun formatCurrency(amount: Double): String = String.format(Locale.US, "$%.2f", amount)
}