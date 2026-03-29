package com.quick.bite.ui.history

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quick.bite.R

/**
 * OrderHistoryActivity - Displays order history with tabs and table layout
 *
 * Layout Architecture:
 * - Top App Bar (L4: RelativeLayout)
 * - Tabs Section (L3: LinearLayout with weightSum)
 * - Order History Table (L7: TableLayout with stretchColumns)
 * - Summary Cards (L6: GridLayout with 2 columns)
 */
class OrderHistoryActivity : AppCompatActivity() {

    // Header Views
    private lateinit var btnBack: ImageButton
    private lateinit var btnSearch: ImageButton

    // Tab Views
    private lateinit var tabAllOrders: LinearLayout
    private lateinit var tabCompleted: LinearLayout
    private lateinit var tabCancelled: LinearLayout

    private lateinit var tvTabAll: TextView
    private lateinit var tvTabCompleted: TextView
    private lateinit var tvTabCancelled: TextView

    private lateinit var indicatorAll: View
    private lateinit var indicatorCompleted: View
    private lateinit var indicatorCancelled: View

    // Order Item Views
    private lateinit var order1: View
    private lateinit var order2: View
    private lateinit var order3: View
    private lateinit var order4: View
    private lateinit var order5: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        initViews()
        setupOrderData()
        setupClickListeners()
    }

    private fun initViews() {
        // Header
        btnBack = findViewById(R.id.btn_back)
        btnSearch = findViewById(R.id.btn_search)

        // Tabs
        tabAllOrders = findViewById(R.id.tab_all_orders)
        tabCompleted = findViewById(R.id.tab_completed)
        tabCancelled = findViewById(R.id.tab_cancelled)

        tvTabAll = findViewById(R.id.tv_tab_all)
        tvTabCompleted = findViewById(R.id.tv_tab_completed)
        tvTabCancelled = findViewById(R.id.tv_tab_cancelled)

        indicatorAll = findViewById(R.id.indicator_all)
        indicatorCompleted = findViewById(R.id.indicator_completed)
        indicatorCancelled = findViewById(R.id.indicator_cancelled)

        // Order items
        order1 = findViewById(R.id.order_1)
        order2 = findViewById(R.id.order_2)
        order3 = findViewById(R.id.order_3)
        order4 = findViewById(R.id.order_4)
        order5 = findViewById(R.id.order_5)
    }

    private fun setupOrderData() {
        // Order 1: Artisan Pizza Hub
        setupOrderItem(
            order1,
            getString(R.string.order_date_1),
            getString(R.string.order_restaurant_1),
            getString(R.string.order_details_1),
            getString(R.string.order_total_1)
        )

        // Order 2: QuickBite Burgers
        setupOrderItem(
            order2,
            getString(R.string.order_date_2),
            getString(R.string.order_restaurant_2),
            getString(R.string.order_details_2),
            getString(R.string.order_total_2)
        )

        // Order 3: Sushi Express
        setupOrderItem(
            order3,
            getString(R.string.order_date_3),
            getString(R.string.order_restaurant_3),
            getString(R.string.order_details_3),
            getString(R.string.order_total_3)
        )

        // Order 4: Taco Fiesta
        setupOrderItem(
            order4,
            getString(R.string.order_date_4),
            getString(R.string.order_restaurant_4),
            getString(R.string.order_details_4),
            getString(R.string.order_total_4)
        )

        // Order 5: Green Salad Co.
        setupOrderItem(
            order5,
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
            // TODO: Navigate to order details screen
            Toast.makeText(this, "Order: $restaurant", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSearch.setOnClickListener {
            // TODO: Implement search functionality
            Toast.makeText(this, "Search orders", Toast.LENGTH_SHORT).show()
        }

        tabAllOrders.setOnClickListener {
            selectTab(0)
        }

        tabCompleted.setOnClickListener {
            selectTab(1)
        }

        tabCancelled.setOnClickListener {
            selectTab(2)
        }
    }

    private fun selectTab(tabIndex: Int) {
        // Reset all tabs
        tvTabAll.setTextColor(getColor(R.color.grey_text))
        tvTabCompleted.setTextColor(getColor(R.color.grey_text))
        tvTabCancelled.setTextColor(getColor(R.color.grey_text))

        indicatorAll.visibility = View.INVISIBLE
        indicatorCompleted.visibility = View.INVISIBLE
        indicatorCancelled.visibility = View.INVISIBLE

        // Activate selected tab
        when (tabIndex) {
            0 -> {
                tvTabAll.setTextColor(getColor(R.color.primary))
                indicatorAll.visibility = View.VISIBLE
                // TODO: Load all orders
            }
            1 -> {
                tvTabCompleted.setTextColor(getColor(R.color.primary))
                indicatorCompleted.visibility = View.VISIBLE
                // TODO: Load completed orders
            }
            2 -> {
                tvTabCancelled.setTextColor(getColor(R.color.primary))
                indicatorCancelled.visibility = View.VISIBLE
                // TODO: Load cancelled orders
            }
        }
    }
}

