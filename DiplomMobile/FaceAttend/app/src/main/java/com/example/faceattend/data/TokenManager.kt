package com.example.faceattend.data

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? = prefs.getString("jwt_token", null)

    fun saveRole(role: String) {
        prefs.edit().putString("user_role", role).apply()
    }

    fun getRole(): String? = prefs.getString("user_role", null)

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun saveUserId(userId: Int) {
        prefs.edit().putInt("user_id", userId).apply()
    }

    fun getUserId(): Int = prefs.getInt("user_id", 0)

    fun saveExpiration(expiration: String) { prefs.edit().putString("token_expiration", expiration).apply() }
    fun getExpiration(): String? = prefs.getString("token_expiration", null)

    fun isTokenValid(): Boolean {
        val token = getToken()
        if (token.isNullOrEmpty()) return false
        val expirationStr = getExpiration()
        if (expirationStr.isNullOrEmpty()) return false
        return try {
            val expiration = java.time.Instant.parse(expirationStr)
            expiration.isAfter(java.time.Instant.now())
        } catch (e: Exception) {
            false
        }
    }

    fun isLoggedIn(): Boolean = isTokenValid()
}