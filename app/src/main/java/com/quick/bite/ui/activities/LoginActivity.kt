package com.quick.bite.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.quick.bite.R
import com.quick.bite.data.db.QuickBiteDatabaseManager
import com.quick.bite.data.repository.AuthRepository
import com.quick.bite.ui.compose.LoginScreen
import com.quick.bite.ui.theme.QuickBiteTheme
import kotlinx.coroutines.launch
import java.util.UUID

class LoginActivity : ComponentActivity() {

    companion object {
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_USERNAME = "extra_username"
    }

    private lateinit var authRepository: AuthRepository
    
    // State for Compose
    private var isLoading by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val dbManager = QuickBiteDatabaseManager(this)
        authRepository = AuthRepository(dbManager)

        // Session check
        if (authRepository.isLoggedIn()) {
            val user = authRepository.getCurrentUser()
            navigateToMain(user?.displayName ?: user?.email ?: "User")
            return
        }

        setContent {
            QuickBiteTheme {
                LoginScreen(
                    onLogin = { email, pass -> performLogin(email, pass) },
                    onRegister = { email, pass -> performRegister(email, pass) },
                    onGoogleSignIn = { startGoogleSignIn() },
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                )
            }
        }
    }

    private fun performLogin(email: String, pass: String) {
        isLoading = true
        errorMessage = null
        lifecycleScope.launch {
            authRepository.signInWithEmail(email, pass)
                .onSuccess { user ->
                    isLoading = false
                    navigateToMain(user.displayName ?: user.email ?: "User")
                }
                .onFailure { error ->
                    isLoading = false
                    errorMessage = error.localizedMessage ?: getString(R.string.error_login_failed)
                }
        }
    }

    private fun performRegister(email: String, pass: String) {
        isLoading = true
        errorMessage = null
        lifecycleScope.launch {
            authRepository.signUpWithEmail(email, pass)
                .onSuccess { user ->
                    isLoading = false
                    navigateToMain(user.displayName ?: user.email ?: "User")
                }
                .onFailure { error ->
                    isLoading = false
                    errorMessage = error.localizedMessage ?: getString(R.string.error_registration_failed)
                }
        }
    }

    private fun startGoogleSignIn() {
        val credentialManager = CredentialManager.create(this)

        // Generating a nonce (number used once) for security, 
        // though Firebase Auth doesn't strictly require it for this flow, 
        // some devices/Play Services versions might expect it.
        val nonce = UUID.randomUUID().toString()

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setNonce(nonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(this@LoginActivity, request)
                handleSignInResult(result)
            } catch (e: GetCredentialException) {
                // Better logging for debugging
                android.util.Log.e("LoginActivity", "Credential Manager Error: ${e.message}", e)
                errorMessage = e.localizedMessage ?: getString(R.string.error_google_sign_in_failed)
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Unexpected Error: ${e.message}", e)
                errorMessage = e.localizedMessage ?: getString(R.string.error_google_sign_in_failed)
            }
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken
            
            isLoading = true
            lifecycleScope.launch {
                authRepository.signInWithGoogle(idToken)
                    .onSuccess { user ->
                        isLoading = false
                        navigateToMain(user.displayName ?: user.email ?: "User")
                    }
                    .onFailure { error ->
                        isLoading = false
                        errorMessage = error.localizedMessage ?: getString(R.string.error_google_sign_in_failed)
                    }
            }
        }
    }

    private fun navigateToMain(username: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_USER_ID, 1L) // Fixed ID for local session
            putExtra(EXTRA_USERNAME, username)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
