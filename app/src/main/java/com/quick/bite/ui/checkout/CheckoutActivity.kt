package com.quick.bite.ui.checkout

import android.os.Bundle
import android.view.View
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.quick.bite.R
import com.quick.bite.api.RetrofitClient
import com.quick.bite.data.db.QuickBiteDatabaseManager
import com.quick.bite.data.repository.QuickBiteRepository
import com.quick.bite.model.Item
import com.quick.bite.model.MasterOrder
import com.quick.bite.model.Order
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
    private lateinit var itemOrder1: View
    private lateinit var itemOrder2: View
    private lateinit var itemOrder3: View
    private lateinit var tvItemCount: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvDeliveryFee: TextView
    private lateinit var tvDiscount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var etPromoCode: EditText
    private lateinit var paymentCard: View
    private lateinit var paymentCash: View
    private lateinit var btnApplyPromo: Button
    private lateinit var btnPlaceOrder: Button

    private data class CheckoutLineItem(val item: Item, val quantity: Int)

    private var lineItems: List<CheckoutLineItem> = emptyList()
    private var subtotal = 0.0
    private var discountAmount = 0.0
    private var appliedPromoCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        repository = QuickBiteRepository(RetrofitClient.getApiService(), QuickBiteDatabaseManager(this))

        setupToolbar()
        initViews()
        loadCartData()
        setupPaymentMethods()
        setupClickListeners()
    }

    private fun loadCartData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val cartData = repository.getCartLineItems()
            val itemCount = repository.getCartItemCount()

            withContext(Dispatchers.Main) {
                lineItems = cartData.map { CheckoutLineItem(it.first, it.second) }
                tvItemCount.text = String.format(Locale.US, "%d items", itemCount)
                setupOrderItems()
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_checkout)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.navy))
    }

    private fun initViews() {
        itemOrder1 = findViewById(R.id.item_order_1)
        itemOrder2 = findViewById(R.id.item_order_2)
        itemOrder3 = findViewById(R.id.item_order_3)
        tvItemCount = findViewById(R.id.tv_item_count)
        tvSubtotal = findViewById(R.id.tv_subtotal)
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee)
        tvDiscount = findViewById(R.id.tv_discount)
        tvTotalAmount = findViewById(R.id.tv_total_amount)
        etPromoCode = findViewById(R.id.et_promo_code)
        paymentCard = findViewById(R.id.rl_payment_card)
        paymentCash = findViewById(R.id.rl_payment_cash)
        btnApplyPromo = findViewById(R.id.btn_apply_promo)
        btnPlaceOrder = findViewById(R.id.btn_place_order)
    }

    private fun setupOrderItems() {
        val itemViews = listOf(itemOrder1, itemOrder2, itemOrder3)
        itemViews.forEachIndexed { index, itemView ->
            val lineItem = lineItems.getOrNull(index)
            if (lineItem == null) {
                itemView.visibility = View.GONE
            } else {
                itemView.visibility = View.VISIBLE
                setupOrderItem(
                    itemView = itemView,
                    lineItem = lineItem,
                    imageRes = resolveDrawableByName(lineItem.item.imageUrl, imageForItem(index)),
                    onPlusClicked = {
                        repository.addToCart(lineItem.item)
                        loadCartData()
                    },
                    onMinusClicked = {
                        repository.decreaseCartItem(lineItem.item.itemID)
                        loadCartData()
                    },
                    onRemoveClicked = {
                        repository.removeFromCart(lineItem.item.itemID)
                        loadCartData()
                    }
                )
            }
        }

        subtotal = roundToTwoDecimals(lineItems.sumOf { it.item.itemPrice * it.quantity })
        recalculateTotals()
    }

    private fun setupOrderItem(itemView: View, lineItem: CheckoutLineItem, imageRes: Int, onPlusClicked: () -> Unit, onMinusClicked: () -> Unit, onRemoveClicked: () -> Unit) {
        itemView.findViewById<ImageView>(R.id.iv_product_image).setImageResource(imageRes)
        itemView.findViewById<TextView>(R.id.tv_product_name).text = lineItem.item.itemName
        itemView.findViewById<TextView>(R.id.tv_product_customization).text = lineItem.item.itemDescription
        itemView.findViewById<TextView>(R.id.tv_product_price).text = formatCurrency(lineItem.item.itemPrice)
        itemView.findViewById<TextView>(R.id.tv_quantity).text = lineItem.quantity.toString()

        itemView.findViewById<MaterialButton>(R.id.btn_plus_quantity).setOnClickListener { onPlusClicked() }
        itemView.findViewById<MaterialButton>(R.id.btn_minus_quantity).setOnClickListener { onMinusClicked() }
        itemView.findViewById<MaterialButton>(R.id.btn_remove_item).setOnClickListener { onRemoveClicked() }
    }

    private fun setupPaymentMethods() = updatePaymentSelection(isCardSelected = true)

    private fun setupClickListeners() {
        btnApplyPromo.setOnClickListener { applyPromoCode() }
        btnPlaceOrder.setOnClickListener { placeOrder() }
        paymentCard.setOnClickListener { updatePaymentSelection(isCardSelected = true) }
        paymentCash.setOnClickListener { updatePaymentSelection(isCardSelected = false) }
    }

    private fun updatePaymentSelection(isCardSelected: Boolean) {
        if (isCardSelected) {
            paymentCard.setBackgroundResource(R.drawable.bg_payment_selected)
            paymentCard.findViewById<View>(R.id.fl_card_icon).setBackgroundResource(R.drawable.bg_payment_icon_selected)
            paymentCard.findViewById<ImageView>(R.id.iv_check_card).visibility = View.VISIBLE
            paymentCash.setBackgroundResource(R.drawable.bg_payment_unselected)
            paymentCash.findViewById<View>(R.id.fl_cash_icon).setBackgroundResource(R.drawable.bg_payment_icon_unselected)
        } else {
            paymentCash.setBackgroundResource(R.drawable.bg_payment_selected)
            paymentCash.findViewById<View>(R.id.fl_cash_icon).setBackgroundResource(R.drawable.bg_payment_icon_selected)
            paymentCard.setBackgroundResource(R.drawable.bg_payment_unselected)
            paymentCard.findViewById<View>(R.id.fl_card_icon).setBackgroundResource(R.drawable.bg_payment_icon_unselected)
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

    private fun placeOrder() {
        if (lineItems.isEmpty()) return

        val totalAmount = roundToTwoDecimals(subtotal + DELIVERY_FEE - discountAmount)
        val user = repository.getCurrentUser()

        val masterOrder = MasterOrder(
            masterID = 0,
            userID = user?.userEmail ?: "tech@example.com",
            usercode = user?.usercode ?: "",
            restaurantID = lineItems.first().item.restaurantID, // Dynamically pull Restaurant ID from Cart
            grandtotal = totalAmount
        )

        val orderItems = lineItems.map {
            Order(
                orderID = 0,
                userID = masterOrder.userID,
                itemName = it.item.itemName,
                quantity = it.quantity,
                itemPrice = it.item.itemPrice,
                totalPrice = it.item.itemPrice * it.quantity,
                masterID = 0
            )
        }

        lifecycleScope.launch(Dispatchers.Main) {
            btnPlaceOrder.isEnabled = false
            val result = repository.placeOrder(masterOrder, orderItems)

            result.onSuccess { masterId ->
                repository.clearCart()
                Toast.makeText(this@CheckoutActivity, "Order #$masterId Placed!", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {
                btnPlaceOrder.isEnabled = true
                Toast.makeText(this@CheckoutActivity, "Error saving order.", Toast.LENGTH_SHORT).show()
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

    private fun imageForItem(index: Int): Int = when (index) {
        0 -> R.drawable.img_burger
        1 -> R.drawable.img_fries
        else -> R.drawable.img_drink
    }

    private fun resolveDrawableByName(drawableName: String, fallbackResId: Int): Int {
        val resolvedId = resources.getIdentifier(drawableName, "drawable", packageName)
        return if (resolvedId != 0) resolvedId else fallbackResId
    }

    private fun formatCurrency(value: Double): String = String.format(Locale.US, "$%.2f", value)
    private fun roundToTwoDecimals(value: Double): Double = kotlin.math.round(value * 100.0) / 100.0

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