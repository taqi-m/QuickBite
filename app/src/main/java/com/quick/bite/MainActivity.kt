package com.quick.bite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.quick.bite.adapters.MainPagerAdapter
import com.quick.bite.ui.checkout.CheckoutActivity
import com.quick.bite.ui.history.OrderHistoryActivity
import androidx.core.view.get

class MainActivity : AppCompatActivity() {

    // Dashboard Widget Data Models
    data class DashboardWidget(
        val icon: String,
        val title: String,
        val mainText: String,
        val subText: String,
        val actionText: String,
        val backgroundType: WidgetBackgroundType = WidgetBackgroundType.NORMAL
    )

    enum class WidgetBackgroundType {
        NORMAL, GRADIENT, DARK
    }

    data class ActiveKitchen(
        val icon: String,
        val name: String,
        val category: String,
        val rating: String,
        val deliveryTime: String,
        val deliveryFee: String
    )

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        setupSystemBars()
        setupViewPager()
        setupBottomNavigation()
    }

    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.app_bar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupViewPager() {
        viewPager = findViewById(R.id.view_pager)
        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter

        // Disable user swipe to prevent conflicts with bottom navigation
        viewPager.isUserInputEnabled = true

        // Register page change callback to sync with bottom navigation
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Update bottom navigation selection based on ViewPager position
                bottomNavigation.menu[getMenuPosition(position)].isChecked = true
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    viewPager.currentItem = MainPagerAdapter.HOME_POSITION
                    true
                }
                R.id.nav_search -> {
                    viewPager.currentItem = MainPagerAdapter.SEARCH_POSITION
                    true
                }
                R.id.nav_history -> {
                    viewPager.currentItem = MainPagerAdapter.HISTORY_POSITION
                    true
                }
                R.id.nav_profile -> {
                    viewPager.currentItem = MainPagerAdapter.PROFILE_POSITION
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Maps ViewPager position to bottom navigation menu position
     * (accounting for the placeholder item at position 2)
     */
    private fun getMenuPosition(viewPagerPosition: Int): Int {
        return when (viewPagerPosition) {
            MainPagerAdapter.HOME_POSITION -> 0
            MainPagerAdapter.SEARCH_POSITION -> 1
            MainPagerAdapter.HISTORY_POSITION -> 3  // Skip placeholder at position 2
            MainPagerAdapter.PROFILE_POSITION -> 4
            else -> 0
        }
    }
}

// Simple RecyclerView Adapters for demo purposes
class DashboardWidgetAdapter(private val widgets: List<MainActivity.DashboardWidget>) :
    RecyclerView.Adapter<DashboardWidgetAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.tv_widget_icon)
        val title: TextView = view.findViewById(R.id.tv_widget_title)
        val mainText: TextView = view.findViewById(R.id.tv_widget_main_text)
        val subText: TextView = view.findViewById(R.id.tv_widget_sub_text)
        val actionButton: MaterialButton = view.findViewById(R.id.btn_widget_action)
        val background: View = view.findViewById(R.id.view_widget_background)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_widget, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val widget = widgets[position]

        // Set icon (for now using placeholder)
        holder.icon.setImageResource(R.drawable.ic_local_fire_department_128)
        holder.title.text = widget.title
        holder.mainText.text = widget.mainText
        holder.subText.text = widget.subText
        holder.actionButton.text = widget.actionText

        // Show/hide action button based on actionText
        if (widget.actionText.isEmpty()) {
            holder.actionButton.visibility = View.GONE
        } else {
            holder.actionButton.visibility = View.VISIBLE
        }

        // Apply background based on widget type
        when (widget.backgroundType) {
            MainActivity.WidgetBackgroundType.GRADIENT -> {
                holder.background.setBackgroundResource(R.drawable.bg_promotion_gradient)
            }
            MainActivity.WidgetBackgroundType.DARK -> {
                holder.background.setBackgroundResource(R.drawable.dark_background)
            }
            else -> {
                holder.background.setBackgroundResource(R.drawable.normal_background)
            }
        }
    }

    override fun getItemCount() = widgets.size
}

class ActiveKitchenAdapter(private val kitchens: List<MainActivity.ActiveKitchen>) :
    RecyclerView.Adapter<ActiveKitchenAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ic_kitchen_icon)
        val name: TextView = view.findViewById(R.id.tv_kitchen_name)
        val category: TextView = view.findViewById(R.id.tv_kitchen_category)
        val rating: TextView = view.findViewById(R.id.tv_kitchen_rating)
        val deliveryTime: TextView = view.findViewById(R.id.tv_delivery_time)
        val deliveryFee: TextView = view.findViewById(R.id.tv_delivery_fee)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_active_kitchen, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kitchen = kitchens[position]

        // Set icon (using placeholder for now)
        holder.icon.setImageResource(R.drawable.ic_soup_kitchen_128)
        holder.name.text = kitchen.name
        holder.category.text = kitchen.category
        holder.rating.text = kitchen.rating
        holder.deliveryTime.text = kitchen.deliveryTime
        holder.deliveryFee.text = kitchen.deliveryFee

        // Set click listener to open restaurant detail
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, RestaurantDetailActivity::class.java)
            intent.putExtra("restaurant_name", kitchen.name)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = kitchens.size
}