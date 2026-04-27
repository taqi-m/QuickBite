package com.quick.bite.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quick.bite.MainActivity
import com.quick.bite.R
import com.quick.bite.adapters.ActiveKitchenAdapter
import com.quick.bite.api.RetrofitClient
import com.quick.bite.data.db.QuickBiteDatabaseManager
import com.quick.bite.data.repository.QuickBiteRepository
import com.quick.bite.model.ActiveKitchen
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var repository: QuickBiteRepository
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var rvResults: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Repository
        repository = QuickBiteRepository(
            RetrofitClient.getApiService(),
            QuickBiteDatabaseManager(requireContext())
        )

        progressBar = view.findViewById(R.id.pb_search_loading)
        etSearch = view.findViewById(R.id.et_search_query)
        rvResults = view.findViewById(R.id.rv_search_results)
        val btnSearch = view.findViewById<ImageButton>(R.id.btn_execute_search)

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }
    }

    private fun performSearch(query: String) {
        progressBar.visibility = View.VISIBLE

        // Use viewLifecycleOwner to prevent memory leaks or crashes if the user navigates away
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Execute blazing-fast local database search
                val restaurants = repository.searchRestaurants(query)

                progressBar.visibility = View.GONE

                if (restaurants.isEmpty()) {
                    Toast.makeText(context, "No restaurants found matching '$query'", Toast.LENGTH_SHORT).show()
                }

                displayResults(restaurants)

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Search failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayResults(restaurants: List<com.quick.bite.model.Restaurant>) {
        // Map domain Restaurant model to ActiveKitchen UI model
        val mappedResults = restaurants.map {
            ActiveKitchen(
                restaurantId = it.restaurantID,
                icon = "🔍",
                name = it.restaurantName,
                category = it.type,
                rating = "4.0 ★",
                deliveryTime = "30m",
                deliveryFee = "FREE"
            )
        }

        // Initialize or update the adapter cleanly
        if (rvResults.adapter == null) {
            rvResults.layoutManager = LinearLayoutManager(requireContext())
            rvResults.adapter = ActiveKitchenAdapter(mappedResults) { restaurantId ->
                // TODO: Implement navigation to restaurant details using the restaurantId
            }
        } else {
            (rvResults.adapter as ActiveKitchenAdapter).updateData(mappedResults)
        }
    }
}