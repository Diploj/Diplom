package com.example.faceattend.views.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceattend.api.repository.GroupRepository
import com.example.faceattend.api.response.GroupDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminGroupsViewModel(
    private val groupRepository: GroupRepository
) : ViewModel() {
    private val _groups = MutableStateFlow<List<GroupDto>>(emptyList())
    val groups: StateFlow<List<GroupDto>> = _groups.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _filterNumber = MutableStateFlow<Int?>(null)
    val filterNumber: StateFlow<Int?> = _filterNumber.asStateFlow()

    private val _filterYear = MutableStateFlow<Int?>(null)
    val filterYear: StateFlow<Int?> = _filterYear.asStateFlow()

    fun setFilter(number: Int?, year: Int?) {
        _filterNumber.value = number
        _filterYear.value = year
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val list = groupRepository.getGroups(_filterNumber.value, _filterYear.value)
                _groups.value = list
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createGroup(number: Int, year: Int, createDate: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                groupRepository.createGroup(number, year, createDate)
                loadGroups()
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Ошибка создания группы"
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteGroup(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                groupRepository.deleteGroup(id)
                loadGroups()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}