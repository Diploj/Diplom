package com.example.faceattend.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceattend.api.repository.CourseRepository
import com.example.faceattend.api.repository.LectureRepository
import com.example.faceattend.api.response.CourseDto
import com.example.faceattend.api.response.LectureDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseDetailsViewModel(
    private val courseRepository: CourseRepository,
    private val lectureRepository: LectureRepository
) : ViewModel() {

    private val _course = MutableStateFlow<CourseDto?>(null)
    val course: StateFlow<CourseDto?> = _course.asStateFlow()

    private val _lectures = MutableStateFlow<List<LectureDto>>(emptyList())
    val lectures: StateFlow<List<LectureDto>> = _lectures.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isActual = MutableStateFlow(false)
    val isActual: StateFlow<Boolean> = _isActual.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()

    fun setFilter(isActual: Boolean) {
        _isActual.value = isActual
        loadLectures()
    }

    fun loadCourse(courseId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val course = courseRepository.getCourseById(courseId)
                _course.value = course
                loadLectures()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadLectures() {
        val course = _course.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val lectures = lectureRepository.getLecturesByCourse(course.id, _isActual.value)
                _lectures.value = lectures
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun showCreateLectureDialog() {
        _showDatePicker.value = true
    }

    fun dismissCreateLectureDialog() {
        _showDatePicker.value = false
    }

    fun createLecture(date: String) {
        val course = _course.value ?: return
        viewModelScope.launch {
            _isCreating.value = true
            _error.value = null
            try {
                lectureRepository.createLecture(course.id, date)
                loadLectures()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isCreating.value = false
                _showDatePicker.value = false
            }
        }
    }

    fun deleteLecture(lectureId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                lectureRepository.deleteLecture(lectureId)
                loadLectures()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}