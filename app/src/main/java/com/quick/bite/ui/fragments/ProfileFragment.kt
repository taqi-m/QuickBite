package com.quick.bite.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.quick.bite.R
import com.quick.bite.api.RetrofitClient
import com.quick.bite.data.db.QuickBiteDatabaseManager
import com.quick.bite.data.repository.QuickBiteRepository
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var repository: QuickBiteRepository
    private lateinit var tvOrdersCount: TextView

    private val currentUserEmail = "tech@example.com"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = QuickBiteRepository(RetrofitClient.getApiService(), QuickBiteDatabaseManager(requireContext()))

        val tvEmail = view.findViewById<TextView>(R.id.tv_profile_email)
        tvOrdersCount = view.findViewById(R.id.tv_orders_count)

        tvEmail.text = currentUserEmail
    }

    override fun onResume() {
        super.onResume()
        loadUserStats()
    }

    private fun loadUserStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = repository.getOrderHistory(currentUserEmail)
            result.onSuccess { masterOrders ->
                tvOrdersCount.text = masterOrders.size.toString()
            }
        }
    }
}