package com.quick.bite.ui.checkout

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quick.bite.R
import com.quick.bite.models.CartItem
import com.quick.bite.repositories.SessionDataRepository

/**
 * CheckoutActivity - Handles the checkout flow with order summary, promo codes, and payment methods.
 *
 * Layout Architecture:
 * - Top App Bar (L4: RelativeLayout)
 * - Scrollable Content with Order Items (L3: LinearLayout)
 * - Promo Input with Icon Overlay (L5: FrameLayout)
 * - Payment Method Cards (L4: RelativeLayout)
 * - Price Breakdown Table (L7: TableLayout)
 * - Fixed Footer with Place Order Button
 */
class CheckoutActivity : AppCompatActivity() {

    companion object {
        private const val COUPON_CODE = "SAVE10"
        private const val COUPON_DISCOUNT_PERCENT = 0.10
    }

    // Order Summary Views
    private lateinit var itemOrder1: View
    private lateinit var itemOrder2: View
    private lateinit var itemOrder3: View

    // Payment Method Cards
    private lateinit var paymentCard: View
    private lateinit var paymentCash: View

    // Buttons
    private lateinit var btnBack: ImageButton
    private lateinit var btnApplyPromo: Button
    private lateinit var btnPlaceOrder: Button
    private lateinit var tvItemCount: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvDeliveryFee: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvDiscount: TextView
    private lateinit var etPromoCode: EditText
    private lateinit var tvCartEmpty: TextView
    private lateinit var layoutOrderSummaryHeader: View
    private lateinit var layoutPriceBreakdown: View

    private var isCouponApplied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        initViews()
        setupOrderItems()
        setupPaymentMethods()
        setupClickListeners()
    }

    private fun initViews() {
        // Order items
        itemOrder1 = findViewById(R.id.item_order_1)
        itemOrder2 = findViewById(R.id.item_order_2)
        itemOrder3 = findViewById(R.id.item_order_3)

        // Payment cards
        paymentCard = findViewById(R.id.rl_payment_card)
        paymentCash = findViewById(R.id.rl_payment_cash)

        // Buttons
        btnBack = findViewById(R.id.btn_back)
        btnApplyPromo = findViewById(R.id.btn_apply_promo)
        btnPlaceOrder = findViewById(R.id.btn_place_order)

        tvItemCount = findViewById(R.id.tv_item_count)
        tvSubtotal = findViewById(R.id.tv_subtotal)
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee)
        tvTotalAmount = findViewById(R.id.tv_total_amount)
        tvDiscount = findViewById(R.id.tv_discount)
        etPromoCode = findViewById(R.id.et_promo_code)
        tvCartEmpty = findViewById(R.id.tv_cart_empty)
        layoutOrderSummaryHeader = findViewById(R.id.layout_order_summary_header)
        layoutPriceBreakdown = findViewById(R.id.layout_price_breakdown)
    }

    private fun setupOrderItems() {
        val cartItems = SessionDataRepository.getCartItems()
        val orderViews = listOf(itemOrder1, itemOrder2, itemOrder3)
        val isCartEmpty = cartItems.isEmpty()

        tvCartEmpty.visibility = if (isCartEmpty) View.VISIBLE else View.GONE
        layoutOrderSummaryHeader.visibility = if (isCartEmpty) View.GONE else View.VISIBLE
        findViewById<View>(R.id.ll_order_items).visibility = if (isCartEmpty) View.GONE else View.VISIBLE
        layoutPriceBreakdown.visibility = if (isCartEmpty) View.GONE else View.VISIBLE
        btnPlaceOrder.isEnabled = !isCartEmpty
        btnPlaceOrder.alpha = if (isCartEmpty) 0.5f else 1f

        orderViews.forEachIndexed { index, view ->
            val cartItem = cartItems.getOrNull(index)
            if (cartItem == null) {
                view.visibility = View.GONE
            } else {
                view.visibility = View.VISIBLE
                setupOrderItem(view, cartItem)
            }
        }

        updatePriceSummary()
    }

    private fun setupOrderItem(itemView: View, cartItem: CartItem) {
        itemView.findViewById<ImageView>(R.id.iv_product_image).setImageResource(R.drawable.image_2)
        itemView.findViewById<TextView>(R.id.tv_product_name).text = cartItem.productName
        itemView.findViewById<TextView>(R.id.tv_product_customization).text = cartItem.description
        itemView.findViewById<TextView>(R.id.tv_product_price).text = SessionDataRepository.formatMoney(cartItem.unitPrice)
        itemView.findViewById<TextView>(R.id.tv_quantity).text = cartItem.quantity.toString()
    }

    private fun setupPaymentMethods() {
        // Credit Card is selected by default
        updatePaymentSelection(isCardSelected = true)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnApplyPromo.setOnClickListener {
            applyPromoCode()
        }

        btnPlaceOrder.setOnClickListener {
            placeOrder()
        }

        paymentCard.setOnClickListener {
            updatePaymentSelection(isCardSelected = true)
        }

        paymentCash.setOnClickListener {
            updatePaymentSelection(isCardSelected = false)
        }
    }

    private fun updatePaymentSelection(isCardSelected: Boolean) {
        if (isCardSelected) {
            // Update Card UI to selected
            paymentCard.setBackgroundResource(R.drawable.bg_payment_selected)
            paymentCard.findViewById<ImageView>(R.id.iv_check_card).visibility = View.VISIBLE
            paymentCard.findViewById<ImageView>(R.id.img_card).setColorFilter(
                getColor(R.color.primary),
                PorterDuff.Mode.SRC_IN
            )

            // Update Cash UI to unselected
            paymentCash.setBackgroundResource(R.drawable.bg_payment_unselected)
            paymentCash.findViewById<ImageView>(R.id.iv_check_cod).visibility = View.GONE
            paymentCash.findViewById<ImageView>(R.id.img_cod).setColorFilter(
                getColor(R.color.grey_text),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            // Update Cash UI to selected
            paymentCash.setBackgroundResource(R.drawable.bg_payment_selected)
            paymentCash.findViewById<ImageView>(R.id.iv_check_cod).visibility = View.VISIBLE
            paymentCash.findViewById<ImageView>(R.id.img_cod).setColorFilter(
                getColor(R.color.primary),
                PorterDuff.Mode.SRC_IN
            )


            // Update Card UI to unselected
            paymentCard.setBackgroundResource(R.drawable.bg_payment_unselected)
            paymentCard.findViewById<ImageView>(R.id.iv_check_card).visibility = View.GONE
            paymentCard.findViewById<ImageView>(R.id.img_card).setColorFilter(
                getColor(R.color.grey_text),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun applyPromoCode() {
        val enteredCode = etPromoCode.text.toString().trim()
        if (enteredCode.equals(COUPON_CODE, ignoreCase = true)) {
            isCouponApplied = true
            updatePriceSummary()
            Toast.makeText(this, "Coupon applied: 10% off", Toast.LENGTH_SHORT).show()
        } else {
            isCouponApplied = false
            updatePriceSummary()
            Toast.makeText(this, "Invalid coupon code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun placeOrder() {
        val discountPercent = if (isCouponApplied) COUPON_DISCOUNT_PERCENT else 0.0
        val order = SessionDataRepository.placeOrder(discountPercent)
        if (order == null) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updatePriceSummary() {
        val totalItems = SessionDataRepository.getCartItemCount()
        val subtotal = SessionDataRepository.getCartSubtotal()
        val deliveryFee = if (subtotal > 0.0) 2.0 else 0.0
        val discount = if (isCouponApplied) subtotal * COUPON_DISCOUNT_PERCENT else 0.0
        val total = subtotal + deliveryFee - discount

        tvItemCount.text = "$totalItems Items"
        tvSubtotal.text = SessionDataRepository.formatMoney(subtotal)
        tvDeliveryFee.text = SessionDataRepository.formatMoney(deliveryFee)
        tvDiscount.text = "-${SessionDataRepository.formatMoney(discount)}"
        tvTotalAmount.text = SessionDataRepository.formatMoney(total)
    }
}


