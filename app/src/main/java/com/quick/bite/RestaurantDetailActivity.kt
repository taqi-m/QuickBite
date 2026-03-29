package com.quick.bite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.quick.bite.repositories.DummyRestaurantRepository
import com.quick.bite.models.Product
import com.quick.bite.repositories.SessionDataRepository

class RestaurantDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RESTAURANT_ID = "extra_restaurant_id"
        const val EXTRA_RESTAURANT_NAME = "extra_restaurant_name"
        const val EXTRA_RESTAURANT_CATEGORY = "extra_restaurant_category"
        const val EXTRA_RESTAURANT_RATING = "extra_restaurant_rating"
    }

    // Data Models for Restaurant Screen
    data class MenuItem(
        val productId: Int,
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

    private var selectedRestaurantId: Int = 1
    private var selectedRestaurantName: String = ""
    private var allProducts: List<Product> = emptyList()
    private lateinit var menuItemAdapter: MenuItemAdapter
    private var selectedType: String = ""
    private val placeholderText = "--"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.restaurant_detail_screen)

        bindRestaurantHeader()
        setupSystemBars()
        setupToolbar()
        setupMenuItems()
        setupCategoryChips()
        setupCartButton()
    }

    override fun onResume() {
        super.onResume()
        updateCartButtonLabel()
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
        allProducts = DummyRestaurantRepository.getProductsForRestaurant(selectedRestaurantId)
        val menuItems = mapProductsToMenuItems(allProducts)
        val rvMenuItems = findViewById<RecyclerView>(R.id.rv_menu_items)

        rvMenuItems.layoutManager = LinearLayoutManager(this)
        menuItemAdapter = MenuItemAdapter(menuItems) { menuItem ->
            // Handle add to cart click
            onAddToCartClicked(menuItem)
        }
        rvMenuItems.adapter = menuItemAdapter
    }

    private fun setupCategoryChips() {
        val availableTypes = allProducts.map { it.type }.distinct()
        val chipIds = listOf(
            R.id.chip_best_sellers,
            R.id.chip_specialty_pizza,
            R.id.chip_sides,
            R.id.chip_beverages,
            R.id.chip_combos,
            R.id.chip_desserts
        )

        chipIds.forEachIndexed { index, chipId ->
            val chip = findViewById<Chip>(chipId)
            val type = availableTypes.getOrNull(index)

            if (chip == null || type == null) {
                chip?.visibility = View.GONE
                return@forEachIndexed
            }

            chip.visibility = View.VISIBLE
            chip.text = type
            chip.isCheckable = true
            chip.isChecked = false
            chip.setOnClickListener { onCategorySelected(type) }
        }

        selectedType = availableTypes.firstOrNull().orEmpty()
        if (selectedType.isNotBlank()) {
            onCategorySelected(selectedType)
        }
    }

    private fun setupCartButton() {
        val btnViewCart = findViewById<MaterialButton>(R.id.btn_view_cart)
        updateCartButtonLabel()

        btnViewCart?.setOnClickListener {
            navigateToCart()
        }
    }

    private fun mapProductsToMenuItems(products: List<Product>): List<MenuItem> {
        return products.map { product ->
            MenuItem(
                productId = product.id,
                name = product.name,
                description = product.description,
                price = product.price,
                rating = product.rating,
                imageUrl = null,
                isVeg = product.isVeg,
                vegIndicatorDrawable = if (product.isVeg) R.drawable.ic_pentagon_128 else R.drawable.ic_square_128
            )
        }
    }

    private fun bindRestaurantHeader() {
        selectedRestaurantId = intent.getIntExtra(EXTRA_RESTAURANT_ID, 1)

        val name = intent.getStringExtra(EXTRA_RESTAURANT_NAME)
        val category = intent.getStringExtra(EXTRA_RESTAURANT_CATEGORY)
        val rating = intent.getStringExtra(EXTRA_RESTAURANT_RATING)

        selectedRestaurantName = name?.trim().orEmpty()

        findViewById<TextView>(R.id.tv_restaurant_name)?.text =
            selectedRestaurantName.ifBlank { placeholderText }
        findViewById<TextView>(R.id.tv_restaurant_category)?.text =
            category?.trim().orEmpty().ifBlank { placeholderText }
        findViewById<TextView>(R.id.tv_restaurant_rating)?.text =
            rating?.trim().orEmpty().ifBlank { placeholderText }
    }

    private fun onCategorySelected(category: String) {
        selectedType = category
        updateChipSelectionState()

        val filteredProducts = allProducts.filter { it.type.equals(selectedType, ignoreCase = true) }
        val productsToShow = if (filteredProducts.isEmpty()) allProducts else filteredProducts
        menuItemAdapter.updateItems(mapProductsToMenuItems(productsToShow))
    }

    private fun updateChipSelectionState() {
        val chipIds = listOf(
            R.id.chip_best_sellers,
            R.id.chip_specialty_pizza,
            R.id.chip_sides,
            R.id.chip_beverages,
            R.id.chip_combos,
            R.id.chip_desserts
        )

        chipIds.forEach { chipId ->
            findViewById<Chip>(chipId)?.let { chip ->
                chip.isChecked = chip.visibility == View.VISIBLE && chip.text.toString() == selectedType
            }
        }
    }

    private fun onAddToCartClicked(menuItem: MenuItem) {
        val selectedProduct = allProducts.firstOrNull { it.id == menuItem.productId } ?: return
        SessionDataRepository.addToCart(selectedProduct, selectedRestaurantName)
        updateCartButtonLabel()
    }

    private fun navigateToCart() {
        val intent = android.content.Intent(this, com.quick.bite.ui.checkout.CheckoutActivity::class.java)
        startActivity(intent)
    }

    private fun updateCartButtonLabel() {
        val btnViewCart = findViewById<MaterialButton>(R.id.btn_view_cart) ?: return
        val itemCount = SessionDataRepository.getCartItemCount()
        btnViewCart.text = "$itemCount ITEMS • View Cart"
    }
}

// Simple RecyclerView Adapter for Menu Items
class MenuItemAdapter(
    menuItems: List<RestaurantDetailActivity.MenuItem>,
    private val onAddToCart: (RestaurantDetailActivity.MenuItem) -> Unit
) : RecyclerView.Adapter<MenuItemAdapter.ViewHolder>() {

    private val visibleItems = menuItems.toMutableList()

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val vegIndicator: android.widget.ImageView = view.findViewById(R.id.iv_veg_indicator)
        val vegLabel: TextView = view.findViewById(R.id.tv_veg_label)
        val name: TextView = view.findViewById(R.id.tv_food_name)
        val description: TextView = view.findViewById(R.id.tv_food_description)
        val price: TextView = view.findViewById(R.id.tv_food_price)
        val rating: TextView = view.findViewById(R.id.tv_food_rating)
        val addButton: MaterialButton = view.findViewById(R.id.btn_add_to_cart)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_food, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = visibleItems[position]

        holder.vegIndicator.setImageResource(menuItem.vegIndicatorDrawable)
        holder.vegLabel.text = if (menuItem.isVeg) {
            holder.itemView.context.getString(R.string.veg_label)
        } else {
            holder.itemView.context.getString(R.string.non_veg_label)
        }
        holder.name.text = menuItem.name
        holder.description.text = menuItem.description
        holder.price.text = menuItem.price
        holder.rating.text = menuItem.rating
        holder.addButton.setOnClickListener { onAddToCart(menuItem) }
    }

    override fun getItemCount() = visibleItems.size

    fun updateItems(newItems: List<RestaurantDetailActivity.MenuItem>) {
        visibleItems.clear()
        visibleItems.addAll(newItems)
        notifyDataSetChanged()
    }
}
