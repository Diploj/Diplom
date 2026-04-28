package com.example.faceattend.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import com.example.faceattend.views.AuthViewModel


@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoginMode by viewModel.isLoginMode.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val name by viewModel.name.collectAsState()
    val surname by viewModel.surname.collectAsState()
    val patronymic by viewModel.patronymic.collectAsState()
    val role by viewModel.role.collectAsState()
    val studentIdNumber by viewModel.studentIdNumber.collectAsState()
    val department by viewModel.department.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLoginMode) "Вход" else "Регистрация",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = viewModel::updateEmail,
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Пароль
        OutlinedTextField(
            value = password,
            onValueChange = viewModel::updatePassword,
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Поля регистрации
        if (!isLoginMode) {
            // Имя
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::updateName,
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Фамилия
            OutlinedTextField(
                value = surname,
                onValueChange = viewModel::updateSurname,
                label = { Text("Фамилия") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Отчество
            OutlinedTextField(
                value = patronymic,
                onValueChange = viewModel::updatePatronymic,
                label = { Text("Отчество") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Выбор роли
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Роль: ", modifier = Modifier.align(Alignment.CenterVertically))
                Spacer(modifier = Modifier.width(8.dp))
                SegmentedButton(
                    selected = role == "student",
                    onClick = { viewModel.updateRole("student") }
                ) { Text("Студент") }
                Spacer(modifier = Modifier.width(8.dp))
                SegmentedButton(
                    selected = role == "lector",
                    onClick = { viewModel.updateRole("lector") }
                ) { Text("Преподаватель") }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Поля в зависимости от роли
            when (role) {
                "student" -> {
                    OutlinedTextField(
                        value = studentIdNumber,
                        onValueChange = viewModel::updateStudentIdNumber,
                        label = { Text("Номер студенческого") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                "lector" -> {
                    OutlinedTextField(
                        value = department,
                        onValueChange = viewModel::updateDepartment,
                        label = { Text("Кафедра") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isLoginMode) {
                    viewModel.login(onLoginSuccess)
                } else {
                    viewModel.register(onRegisterSuccess)
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(if (isLoginMode) "Войти" else "Зарегистрироваться")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = viewModel::toggleMode,
            enabled = !isLoading
        ) {
            Text(if (isLoginMode) "Нет аккаунта? Зарегистрироваться" else "Уже есть аккаунт? Войти")
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
@Composable
fun SegmentedButton(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.small
    ) {
        content()
    }
}