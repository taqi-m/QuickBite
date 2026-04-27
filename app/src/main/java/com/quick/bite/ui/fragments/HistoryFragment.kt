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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.quick.bite.R
import com.quick.bite.data.db.QuickBiteDatabaseManager
import com.quick.bite.data.repository.QuickBiteRepository
import com.quick.bite.model.Order
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvMonthlyTotal: TextView
    private lateinit var tvEmptyState: TextView

    private lateinit var repository: QuickBiteRepository
    private lateinit var orderAdapter: OrderAdapter

    private var currentUserId: Long = -1

    private var allOrders: List<Order> = emptyList()
    private var currentTab: String = TAB_PENDING

    companion object {
        private const val TAB_PENDING = "PENDING"
        private const val TAB_COMPLETED = "COMPLETED"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize repository
        repository = QuickBiteRepository(QuickBiteDatabaseManager(requireContext()))

        // Bind views
        progressBar = view.findViewById(R.id.pb_history_loading)
        tabLayout = view.findViewById(R.id.tab_layout)
        recyclerView = view.findViewById(R.id.rv_orders)
        tvTotalOrders = view.findViewById(R.id.tv_total_orders)
        tvMonthlyTotal = view.findViewById(R.id.tv_monthly_total)
        tvEmptyState = view.findViewById(R.id.tv_empty_state)

        // Setup RecyclerView
        orderAdapter = OrderAdapter(
            onCancelClick = { order -> cancelOrder(order) }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }

        // Setup tabs
        setupTabs()
    }

    override fun onResume() {
        super.onResume()
        loadOrderHistory()
    }

    /**
     * Sets up the TabLayout with Pending and Completed tabs.
     */
    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Pending").setTag(TAB_PENDING))
        tabLayout.addTab(tabLayout.newTab().setText("Completed").setTag(TAB_COMPLETED))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.tag as? String ?: TAB_PENDING
                filterAndDisplayOrders()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // No action needed
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // No action needed
            }
        })
    }

    /**
     * Loads all orders for the current user from the repository.
     */
    private fun loadOrderHistory() {
        progressBar.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE



        viewLifecycleOwner.lifecycleScope.launch {
            repository.getCurrentUserId().onSuccess { userId ->
                if (userId == null) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "User not logged in",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }
                currentUserId = userId
            }.onFailure { error ->
                progressBar.visibility = View.GONE
                Toast.makeText(
                    requireContext(),
                    "Failed to get user ID: ${error.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            val result = repository.getUserOrders(currentUserId)
            progressBar.visibility = View.GONE

            result.onSuccess { orders ->
                allOrders = orders
                filterAndDisplayOrders()
                updateSummary(orders)
            }.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load orders: ${error.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Filters orders based on the selected tab and updates the RecyclerView.
     */
    private fun filterAndDisplayOrders() {
        val filteredOrders = when (currentTab) {
            TAB_PENDING -> allOrders.filter { it.orderStatus.equals("PENDING", ignoreCase = true) }
            TAB_COMPLETED -> allOrders.filter { it.orderStatus.equals("COMPLETED", ignoreCase = true) }
            else -> allOrders
        }

        orderAdapter.submitList(filteredOrders)

        // Show empty state if no orders
        if (filteredOrders.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = if (currentTab == TAB_PENDING) {
                "No pending orders"
            } else {
                "No completed orders"
            }
        } else {
            tvEmptyState.visibility = View.GONE
        }
    }

    /**
     * Cancels a pending order via the repository.
     * Per API documentation, completed orders cannot be cancelled (returns 400).
     */
    private fun cancelOrder(order: Order) {
        // Validate that only pending orders can be cancelled
        if (!order.orderStatus.equals("PENDING", ignoreCase = true)) {
            Toast.makeText(
                requireContext(),
                "Only pending orders can be cancelled",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.cancelOrder(order.orderID)
            progressBar.visibility = View.GONE

            result.onSuccess {
                Toast.makeText(
                    requireContext(),
                    "Order #${order.orderID} cancelled successfully",
                    Toast.LENGTH_SHORT
                ).show()
                // Refresh the order history
                loadOrderHistory()
            }.onFailure { error ->
                val message = error.localizedMessage ?: "Failed to cancel order"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Updates the summary section with total orders and monthly spending.
     */
    private fun updateSummary(orders: List<Order>) {
        // Update total orders count
        tvTotalOrders.text = "Total Orders: ${orders.size}"

        // Calculate monthly total using server totalAmount
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val monthlyTotal = orders
            .filter { order ->
                val orderCalendar = Calendar.getInstance().apply {
                    timeInMillis = order.createdAt
                }
                orderCalendar.get(Calendar.MONTH) == currentMonth &&
                        orderCalendar.get(Calendar.YEAR) == currentYear
            }
            .sumOf { order ->
                order.totalAmount  // Use server-calculated total
            }

        tvMonthlyTotal.text = formatCurrency(monthlyTotal)
    }

    /**
     * Parses the order items to calculate the total amount.
     * The orderItems field can be a JSON string or object.
     */
    private fun parseOrderTotal(order: Order): Double {
        return order.totalAmount  // Simply return the server-calculated total
    }

    /**
     * Formats a double value as currency string.
     */
    private fun formatCurrency(amount: Double): String {
        return String.format(Locale.US, "$%.2f", amount)
    }

    // =========================
    // RECYCLERVIEW ADAPTER
    // =========================

    /**
     * RecyclerView adapter for displaying orders.
     * Shows cancel button only for pending orders.
     */
    inner class OrderAdapter(
        private val onCancelClick: (Order) -> Unit
    ) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

        private var orders: List<Order> = emptyList()

        fun submitList(newOrders: List<Order>) {
            orders = newOrders
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_order_history, parent, false)
            return OrderViewHolder(view)
        }

        override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
            holder.bind(orders[position])
        }

        override fun getItemCount(): Int = orders.size

        inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvOrderDate: TextView = itemView.findViewById(R.id.tv_order_date)
            private val tvRestaurantName: TextView = itemView.findViewById(R.id.tv_restaurant_name)
            private val tvOrderDetails: TextView = itemView.findViewById(R.id.tv_order_details)
            private val tvOrderTotal: TextView = itemView.findViewById(R.id.tv_order_total)
            private val btnCancelOrder: Button = itemView.findViewById(R.id.btn_cancel_order)

            fun bind(order: Order) {
                // Format and display the order date
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                tvOrderDate.text = dateFormat.format(Date(order.createdAt))

                // Display order ID as restaurant name placeholder
                tvRestaurantName.text = "Order #${order.orderID}"

                // Show status with appropriate styling
                val isPending = order.orderStatus.equals("PENDING", ignoreCase = true)
                tvOrderDetails.text = "Status: ${order.orderStatus}"

                // Parse and display total (placeholder calculation)
                tvOrderTotal.text = formatCurrency(parseOrderTotal(order))

                // Show cancel button only for pending orders
                if (isPending) {
                    btnCancelOrder.visibility = View.VISIBLE
                    btnCancelOrder.setOnClickListener {
                        onCancelClick(order)
                    }
                } else {
                    btnCancelOrder.visibility = View.GONE
                }
            }
        }
    }
}