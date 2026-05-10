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
import com.quick.bite.data.repository.AuthRepository
import com.quick.bite.ui.activities.LoginActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var authRepository: AuthRepository

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
        authRepository = AuthRepository(QuickBiteDatabaseManager(requireContext()))

        val tvName = view.findViewById<TextView>(R.id.tv_profile_name)

        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            tvName.text = currentUser.displayName ?: currentUser.email ?: "Unknown User"
        } else {
            tvName.text = "Not Signed In"
        }

        handleLogout()
    }


    fun handleLogout() {
        btnSignOut.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch{
                authRepository.signOut().onSuccess {
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
