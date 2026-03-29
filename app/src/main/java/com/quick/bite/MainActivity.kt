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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.quick.bite.ui.fragments.HistoryFragment
import com.quick.bite.ui.fragments.HomeFragment
import com.quick.bite.ui.fragments.ProfileFragment
import com.quick.bite.ui.fragments.SearchFragment

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_NAME = "extra_user_name"
        const val EXTRA_WELCOME_MESSAGE = "extra_welcome_message"
    }

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

    private lateinit var bottomNavigation: BottomNavigationView
    private var userName: String = ""
    private var welcomeMessage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        readIntentExtras()
        setupSystemBars()
        setupBottomNavigation()

        if (savedInstanceState == null) {
            openHomeFragment()
        }
    }

    private fun readIntentExtras() {
        userName = intent.getStringExtra(EXTRA_USER_NAME).orEmpty()
        welcomeMessage = intent.getStringExtra(EXTRA_WELCOME_MESSAGE).orEmpty()

        val statusView = findViewById<TextView>(R.id.tv_order_status)
        if (welcomeMessage.isNotBlank()) {
            statusView.text = welcomeMessage
        }
    }

    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.app_bar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    openHomeFragment()
                    true
                }
                R.id.nav_search -> {
                    openSearchFragment()
                    true
                }
                R.id.nav_history -> {
                    openHistoryFragment()
                    true
                }
                R.id.nav_profile -> {
                    openProfileFragment()
                    true
                }
                else -> false
            }
        }
    }

    private fun openHomeFragment() {
        val homeFragment = HomeFragment.newInstance(userName, welcomeMessage)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, homeFragment)
            .commit()
    }

    private fun openSearchFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SearchFragment())
            .commit()
    }

    private fun openHistoryFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HistoryFragment())
            .commit()
    }

    private fun openProfileFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProfileFragment())
            .commit()
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
