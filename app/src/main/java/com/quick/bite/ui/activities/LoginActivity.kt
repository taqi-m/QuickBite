package com.quick.bite.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.quick.bite.R
import com.quick.bite.data.db.QuickBiteDatabaseManager
import com.quick.bite.data.repository.QuickBiteRepository
import com.quick.bite.model.User
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_USERNAME = "extra_username"
        private const val MIN_PASSWORD_LENGTH = 6
    }

    private lateinit var repository: QuickBiteRepository

    // Views
    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var tvToggleMode: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var progressBar: ProgressBar

    // State
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize repository
        repository = QuickBiteRepository(QuickBiteDatabaseManager(this))

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        tilUsername = findViewById(R.id.til_username)
        tilPassword = findViewById(R.id.til_password)
        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        btnRegister = findViewById(R.id.btn_register)
        tvToggleMode = findViewById(R.id.tv_toggle_mode)
        tvTitle = findViewById(R.id.tv_title)
        tvSubtitle = findViewById(R.id.tv_subtitle)
        progressBar = findViewById(R.id.pb_loading)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performRegister()
            }
        }

        btnRegister.setOnClickListener {
            toggleMode()
        }

        tvToggleMode.setOnClickListener {
            toggleMode()
        }
    }

    /**
     * Toggles between login and register mode.
     */
    private fun toggleMode() {
        isLoginMode = !isLoginMode

        if (isLoginMode) {
            // Switch to Login mode
            tvTitle.text = getString(R.string.welcome_back)
            tvSubtitle.text = getString(R.string.login_subtitle)
            btnLogin.text = getString(R.string.login)
            btnRegister.text = getString(R.string.create_account)
            tvToggleMode.text = getString(R.string.dont_have_account)
            tilPassword.isPasswordVisibilityToggleEnabled = false
        } else {
            // Switch to Register mode
            tvTitle.text = getString(R.string.create_account_title)
            tvSubtitle.text = getString(R.string.register_subtitle)
            btnLogin.text = getString(R.string.register)
            btnRegister.text = getString(R.string.back_to_login)
            tvToggleMode.text = getString(R.string.already_have_account)
            tilPassword.isPasswordVisibilityToggleEnabled = true
        }

        // Clear fields and errors
        etUsername.text?.clear()
        etPassword.text?.clear()
        tilUsername.error = null
        tilPassword.error = null
    }

    /**
     * Validates input fields.
     * Returns true if valid, false otherwise.
     */
    private fun validateInput(): Boolean {
        var isValid = true

        // Validate username
        val username = etUsername.text.toString().trim()
        if (TextUtils.isEmpty(username)) {
            tilUsername.error = getString(R.string.error_username_required)
            isValid = false
        } else if (username.length < 3) {
            tilUsername.error = getString(R.string.error_username_too_short)
            isValid = false
        } else {
            tilUsername.error = null
        }

        // Validate password
        val password = etPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            tilPassword.error = getString(R.string.error_password_required)
            isValid = false
        } else if (password.length < MIN_PASSWORD_LENGTH) {
            tilPassword.error = getString(R.string.error_password_too_short, MIN_PASSWORD_LENGTH)
            isValid = false
        } else {
            tilPassword.error = null
        }

        return isValid
    }

    /**
     * Performs login using the repository.
     */
    private fun performLogin() {
        if (!validateInput()) return

        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()

        setLoading(true)

        lifecycleScope.launch {
            val result = repository.login(username, password)

            result.onSuccess { user ->
                setLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.welcome_user, user.username),
                    Toast.LENGTH_SHORT
                ).show()
                navigateToMain(user)
            }.onFailure { error ->
                setLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    error.localizedMessage ?: getString(R.string.error_login_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Performs registration using the repository.
     */
    private fun performRegister() {
        if (!validateInput()) return

        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()

        setLoading(true)

        lifecycleScope.launch {
            val result = repository.register(username, password)

            result.onSuccess { user ->
                setLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.account_created, user.username),
                    Toast.LENGTH_SHORT
                ).show()
                navigateToMain(user)
            }.onFailure { error ->
                setLoading(false)
                // Check if username already exists
                val message = error.localizedMessage ?: getString(R.string.error_registration_failed)
                if (message.contains("409") || message.contains("already exists", ignoreCase = true)) {
                    tilUsername.error = getString(R.string.error_username_taken)
                } else {
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Enables or disables loading state.
     */
    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
        btnRegister.isEnabled = !isLoading
        etUsername.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
    }

    /**
     * Navigates to the main activity after successful login/register.
     */
    private fun navigateToMain(user: User) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_USER_ID, user.userID)
            putExtra(EXTRA_USERNAME, user.username)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}