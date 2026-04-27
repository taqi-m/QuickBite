package com.quick.bite.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem as ToolbarMenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.quick.bite.R
import com.quick.bite.data.db.QuickBiteDatabaseManager
import com.quick.bite.data.repository.QuickBiteRepository
import com.quick.bite.model.Item
import com.quick.bite.model.Restaurant
import kotlinx.coroutines.launch
import java.util.Locale

class RestaurantDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RESTAURANT_ID = "extra_restaurant_id"
    }

    private lateinit var repository: QuickBiteRepository
    private lateinit var toolbar: Toolbar
    private lateinit var progressBar: ProgressBar
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var btnViewCart: MaterialButton
    private lateinit var rvMenuItems: RecyclerView
    private lateinit var tvRestaurantName: TextView
    private lateinit var tvRestaurantCategory: TextView
    private lateinit var tvRestaurantRating: TextView
    private lateinit var tvRatingCount: TextView
    private lateinit var tvDeliveryTime: TextView
    private lateinit var tvDeliveryFee: TextView
    private lateinit var tvRestaurantDistance: TextView
    private lateinit var ivRestaurantHero: ImageView

    private var menuItemAdapter: MenuItemAdapter? = null
    private var allMenuItems: List<Item> = emptyList()
    private var currentRestaurant: Restaurant? = null
    private var cartItemCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.restaurant_detail_screen)

        // Initialize repository with correct constructor
        repository = QuickBiteRepository(QuickBiteDatabaseManager(this))

        initViews()
        setupSystemBars()
        setupToolbar()
        setupCartButton()
        setupRecyclerView()

        val restaurantId = intent.getIntExtra(EXTRA_RESTAURANT_ID, -1)
        if (restaurantId != -1) {
            loadRestaurantData(restaurantId)
        } else {
            Toast.makeText(this, "Invalid Restaurant ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh cart state every time the user comes back to this screen
        refreshCartCount()
    }

    private fun initViews() {
        progressBar = findViewById(R.id.pb_detail_loading)
        categoryChipGroup = findViewById(R.id.chip_group_categories)
        btnViewCart = findViewById(R.id.btn_view_cart)
        toolbar = findViewById(R.id.toolbar)
        rvMenuItems = findViewById(R.id.rv_menu_items)
        tvRestaurantName = findViewById(R.id.tv_restaurant_name)
        tvRestaurantCategory = findViewById(R.id.tv_restaurant_category)
        tvRestaurantRating = findViewById(R.id.tv_restaurant_rating)
        tvRatingCount = findViewById(R.id.tv_rating_count)
        tvDeliveryTime = findViewById(R.id.tv_delivery_time)
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee)
        tvRestaurantDistance = findViewById(R.id.tv_restaurant_distance)
        ivRestaurantHero = findViewById(R.id.iv_restaurant_hero)
    }

    private fun setupRecyclerView() {
        rvMenuItems.layoutManager = LinearLayoutManager(this)
        menuItemAdapter = MenuItemAdapter(emptyList()) { item ->
            onAddToCartClicked(item)
        }
        rvMenuItems.adapter = menuItemAdapter
    }

    /**
     * Loads restaurant details and menu items using the repository.
     * Uses getRestaurantById() for single restaurant and getItemsByRestaurant() for menu items.
     */
    private fun loadRestaurantData(restaurantId: Int) {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            // Fetch restaurant details and menu items in parallel
            val restaurantResult = repository.getRestaurantById(restaurantId)
            val menuResult = repository.getItemsByRestaurant(restaurantId)

            progressBar.visibility = View.GONE

            // Handle restaurant details
            restaurantResult.onSuccess { restaurant ->
                currentRestaurant = restaurant
                bindRestaurantInfo(restaurant)
            }.onFailure { error ->
                Toast.makeText(
                    this@RestaurantDetailActivity,
                    "Error loading restaurant: ${error.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Handle menu items
            menuResult.onSuccess { items ->
                allMenuItems = items
                updateMenuItems(items)
                setupCategoryChips(items)
            }.onFailure { error ->
                Toast.makeText(
                    this@RestaurantDetailActivity,
                    "Error loading menu: ${error.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Refresh cart count after loading
            refreshCartCount()
        }
    }

    /**
     * Binds restaurant data to the UI views using the actual Restaurant model properties.
     * Restaurant model has: restaurantID, name, imageUrl, category, rating, deliveryTime
     */
    private fun bindRestaurantInfo(restaurant: Restaurant) {
        tvRestaurantName.text = restaurant.name
        tvRestaurantCategory.text = restaurant.category

        // Display rating
        tvRestaurantRating.text = String.format(Locale.US, "%.1f", restaurant.rating)

        // Display delivery time from model
        tvDeliveryTime.text = "${restaurant.deliveryTime} min"

        // Set delivery fee based on rating (as a placeholder logic since parkingLot doesn't exist)
        tvDeliveryFee.text = if (restaurant.rating >= 4.0) "FREE" else "$2.00"

        // Set distance placeholder (not in model)
        tvRestaurantDistance.text = "1.2 km"

        // Set rating count placeholder
        tvRatingCount.text = "(500+ reviews)"

        // Set hero image
        ivRestaurantHero.setImageResource(R.drawable.restaurant_cover)
    }

    /**
     * Updates the menu items in the RecyclerView.
     */
    private fun updateMenuItems(items: List<Item>) {
        menuItemAdapter?.updateData(items)
    }

    /**
     * Sets up category chips based on unique typeLabels from menu items.
     * The Item model has a `typeLabel` field which can be used for categorization.
     */
    private fun setupCategoryChips(items: List<Item>) {
        val typeLabels = items
            .map { it.typeLabel }
            .distinct()
            .sorted()

        categoryChipGroup.removeAllViews()

        // Add "ALL" chip
        val allChip = Chip(this).apply {
            text = "ALL"
            isCheckable = true
            isChecked = true
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    filterMenuByCategory(null)
                }
            }
        }
        categoryChipGroup.addView(allChip)

        // Add category chips
        typeLabels.forEach { label ->
            val chip = Chip(this).apply {
                text = label
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        filterMenuByCategory(label)
                    }
                }
            }
            categoryChipGroup.addView(chip)
        }
    }

    /**
     * Filters menu items by typeLabel category.
     */
    private fun filterMenuByCategory(category: String?) {
        val filteredItems = if (category == null) {
            allMenuItems
        } else {
            allMenuItems.filter { it.typeLabel.equals(category, ignoreCase = true) }
        }
        menuItemAdapter?.updateData(filteredItems)
    }

    /**
     * Handles adding an item to the cart via the repository.
     * Uses the actual repository method signature: addToCart(userID: Int, itemID: Int, quantity: Int)
     */
    private fun onAddToCartClicked(item: Item) {
        lifecycleScope.launch {
            // Get the complete user object, not just the ID
            val currentUserResult = repository.getCurrentUser()

            currentUserResult.onSuccess { user ->
                // user.userID is Long: 1777500255961
                val result = repository.addToCart(user.userID, item.itemID, 1)
                result.onSuccess {
                    refreshCartCount()
                    Toast.makeText(
                        this@RestaurantDetailActivity,
                        "${item.name} added to cart",
                        Toast.LENGTH_SHORT
                    ).show()
                }.onFailure { error ->
                    Toast.makeText(
                        this@RestaurantDetailActivity,
                        "Failed to add to cart: ${error.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("DetailActivity", "Cart error", error)
                }
            }.onFailure { error ->
                Toast.makeText(
                    this@RestaurantDetailActivity,
                    "User not logged in: ${error.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("DetailActivity", "No user found", error)
            }
        }
    }

    /**
     * Refreshes the cart count from the repository.
     */
    private fun refreshCartCount() {
        lifecycleScope.launch {
            val currentUserResult = repository.getCurrentUser()

            currentUserResult.onSuccess { user ->
                val cartResult = repository.getCart(user.userID)
                cartResult.onSuccess { cart ->
                    cartItemCount = cart.items.values.sum()
                    updateCartButtonState()
                }.onFailure { error ->
                    // If cart fetch fails, try local cart
                    val localCartResult = repository.getLocalCart(user.userID)
                    localCartResult.onSuccess { cartItems ->
                        cartItemCount = cartItems.sumOf { (it["quantity"] as? Int) ?: 0 }
                        updateCartButtonState()
                    }.onFailure {
                        Log.e("DetailActivity", "Failed to get cart", error)
                        btnViewCart.visibility = View.GONE
                    }
                }
            }.onFailure { error ->
                Log.e("DetailActivity", "No user found for cart refresh", error)
                btnViewCart.visibility = View.GONE
            }
        }
    }

    /**
     * Sets up the floating "View Cart" button.
     */
    private fun setupCartButton() {
        btnViewCart.setOnClickListener {
            // Navigate to CheckoutActivity or CartFragment
//            Toast.makeText(this, "Cart clicked - navigate to cart screen", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, CheckoutActivity::class.java))
        }
        updateCartButtonState()
    }

    /**
     * Updates the cart button visibility and text based on cart count.
     */
    private fun updateCartButtonState() {
        if (cartItemCount > 0) {
            btnViewCart.visibility = View.VISIBLE
            btnViewCart.text = "View Cart ($cartItemCount items)"
        } else {
            btnViewCart.visibility = View.GONE
        }
    }

    /**
     * Sets up the toolbar with back navigation.
     */
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))
    }

    /**
     * Handles system bar insets for edge-to-edge display.
     */
    private fun setupSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.app_bar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_restaurant_detail_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: ToolbarMenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

// =========================
// MENU ITEM ADAPTER
// =========================

/**
 * RecyclerView adapter for displaying menu items.
 * Uses the actual Item model properties:
 * Item(itemID, restaurantID, name, description, imageUrl, typeLabel, price, itemRating)
 */
class MenuItemAdapter(
    private var items: List<Item>,
    private val onAddToCart: (Item) -> Unit
) : RecyclerView.Adapter<MenuItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tv_food_name)
        val description: TextView = view.findViewById(R.id.tv_food_description)
        val price: TextView = view.findViewById(R.id.tv_food_price)
        val addButton: MaterialButton = view.findViewById(R.id.btn_add_to_cart)
        val typeLabel: TextView = view.findViewById(R.id.tv_type_label)
        val rating: TextView = view.findViewById(R.id.tv_food_rating)
        val image: ImageView = view.findViewById(R.id.iv_food_image)
        val vegIndicator: ImageView = view.findViewById(R.id.iv_veg_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_food, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Bind item data using actual model properties
        holder.name.text = item.name
        holder.description.text = item.description

        // Format price as currency (model has price as Int)
        holder.price.text = "$${item.price}"

        // Set type label (e.g., "Veg", "Non-Veg", "Beverage")
        holder.typeLabel.text = item.typeLabel

        // Set rating
        holder.rating.text = String.format(Locale.US, "%.1f (120)", item.itemRating)

        // Set veg/non-veg indicator based on typeLabel
        if (item.typeLabel.equals("Veg", ignoreCase = true)) {
            holder.vegIndicator.setImageResource(R.drawable.ic_pentagon_128)
            holder.typeLabel.text = "VEG"
        } else {
            holder.vegIndicator.setImageResource(R.drawable.ic_pentagon_128)
            holder.typeLabel.text = item.typeLabel.uppercase()
        }

        // Set food image (placeholder if no image URL)
        holder.image.setImageResource(R.drawable.image_2)

        // Set click listener for add to cart button
        holder.addButton.setOnClickListener {
            onAddToCart(item)
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * Updates the adapter data and refreshes the view.
     */
    fun updateData(newItems: List<Item>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}