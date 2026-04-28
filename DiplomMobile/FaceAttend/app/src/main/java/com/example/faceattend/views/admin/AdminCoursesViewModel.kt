package com.example.faceattend.views.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceattend.api.repository.CourseRepository
import com.example.faceattend.api.repository.LectorRepository
import com.example.faceattend.api.repository.SubjectRepository
import com.example.faceattend.api.requests.CourseCreateRequest
import com.example.faceattend.api.requests.CourseFilter
import com.example.faceattend.api.requests.LectorFilter
import com.example.faceattend.api.response.CourseDto
import com.example.faceattend.api.response.GroupDto
import com.example.faceattend.api.response.LectorProfile
import com.example.faceattend.api.response.SubjectDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminCoursesViewModel(
    private val courseRepository: CourseRepository,
) : ViewModel() {

    private val _courses = MutableStateFlow<List<CourseDto>>(emptyList())
    val courses: StateFlow<List<CourseDto>> = _courses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _filterSubject = MutableStateFlow<Int?>(null)
    val filterSubject: StateFlow<Int?> = _filterSubject.asStateFlow()

    private val _filterLector = MutableStateFlow<Int?>(null)
    val filterLector: StateFlow<Int?> = _filterLector.asStateFlow()

    init {
        loadCourses()
    }

    fun loadCourses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val filter = CourseFilter(_filterSubject.value, _filterLector.value)
                val list = courseRepository.getCourses(filter)
                _courses.value = list
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setFilter(subjectId: Int?, lectorId: Int?) {
        _filterSubject.value = subjectId
        _filterLector.value = lectorId
        loadCourses()
    }

    fun createCourse(request: CourseCreateRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                courseRepository.createCourse(request)
                loadCourses()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCourse(id:Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                courseRepository.deleteCourse(id)
                loadCourses()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCourseGroups(courseId: Int, onResult: (List<GroupDto>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val groups = courseRepository.getCourseGroups(courseId)
                onResult(groups)
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка")
            }
        }
    }

    fun removeGroupFromCourse(courseId: Int, groupId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                courseRepository.removeGroup(courseId, groupId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка")
            }
        }
    }

    fun addGroupToCourse(courseId: Int, groupId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                courseRepository.addGroup(courseId, groupId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка")
            }
        }
    }
}