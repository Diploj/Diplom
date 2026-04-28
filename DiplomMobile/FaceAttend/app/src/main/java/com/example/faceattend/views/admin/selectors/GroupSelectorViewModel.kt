package com.example.faceattend.views.admin.selectors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceattend.api.repository.GroupRepository
import com.example.faceattend.api.response.GroupDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroupSelectorViewModel(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _filterNumber = MutableStateFlow("")
    val filterNumber: StateFlow<String> = _filterNumber.asStateFlow()

    private val _filterYear = MutableStateFlow("")
    val filterYear: StateFlow<String> = _filterYear.asStateFlow()

    private val _groups = MutableStateFlow<List<GroupDto>>(emptyList())
    val groups: StateFlow<List<GroupDto>> = _groups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _showResults = MutableStateFlow(false)
    val showResults: StateFlow<Boolean> = _showResults.asStateFlow()

    private var allGroups: List<GroupDto> = emptyList()

    fun updateFilterNumber(value: String) { _filterNumber.value = value }
    fun updateFilterYear(value: String) { _filterYear.value = value }

    fun search() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (allGroups.isEmpty()) {
                    allGroups = groupRepository.getGroups()
                }
                val filtered = allGroups.filter { group ->
                    (_filterNumber.value.isEmpty() || group.number.toString().contains(_filterNumber.value)) &&
                            (_filterYear.value.isEmpty() || group.year.toString().contains(_filterYear.value))
                }
                _groups.value = filtered
                _showResults.value = true
            } catch (e: Exception) {
                _error.value = e.message
                _groups.value = emptyList()
                _showResults.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _filterNumber.value = ""
        _filterYear.value = ""
        _groups.value = emptyList()
        _showResults.value = false
        _error.value = null
    }

    fun reset() {
        clearResults()
        allGroups = emptyList()
    }
}