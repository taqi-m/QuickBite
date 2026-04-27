package com.quick.bite.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.quick.bite.R
import com.quick.bite.data.db.QuickBiteDatabaseManager
import com.quick.bite.data.repository.QuickBiteRepository
import com.quick.bite.model.Cart
import com.quick.bite.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    companion object {
        private const val SAVE10_COUPON = "SAVE10"
        private const val SAVE10_DISCOUNT_RATE = 0.10
        private const val DELIVERY_FEE = 2.00
    }

    private lateinit var repository: QuickBiteRepository
    private lateinit var rvOrderItems: RecyclerView
    private lateinit var tvItemCount: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvDeliveryFee: TextView
    private lateinit var tvDiscount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvEmptyCart: TextView
    private lateinit var etPromoCode: EditText
    private lateinit var paymentCard: View
    private lateinit var paymentCash: View
    private lateinit var btnApplyPromo: Button
    private lateinit var btnPlaceOrder: Button

    private var checkoutAdapter: CheckoutAdapter? = null
    private var cartItems: Map<String, Int> = emptyMap()
    private var menuItems: Map<Int, Item> = emptyMap() // Cache of item details
    private var subtotal = 0.0
    private var discountAmount = 0.0
    private var appliedPromoCode: String = ""
    private var isCardSelected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Initialize repository with correct constructor
        repository = QuickBiteRepository(QuickBiteDatabaseManager(this))

        setupToolbar()
        initViews()
        setupRecyclerView()
        setupPaymentMethods()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadCartData()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_checkout)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            title = getString(R.string.checkout)
        }
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.navy))
    }

    private fun initViews() {
        rvOrderItems = findViewById(R.id.rv_order_items)
        tvItemCount = findViewById(R.id.tv_item_count)
        tvSubtotal = findViewById(R.id.tv_subtotal)
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee)
        tvDiscount = findViewById(R.id.tv_discount)
        tvTotalAmount = findViewById(R.id.tv_total_amount)
        tvEmptyCart = findViewById(R.id.tv_empty_cart)
        etPromoCode = findViewById(R.id.et_promo_code)
        paymentCard = findViewById(R.id.rl_payment_card)
        paymentCash = findViewById(R.id.rl_payment_cash)
        btnApplyPromo = findViewById(R.id.btn_apply_promo)
        btnPlaceOrder = findViewById(R.id.btn_place_order)
    }

    private fun setupRecyclerView() {
        rvOrderItems.layoutManager = LinearLayoutManager(this)
        checkoutAdapter = CheckoutAdapter(
            onAddClick = { itemId -> incrementItem(itemId) },
            onRemoveClick = { itemId -> decrementItem(itemId) },
            onDeleteClick = { itemId -> deleteItem(itemId) }
        )
        rvOrderItems.adapter = checkoutAdapter
    }

    /**
     * Loads cart data from the repository.
     * Uses getCart() to fetch cart items and getItems() for item details.
     */
    private fun loadCartData() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Fetch cart from repository

            val currentUserResult = repository.getCurrentUser()
            currentUserResult.onSuccess { user ->
                val cartResult = repository.getCart(user.userID)

                cartResult.onSuccess { cart ->
                    cartItems = cart.items

                    // Fetch all items to get details for cart items
                    if (cartItems.isNotEmpty()) {
                        val itemsResult = repository.getItems()
                        itemsResult.onSuccess { items ->
                            menuItems = items.associateBy { it.itemID }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        updateUI(cart)
                    }
                }.onFailure { error ->
                    // Try local cart as fallback
                    val localCartResult = repository.getLocalCart(user.userID)
                    localCartResult.onSuccess { localItems ->
                        cartItems = localItems.associate {
                            (it["itemID"]?.toString() ?: "0") to ((it["quantity"] as? Int) ?: 0)
                        }
                        if (cartItems.isNotEmpty()) {
                            val itemsResult = repository.getItems()
                            itemsResult.onSuccess { items ->
                                menuItems = items.associateBy { it.itemID }
                            }
                        }
                        withContext(Dispatchers.Main) {
                            updateUI(Cart(userID = user.toString(), items = cartItems))
                        }
                    }.onFailure {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@CheckoutActivity,
                                "Failed to load cart: ${error.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }.onFailure {

            }

        }
    }

    /**
     * Updates the UI with cart data.
     */
    private fun updateUI(cart: Cart) {
        val cartItemIds = cart.items.keys.mapNotNull { it.toIntOrNull() }
        val totalItems = cart.items.values.sum()

        // Update item count
        tvItemCount.text = if (totalItems == 1) "1 item" else "$totalItems items"

        // Show empty state if cart is empty
        if (cart.items.isEmpty()) {
            tvEmptyCart.visibility = View.VISIBLE
            rvOrderItems.visibility = View.GONE
            btnPlaceOrder.isEnabled = false
        } else {
            tvEmptyCart.visibility = View.GONE
            rvOrderItems.visibility = View.VISIBLE
            btnPlaceOrder.isEnabled = true
        }

        // Update adapter with cart items
        checkoutAdapter?.submitList(cart, menuItems)

        // Calculate subtotal
        subtotal = cart.items.entries.sumOf { (itemIdStr, quantity) ->
            val itemId = itemIdStr.toIntOrNull() ?: return@sumOf 0.0
            val item = menuItems[itemId]
            (item?.price?.toDouble() ?: item?.price?.toDouble() ?: 0.0) * quantity
        }

        recalculateTotals()
    }

    /**
     * Increments the quantity of an item in the cart.
     */
    private fun incrementItem(itemId: Int) {

        lifecycleScope.launch {
            val currentUserResult = repository.getCurrentUser()

            currentUserResult.onSuccess { user ->
                val result = repository.addToCart(user.userID, itemId, 1)
                result.onSuccess {
                    loadCartData() // Refresh the entire cart
                }.onFailure { error ->
                    Toast.makeText(
                        this@CheckoutActivity,
                        "Failed to update quantity: ${error.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.onFailure {

            }
        }
    }

    /**
     * Decrements the quantity of an item in the cart.
     * If quantity reaches 0, removes the item.
     */
    private fun decrementItem(itemId: Int) {
        val currentQty = cartItems[itemId.toString()] ?: 0
        if (currentQty <= 1) {
            deleteItem(itemId)
        } else {
            lifecycleScope.launch {
                val currentUserResult = repository.getCurrentUser()

                currentUserResult.onSuccess { user ->

                    // Update cart with reduced quantity
                    val result = repository.addToCart(user.userID, itemId, currentQty - 1)
                    result.onSuccess {
                        loadCartData()
                    }.onFailure { error ->
                        Toast.makeText(
                            this@CheckoutActivity,
                            "Failed to update quantity: ${error.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }.onFailure {

                }
            }
        }
    }

    /**
     * Removes an item completely from the cart.
     */
    private fun deleteItem(itemId: Int) {
        lifecycleScope.launch {
            val currentUserResult = repository.getCurrentUser()

            currentUserResult.onSuccess { user ->
                val result = repository.removeCartItem(user.userID, itemId)
                result.onSuccess {
                    loadCartData()
                    Toast.makeText(this@CheckoutActivity, "Item removed from cart", Toast.LENGTH_SHORT).show()
                }.onFailure { error ->
                    Toast.makeText(
                        this@CheckoutActivity,
                        "Failed to remove item: ${error.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }.onFailure {

            }

        }
    }

    private fun setupPaymentMethods() {
        updatePaymentSelection(isCardSelected = true)
    }

    private fun setupClickListeners() {
        btnApplyPromo.setOnClickListener { applyPromoCode() }
        btnPlaceOrder.setOnClickListener { placeOrder() }
        paymentCard.setOnClickListener { updatePaymentSelection(isCardSelected = true) }
        paymentCash.setOnClickListener { updatePaymentSelection(isCardSelected = false) }
    }

    private fun updatePaymentSelection(isCardSelected: Boolean) {
        this.isCardSelected = isCardSelected
        if (isCardSelected) {
            paymentCard.setBackgroundResource(R.drawable.bg_payment_selected)
            paymentCard.findViewById<View>(R.id.fl_card_icon)
                .setBackgroundResource(R.drawable.bg_payment_icon_selected)
            paymentCard.findViewById<ImageView>(R.id.iv_check_card).visibility = View.VISIBLE
            paymentCash.setBackgroundResource(R.drawable.bg_payment_unselected)
            paymentCash.findViewById<View>(R.id.fl_cash_icon)
                .setBackgroundResource(R.drawable.bg_payment_icon_unselected)
        } else {
            paymentCash.setBackgroundResource(R.drawable.bg_payment_selected)
            paymentCash.findViewById<View>(R.id.fl_cash_icon)
                .setBackgroundResource(R.drawable.bg_payment_icon_selected)
            paymentCard.setBackgroundResource(R.drawable.bg_payment_unselected)
            paymentCard.findViewById<View>(R.id.fl_card_icon)
                .setBackgroundResource(R.drawable.bg_payment_icon_unselected)
            paymentCard.findViewById<ImageView>(R.id.iv_check_card).visibility = View.GONE
        }
    }

    private fun applyPromoCode() {
        val enteredCode = etPromoCode.text.toString().trim().uppercase(Locale.US)
        if (enteredCode == SAVE10_COUPON) {
            appliedPromoCode = enteredCode
            discountAmount = roundToTwoDecimals(subtotal * SAVE10_DISCOUNT_RATE)
            Toast.makeText(this, "SAVE10 applied: 10% off subtotal", Toast.LENGTH_SHORT).show()
        } else {
            appliedPromoCode = ""
            discountAmount = 0.0
            Toast.makeText(this, "Invalid coupon code", Toast.LENGTH_SHORT).show()
        }
        recalculateTotals()
    }

    /**
     * Places the order using the repository's placeOrder method.
     * The repository expects: placeOrder(userID: Int, orderItems: Map<String, Int>)
     */
    private fun placeOrder() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        btnPlaceOrder.isEnabled = false

        lifecycleScope.launch {
            val currentUserResult = repository.getCurrentUser()

            currentUserResult.onSuccess { user ->
                // Convert cart items to Map<String, Int> format expected by the API
                val orderItems = cartItems.mapValues { it.value }

                val result = repository.placeOrder(user.userID, orderItems)

                result.onSuccess { order ->
                    // Clear the cart after successful order
                    clearCartAfterOrder()
                    Toast.makeText(
                        this@CheckoutActivity,
                        "Order #${order.orderID} Placed Successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }.onFailure { error ->
                    btnPlaceOrder.isEnabled = true
                    Toast.makeText(
                        this@CheckoutActivity,
                        "Failed to place order: ${error.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("CheckoutActivityLog", "Order placement failed", error)
                }
            }.onFailure {

            }
        }
    }

    /**
     * Clears the cart after a successful order.
     */
    private fun clearCartAfterOrder() {
        lifecycleScope.launch {
            val currentUserResult = repository.getCurrentUser()

            currentUserResult.onSuccess { user ->
                repository.clearCart(user.userID)
            }.onFailure {

            }
        }
    }

    private fun recalculateTotals() {
        val totalAmount = roundToTwoDecimals(subtotal + DELIVERY_FEE - discountAmount)
        tvSubtotal.text = formatCurrency(subtotal)
        tvDeliveryFee.text = formatCurrency(DELIVERY_FEE)
        tvDiscount.text = String.format(Locale.US, "-$%.2f", discountAmount)
        tvTotalAmount.text = formatCurrency(totalAmount)
    }

    private fun formatCurrency(value: Double): String = String.format(Locale.US, "$%.2f", value)

    private fun roundToTwoDecimals(value: Double): Double =
        kotlin.math.round(value * 100.0) / 100.0

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
// CHECKOUT ADAPTER
// =========================

/**
 * RecyclerView adapter for displaying cart items in the checkout screen.
 * Uses the actual Cart and Item models.
 */
class CheckoutAdapter(
    private val onAddClick: (Int) -> Unit,
    private val onRemoveClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder>() {

    private var cartEntries: List<Pair<Int, Int>> = emptyList() // Pair<itemID, quantity>
    private var menuItems: Map<Int, Item> = emptyMap()

    fun submitList(cart: Cart, items: Map<Int, Item>) {
        this.menuItems = items
        this.cartEntries = cart.items.entries.mapNotNull { entry ->
            val itemId = entry.key.toIntOrNull()
            if (itemId != null) itemId to entry.value else null
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checkout_order, parent, false)
        return CheckoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        val (itemId, quantity) = cartEntries[position]
        val item = menuItems[itemId]
        holder.bind(itemId, item, quantity)
    }

    override fun getItemCount(): Int = cartEntries.size

    inner class CheckoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.iv_product_image)
        private val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val tvProductCustomization: TextView = itemView.findViewById(R.id.tv_product_customization)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tv_quantity)
        private val btnMinusQuantity: MaterialButton = itemView.findViewById(R.id.btn_minus_quantity)
        private val btnPlusQuantity: MaterialButton = itemView.findViewById(R.id.btn_plus_quantity)
        private val btnRemoveItem: MaterialButton = itemView.findViewById(R.id.btn_remove_item)

        fun bind(itemId: Int, item: Item?, quantity: Int) {
            if (item != null) {
                tvProductName.text = item.name
                tvProductCustomization.text = item.description
                tvProductPrice.text = String.format(Locale.US, "$%d", item.price)
                ivProductImage.setImageResource(getImageForItem(itemId))
            } else {
                tvProductName.text = "Item #$itemId"
                tvProductCustomization.text = "Loading..."
                tvProductPrice.text = "$0.00"
                ivProductImage.setImageResource(R.drawable.image_2)
            }

            tvQuantity.text = quantity.toString()

            btnPlusQuantity.setOnClickListener { onAddClick(itemId) }
            btnMinusQuantity.setOnClickListener { onRemoveClick(itemId) }
            btnRemoveItem.setOnClickListener { onDeleteClick(itemId) }
        }

        private fun getImageForItem(itemId: Int): Int {
            return when (itemId % 3) {
                0 -> R.drawable.img_burger
                1 -> R.drawable.img_fries
                else -> R.drawable.img_drink
            }
        }
    }
}