package com.quick.bite.ui.checkout

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.quick.bite.R

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
    }

    private fun setupOrderItems() {
        // Item 1: Gourmet Burger
        setupOrderItem(
            itemOrder1,
            R.drawable.img_burger,
            getString(R.string.item_gourmet_burger),
            getString(R.string.item_burger_customization),
            getString(R.string.item_burger_price),
            "1"
        )

        // Item 2: Large Fries
        setupOrderItem(
            itemOrder2,
            R.drawable.img_fries,
            getString(R.string.item_large_fries),
            getString(R.string.item_fries_customization),
            getString(R.string.item_fries_price),
            "1"
        )

        // Item 3: Iced Drink
        setupOrderItem(
            itemOrder3,
            R.drawable.img_drink,
            getString(R.string.item_iced_drink),
            getString(R.string.item_drink_customization),
            getString(R.string.item_drink_price),
            "1"
        )
    }

    private fun setupOrderItem(
        itemView: View,
        imageRes: Int,
        name: String,
        customization: String,
        price: String,
        quantity: String
    ) {
        itemView.findViewById<ImageView>(R.id.iv_product_image).setImageResource(imageRes)
        itemView.findViewById<TextView>(R.id.tv_product_name).text = name
        itemView.findViewById<TextView>(R.id.tv_product_customization).text = customization
        itemView.findViewById<TextView>(R.id.tv_product_price).text = price
        itemView.findViewById<TextView>(R.id.tv_quantity).text = quantity
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
        // TODO: Implement promo code validation
        Toast.makeText(this, "Promo code applied!", Toast.LENGTH_SHORT).show()
    }

    private fun placeOrder() {
        // TODO: Implement order placement logic
        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }
}


