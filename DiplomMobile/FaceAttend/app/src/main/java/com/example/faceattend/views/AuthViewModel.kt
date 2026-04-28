package com.example.faceattend.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.faceattend.api.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.onSuccess

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode

    // Общие поля
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    // Поля регистрации
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _surname = MutableStateFlow("")
    val surname: StateFlow<String> = _surname

    private val _patronymic = MutableStateFlow("")
    val patronymic: StateFlow<String> = _patronymic

    private val _role = MutableStateFlow("student")
    val role: StateFlow<String> = _role

    // Поля для студента
    private val _studentIdNumber = MutableStateFlow("")
    val studentIdNumber: StateFlow<String> = _studentIdNumber

    // Поля для преподавателя
    private val _department = MutableStateFlow("")
    val department: StateFlow<String> = _department

    // Обновления
    fun updateEmail(value: String) { _email.value = value }
    fun updatePassword(value: String) { _password.value = value }
    fun updateName(value: String) { _name.value = value }
    fun updateSurname(value: String) { _surname.value = value }
    fun updatePatronymic(value: String) { _patronymic.value = value }
    fun updateRole(value: String) { _role.value = value }
    fun updateStudentIdNumber(value: String) { _studentIdNumber.value = value }
    fun updateDepartment(value: String) { _department.value = value }
    fun toggleMode() { _isLoginMode.value = !_isLoginMode.value }

    fun login(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.login(_email.value, _password.value)
            result.onSuccess {
                onSuccess()
            }.onFailure {
                _error.value = it.message ?: "Login failed"
            }
            _isLoading.value = false
        }
    }

    fun register(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = if (_role.value == "student") {
                authRepository.registerStudent(
                    name = _name.value,
                    surname = _surname.value,
                    patronymic = _patronymic.value,
                    email = _email.value,
                    password = _password.value,
                    studentIdNumber = _studentIdNumber.value.takeIf { it.isNotBlank() }
                )
            } else {
                authRepository.registerTeacher(
                    name = _name.value,
                    surname = _surname.value,
                    patronymic = _patronymic.value,
                    email = _email.value,
                    password = _password.value,
                    department = _department.value.takeIf { it.isNotBlank() }
                )
            }

            result.onSuccess {
                onSuccess()
            }.onFailure {
                _error.value = it.message ?: "Registration failed"
            }

            _isLoading.value = false
        }
    }
}