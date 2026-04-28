package com.example.faceattend.views.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceattend.api.repository.StudentRepository
import com.example.faceattend.api.requests.StudentFilter
import com.example.faceattend.api.response.StudentProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminStudentsViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {
    private val _students = MutableStateFlow<List<StudentProfile>>(emptyList())
    val students: StateFlow<List<StudentProfile>> = _students.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _filter = MutableStateFlow(StudentFilter())
    val filter: StateFlow<StudentFilter> = _filter.asStateFlow()

    fun setFilter(filter: StudentFilter) {
        _filter.value = filter
        loadStudents()
    }

    fun loadStudents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val list = studentRepository.getStudents(_filter.value)
                _students.value = list
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setStudentGroup(studentId: Int, groupId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                studentRepository.setStudentGroup(studentId, groupId)
                loadStudents()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Ошибка")
            } finally {
                _isLoading.value = false
            }
        }
    }

}