package com.example.faceattend.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceattend.api.repository.CourseRepository
import com.example.faceattend.api.response.CourseDto
import com.example.faceattend.api.response.UserProfile
import com.example.faceattend.data.UserProfileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CoursesViewModel(
    private val courseRepository: CourseRepository,
    private val userProfileManager: UserProfileManager
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _courses = MutableStateFlow<List<CourseDto>>(emptyList())
    val courses: StateFlow<List<CourseDto>> = _courses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showGroupMissingDialog = MutableStateFlow(false)
    val showGroupMissingDialog: StateFlow<Boolean> = _showGroupMissingDialog.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val profile = userProfileManager.getProfile()
        _profile.value = profile
        val role = profile?.role?.lowercase() ?: ""
        val groupId = profile?.groupId
        val lectorId = if (role == "lector") profile?.userId else null

        if (role == "student" && groupId == null) {
            _showGroupMissingDialog.value = true
        } else {
            _showGroupMissingDialog.value = false
            loadCourses(role, groupId, lectorId)
        }
    }

    private fun loadCourses(role: String, groupId: Int?, lectorId: Int?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = when (role) {
                    "student" -> courseRepository.getCoursesByGroup(groupId ?: throw IllegalArgumentException("groupId missing"))
                    "lector" -> courseRepository.getCoursesByLector(lectorId ?: throw IllegalArgumentException("lectorId missing"))
                    "admin" -> courseRepository.getAllCourses()
                    else -> emptyList()
                }
                _courses.value = result
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retry() {
        val profile = _profile.value
        val role = profile?.role?.lowercase() ?: ""
        val groupId = profile?.groupId
        val lectorId = if (role == "lector") profile?.userId else null
        loadCourses(role, groupId, lectorId)
    }

    fun dismissDialog() {
        _showGroupMissingDialog.value = false
    }
}