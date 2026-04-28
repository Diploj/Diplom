package com.example.faceattend.views.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceattend.api.repository.SubjectRepository
import com.example.faceattend.api.response.SubjectDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminSubjectsViewModel(
    private val subjectRepository: SubjectRepository
) : ViewModel() {
    private val _subjects = MutableStateFlow<List<SubjectDto>>(emptyList())
    val subjects: StateFlow<List<SubjectDto>> = _subjects.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _filterName = MutableStateFlow<String?>(null)
    val filterName: StateFlow<String?> = _filterName.asStateFlow()

    fun setFilter(name: String?) {
        _filterName.value = name
        loadSubjects()
    }

    fun loadSubjects() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val list = subjectRepository.getSubjects(_filterName.value)
                _subjects.value = list
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSubject(name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                subjectRepository.createSubject(name)
                loadSubjects()
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Ошибка создания предмета"
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSubject(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                subjectRepository.deleteSubject(id)
                loadSubjects()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}