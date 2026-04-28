package com.example.faceattend.views.admin.selectors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceattend.api.repository.LectorRepository
import com.example.faceattend.api.requests.LectorFilter
import com.example.faceattend.api.response.LectorProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LectorSelectorViewModel(
    private val lectorRepository: LectorRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<LectorProfile>>(emptyList())
    val searchResults: StateFlow<List<LectorProfile>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun search(filter: LectorFilter) {
        if (filter.name.isNullOrBlank() && filter.surname.isNullOrBlank() && filter.patronymic.isNullOrBlank() &&
            filter.email.isNullOrBlank() && filter.department.isNullOrBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val results = lectorRepository.searchLectors(filter)
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