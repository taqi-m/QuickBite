package com.quick.bite.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.quick.bite.DashboardWidgetAdapter
import com.quick.bite.R
import com.quick.bite.RestaurantDetailActivity
import com.quick.bite.adapters.RestaurantAdapter
import com.quick.bite.models.Restaurant
import com.quick.bite.repositories.DummyDashboardRepository
import com.quick.bite.repositories.DummyRestaurantRepository
import com.quick.bite.ui.checkout.CheckoutActivity

class HomeFragment : Fragment() {

    companion object {
        private const val ARG_USER_NAME = "arg_user_name"
        private const val ARG_WELCOME_MESSAGE = "arg_welcome_message"

        fun newInstance(userName: String, welcomeMessage: String): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_NAME, userName)
                    putString(ARG_WELCOME_MESSAGE, welcomeMessage)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showWelcomeMessage(view)
        setupDashboardWidgets(view)
        setupRestaurantList(view)
        setupCartButton(view)
    }

    private fun showWelcomeMessage(view: View) {
        val subtitle = view.findViewById<TextView>(R.id.tv_dashboard_subtitle)
        val userName = arguments?.getString(ARG_USER_NAME).orEmpty()
        val welcome = arguments?.getString(ARG_WELCOME_MESSAGE).orEmpty()

        subtitle.text = when {
            welcome.isNotBlank() -> welcome
            userName.isNotBlank() -> getString(R.string.welcome_template, userName)
            else -> getString(R.string.placeholder_empty)
        }
    }

    private fun setupDashboardWidgets(view: View) {
        val dashboardWidgets = DummyDashboardRepository.getDashboardWidgets()
        val rvDashboard = view.findViewById<RecyclerView>(R.id.rv_dashboard_widgets)

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) 2 else 1
            }
        }

        rvDashboard.layoutManager = gridLayoutManager
        rvDashboard.adapter = DashboardWidgetAdapter(dashboardWidgets)
    }

    private fun setupRestaurantList(view: View) {
        val restaurants = DummyRestaurantRepository.getRestaurants()
        val rvKitchens = view.findViewById<RecyclerView>(R.id.rv_active_kitchens)
        val searchInput = view.findViewById<EditText>(R.id.et_command_search)

        val adapter = RestaurantAdapter(restaurants) { selectedRestaurant ->
            openRestaurantDetail(selectedRestaurant)
        }

        rvKitchens.layoutManager = LinearLayoutManager(requireContext())
        rvKitchens.adapter = adapter

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun setupCartButton(view: View) {
        view.findViewById<MaterialButton>(R.id.btn_cart)?.setOnClickListener {
            val intent = Intent(activity, CheckoutActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openRestaurantDetail(restaurant: Restaurant) {
        val intent = Intent(requireContext(), RestaurantDetailActivity::class.java).apply {
            putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_ID, restaurant.id)
            putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_NAME, restaurant.name)
            putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_CATEGORY, restaurant.category)
            putExtra(RestaurantDetailActivity.EXTRA_RESTAURANT_RATING, restaurant.rating)
        }
        startActivity(intent)
    }
}
