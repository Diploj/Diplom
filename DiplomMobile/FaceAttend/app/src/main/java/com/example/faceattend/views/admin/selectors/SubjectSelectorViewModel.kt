package com.example.faceattend.views.admin.selectors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceattend.api.repository.SubjectRepository
import com.example.faceattend.api.response.SubjectDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubjectSelectorViewModel(
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<SubjectDto>>(emptyList())
    val searchResults: StateFlow<List<SubjectDto>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun search(query: String) {
        val searchQuery = if (query.isBlank()) null else query
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val results = subjectRepository.getSubjects(searchQuery)
                _searchResults.value = results
            } catch (e: Exception) {
                _error.value = e.message
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clear() {
        _searchResults.value = emptyList()
        _error.value = null
    }
}