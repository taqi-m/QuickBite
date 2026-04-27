package com.quick.bite.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.quick.bite.R
import com.quick.bite.adapters.MainPagerAdapter
import com.quick.bite.ui.fragments.HomeFragment

class MainActivity : AppCompatActivity(), HomeFragment.RestaurantSelectionContract {

    enum class WidgetBackgroundType {
        NORMAL, GRADIENT, DARK
    }

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

    override fun onCartClicked() {
        val intent = Intent(this, CheckoutActivity::class.java)
        startActivity(intent)
    }

    override fun onRestaurantSelected(restaurantId: Int) {
        val intent = Intent(this, RestaurantDetailActivity::class.java)
        intent.putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, restaurantId)
        startActivity(intent)
    }

    /**
     * Maps ViewPager position to bottom navigation menu position
     * (accounting for the placeholder item at position 2)
     */
    private fun getMenuPosition(viewPagerPosition: Int): Int {
        return when (viewPagerPosition) {
            MainPagerAdapter.HOME_POSITION -> 0
            MainPagerAdapter.HISTORY_POSITION -> 1
            MainPagerAdapter.PROFILE_POSITION -> 2
            else -> 0
        }
    }
}

