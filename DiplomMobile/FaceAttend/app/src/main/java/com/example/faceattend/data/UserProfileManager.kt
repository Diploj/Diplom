package com.example.faceattend.data

import android.content.Context
import android.content.SharedPreferences
import com.example.faceattend.api.response.UserProfile
import kotlin.let

class UserProfileManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

    fun saveProfile(profile: UserProfile) {
        with(prefs.edit()) {
            putInt("user_id", profile.userId)
            putString("name", profile.name)
            putString("surname", profile.surname)
            putString("patronymic", profile.patronymic)
            putString("email", profile.email)
            putString("role", profile.role)
            profile.studentIdNumber?.let { putString("student_id_number", it) }
            profile.groupId?.let { putInt("group_id", it) }
            profile.department?.let { putString("department", it) }
            apply()
        }
    }

    fun getProfile(): UserProfile? {
        val userId = prefs.getInt("user_id", 0)
        if (userId == 0) return null
        return UserProfile(
            userId = userId,
            name = prefs.getString("name", "") ?: "",
            surname = prefs.getString("surname", "") ?: "",
            patronymic = prefs.getString("patronymic", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            role = prefs.getString("role", "") ?: "",
            studentIdNumber = prefs.getString("student_id_number", null),
            groupId = if (prefs.contains("group_id")) prefs.getInt("group_id", 0) else null,
            department = prefs.getString("department", null)
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}