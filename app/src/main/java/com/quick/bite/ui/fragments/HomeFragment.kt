package com.quick.bite.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.quick.bite.model.DashboardWidget
import com.quick.bite.model.ActiveKitchen
import com.quick.bite.adapters.ActiveKitchenAdapter
import com.quick.bite.adapters.DashboardWidgetAdapter
import com.quick.bite.MainActivity
import com.quick.bite.R
import com.quick.bite.api.RetrofitClient
import com.quick.bite.data.db.QuickBiteDatabaseManager
import com.quick.bite.data.repository.QuickBiteRepository
import com.quick.bite.model.Restaurant
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var repository: QuickBiteRepository
    private lateinit var progressBar: ProgressBar
    private lateinit var rvKitchens: RecyclerView

    interface RestaurantSelectionContract {
        fun onCartClicked()
        fun onRestaurantSelected(restaurantId: Int)
    }

    private var restaurantSelectionContract: RestaurantSelectionContract? = null

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        restaurantSelectionContract = context as? RestaurantSelectionContract
    }

    override fun onDetach() {
        super.onDetach()
        restaurantSelectionContract = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = QuickBiteRepository(RetrofitClient.getApiService(), QuickBiteDatabaseManager(requireContext()))
        progressBar = view.findViewById(R.id.pb_home_loading)
        rvKitchens = view.findViewById(R.id.rv_active_kitchens)

        setupDashboardWidgets(view)
        setupCartButton(view)
    }

    override fun onResume() {
        super.onResume()
        loadRestaurants()
    }

    private fun loadRestaurants() {
        progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getRestaurants()
            progressBar.visibility = View.GONE

            result.onSuccess { restaurants ->
                updateKitchensList(restaurants)
            }.onFailure { error ->
                Toast.makeText(context, "Sync Error: Using offline data if available.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateKitchensList(restaurants: List<Restaurant>) {
        val activeKitchens = restaurants.take(15).map { restaurant ->
            ActiveKitchen(
                restaurantId = restaurant.restaurantID,
                icon = "🍽",
                name = restaurant.restaurantName,
                category = "${restaurant.type} • ${priceTierFor(2.0)}",
                rating = "4.5 ★",
                deliveryTime = "25m",
                deliveryFee = "$2.00"
            )
        }

        if (rvKitchens.adapter == null) {
            rvKitchens.layoutManager = LinearLayoutManager(requireContext())
            rvKitchens.adapter = ActiveKitchenAdapter(activeKitchens) { restaurantId ->
                restaurantSelectionContract?.onRestaurantSelected(restaurantId)
            }
        } else {
            (rvKitchens.adapter as ActiveKitchenAdapter).updateData(activeKitchens)
        }
    }

    private fun setupDashboardWidgets(view: View) {
        val rvDashboard = view.findViewById<RecyclerView>(R.id.rv_dashboard_widgets)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int = if (position == 0) 2 else 1
            }
        }
        rvDashboard.layoutManager = gridLayoutManager
        rvDashboard.adapter = DashboardWidgetAdapter(createDashboardWidgets())
    }

    private fun setupCartButton(view: View) {
        view.findViewById<MaterialButton>(R.id.btn_cart)?.setOnClickListener {
            restaurantSelectionContract?.onCartClicked()
        }
    }

    private fun createDashboardWidgets(): List<DashboardWidget> {
        return listOf(
            DashboardWidget("🔥", "Live Deals", "50% OFF", "Burgers & Wings • Ends in 2h", "CLAIM", MainActivity.WidgetBackgroundType.GRADIENT)
        )
    }

    private fun priceTierFor(deliveryFee: Double): String = if (deliveryFee <= 1.5) "$" else if (deliveryFee <= 2.5) "$$" else "$$$"
}