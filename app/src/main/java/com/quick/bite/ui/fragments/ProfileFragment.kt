package com.quick.bite.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.quick.bite.R
import com.quick.bite.data.db.QuickBiteDatabaseManager
import com.quick.bite.data.repository.QuickBiteRepository
import com.quick.bite.ui.activities.LoginActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var repository: QuickBiteRepository

    private lateinit var btnSignOut: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSignOut = view.findViewById(R.id.btn_sign_out)
        repository = QuickBiteRepository(QuickBiteDatabaseManager(requireContext()))

        val tvName = view.findViewById<TextView>(R.id.tv_profile_name)


        try {
            repository.getCurrentUser().onSuccess {
                tvName.text = it.username ?: "Unknown User"
            }.onFailure {
                throw Exception()
            }
        } catch (e: Exception) {
            tvName.text = "Error loading email"
        }

        handleLogout()
    }


    fun handleLogout() {
        btnSignOut.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch{
                repository.logout().onSuccess {
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }.onFailure {
                    Toast.makeText(context, "Logout failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}