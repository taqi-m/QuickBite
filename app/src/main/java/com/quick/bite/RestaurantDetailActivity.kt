package com.quick.bite

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class RestaurantDetailActivity : AppCompatActivity() {

    // Data Models for Restaurant Screen
    data class MenuItem(
        val name: String,
        val description: String,
        val price: String,
        val rating: String,
        val imageUrl: String?,
        val isVeg: Boolean,
        val vegIndicatorDrawable: Int
    )

    data class RestaurantInfo(
        val name: String,
        val category: String,
        val rating: String,
        val reviewCount: String,
        val deliveryTime: String,
        val deliveryFee: String,
        val distance: String,
        val heroImageUrl: String?,
        val logoUrl: String?
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.restaurant_detail_screen)

        setupSystemBars()
        setupToolbar()
        setupMenuItems()
        setupCategoryChips()
        setupCartButton()
    }

    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.app_bar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Set navigation click listener
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupMenuItems() {
        val menuItems = createMenuItems()
        val rvMenuItems = findViewById<RecyclerView>(R.id.rv_menu_items)

        rvMenuItems.layoutManager = LinearLayoutManager(this)
        rvMenuItems.adapter = MenuItemAdapter(menuItems) { menuItem ->
            // Handle add to cart click
            onAddToCartClicked(menuItem)
        }
    }

    private fun setupCategoryChips() {
        val categories = listOf(
            "Best Sellers" to true,  // selected by default
            "Specialty Pizza" to false,
            "Sides" to false,
            "Beverages" to false,
            "Combos" to false,
            "Desserts" to false
        )

        // Set click listeners for category chips
        categories.forEachIndexed { index, (category, isSelected) ->
            val chipId = when (index) {
                0 -> R.id.chip_best_sellers
                1 -> R.id.chip_specialty_pizza
                2 -> R.id.chip_sides
                3 -> R.id.chip_beverages
                4 -> R.id.chip_combos
                5 -> R.id.chip_desserts
                else -> return@forEachIndexed
            }

            findViewById<Chip>(chipId)?.setOnClickListener {
                onCategorySelected(category)
            }
        }
    }

    private fun setupCartButton() {
        val btnViewCart = findViewById<MaterialButton>(R.id.btn_view_cart)

        btnViewCart?.setOnClickListener {
            // Navigate to cart screen
            navigateToCart()
        }
    }

    private fun createMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem(
                name = "Margherita Classica",
                description = "San Marzano tomatoes, fresh buffalo mozzarella, basil, EVOO.",
                price = "$14.00",
                rating = "4.9 (120)",
                imageUrl = null,
                isVeg = true,
                vegIndicatorDrawable = R.drawable.ic_pentagon_128
            ),
            MenuItem(
                name = "Spicy Diavola",
                description = "Spicy salami, chili flakes, red onions, and hot honey drizzle.",
                price = "$18.50",
                rating = "4.7 (85)",
                imageUrl = null,
                isVeg = false,
                vegIndicatorDrawable = R.drawable.ic_square_128
            ),
            MenuItem(
                name = "Truffle Mushroom",
                description = "Wild forest mushrooms, black truffle oil, and ricotta cream.",
                price = "$21.00",
                rating = "4.9 (210)",
                imageUrl = null,
                isVeg = true,
                vegIndicatorDrawable = R.drawable.ic_pentagon_128
            )
        )
    }

    private fun onCategorySelected(category: String) {
        // Handle category selection
        // Filter menu items based on selected category
        // Update RecyclerView with filtered items
    }

    private fun onAddToCartClicked(menuItem: MenuItem) {
        // Handle add to cart action
        // Update cart badge count
        // Show success animation or snackbar
    }

    private fun navigateToCart() {
        // Navigate to cart screen
        // Intent to CartActivity or show bottom sheet
    }
}

// Simple RecyclerView Adapter for Menu Items
class MenuItemAdapter(
    private val menuItems: List<RestaurantDetailActivity.MenuItem>,
    private val onAddToCart: (RestaurantDetailActivity.MenuItem) -> Unit
) : RecyclerView.Adapter<MenuItemAdapter.ViewHolder>() {

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        // ViewHolder implementation would bind menu item data to views
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_food, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = menuItems[position]
        // Bind menu item data to views
        // Set click listener for add button: onAddToCart(menuItem)
    }

    override fun getItemCount() = menuItems.size
}
