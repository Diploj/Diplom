package com.example.faceattend.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionExpiredManager {
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    suspend fun trigger() {
        _showDialog.emit(true)
    }

    fun reset() {
        _showDialog.value = false
    }
}