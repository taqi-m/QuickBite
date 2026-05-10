package com.quick.bite.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.quick.bite.data.db.QuickBiteDatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(
    private val dbManager: QuickBiteDatabaseManager,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user ?: throw Exception("User is null")
                
                // Sync with local DB
                dbManager.syncUser(
                    mapOf(
                        "userID" to 1L, // Using a fixed ID for local single-session
                        "username" to (user.email ?: "User"),
                        "password" to password
                    )
                )
                user
            }
        }

    suspend fun signUpWithEmail(email: String, password: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user ?: throw Exception("User is null")
                
                // Sync with local DB
                dbManager.syncUser(
                    mapOf(
                        "userID" to 1L,
                        "username" to (user.email ?: "User"),
                        "password" to password
                    )
                )
                user
            }
        }

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> =
        withContext(Dispatchers.IO) {
            runCatching {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user ?: throw Exception("User is null")
                
                // Sync with local DB
                dbManager.syncUser(
                    mapOf(
                        "userID" to 1L,
                        "username" to (user.displayName ?: user.email ?: "Google User"),
                        "password" to null
                    )
                )
                user
            }
        }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val currentUserId = dbManager.getCurrentUserId()
            if (currentUserId != null) {
                dbManager.logoutUser(currentUserId)
            }
            auth.signOut()
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isLoggedIn(): Boolean = auth.currentUser != null
}
