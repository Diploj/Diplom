package com.example.faceattend.views


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceattend.api.repository.StudentRepository
import com.example.faceattend.api.repository.UserRepository
import com.example.faceattend.api.response.FaceImageDto
import com.example.faceattend.api.response.UserProfile
import com.example.faceattend.data.TokenManager
import com.example.faceattend.data.UserProfileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val tokenManager: TokenManager,
    private val userProfileManager: UserProfileManager
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _photos = MutableStateFlow<List<FaceImageDto>>(emptyList())
    val photos: StateFlow<List<FaceImageDto>> = _photos

    private val _isPhotosLoading = MutableStateFlow(false)
    val isPhotosLoading: StateFlow<Boolean> = _isPhotosLoading

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    init {
        loadCachedProfile()
    }

    private fun loadCachedProfile() {
        viewModelScope.launch {
            val cached = userProfileManager.getProfile()
            if (cached != null) {
                _userProfile.value = cached
            }
            loadProfile()
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = userRepository.getUserProfile()
            result.onSuccess { profile ->
                _userProfile.value = profile
                userProfileManager.clear()
                userProfileManager.saveProfile(profile)
            }.onFailure { exception ->
                _error.value = exception.message ?: "Неизвестная ошибка"
            }
            _isLoading.value = false
        }
    }

    fun loadPhotos() {
        viewModelScope.launch {
            _isPhotosLoading.value = true
            try {
                val photoList = studentRepository.getStudentPhotoUrls()
                _photos.value = photoList.getOrDefault(emptyList())
            } catch (e: Exception) {
                _error.value = "Не удалось загрузить фото: ${e.message}"
            } finally {
                _isPhotosLoading.value = false
            }
        }
    }

    fun deletePhoto(photoId: Int) {
        viewModelScope.launch {
            _isDeleting.value = true
            studentRepository.deletePhoto(photoId).onSuccess {
                _photos.value = _photos.value.filter { it.id != photoId }
            }.onFailure { error ->
                _error.value = "Ошибка удаления: ${error.message}"
            }
            _isDeleting.value = false
        }
    }

    fun uploadPhoto(photoFile: File) {
        viewModelScope.launch {
            _isUploading.value = true
            _error.value = null
            val result = studentRepository.uploadPhoto(photoFile)
            result.onSuccess {
                loadPhotos()
            }.onFailure { error ->
                _error.value = "Ошибка загрузки фото: ${error.message}"
            }
            _isUploading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clear()
            userProfileManager.clear()
        }
    }
}