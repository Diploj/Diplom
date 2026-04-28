package com.example.faceattend.screens.adminScreens.selectors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.faceattend.api.requests.LectorFilter
import com.example.faceattend.api.response.LectorProfile
import com.example.faceattend.views.admin.selectors.LectorSelectorViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun LectorSelectorDialog(
    onSelect: (LectorProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel: LectorSelectorViewModel = koinViewModel()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var patronymic by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }

    fun performSearch() {
        val filter = LectorFilter(
            name = name.takeIf { it.isNotBlank() },
            surname = surname.takeIf { it.isNotBlank() },
            patronymic = patronymic.takeIf { it.isNotBlank() },
            email = email.takeIf { it.isNotBlank() },
            department = department.takeIf { it.isNotBlank() }
        )
        viewModel.search(filter)
    }

    fun clear() {
        name = ""
        surname = ""
        patronymic = ""
        email = ""
        department = ""
        viewModel.clear()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите преподавателя") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Фамилия") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = patronymic,
                    onValueChange = { patronymic = it },
                    label = { Text("Отчество") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Кафедра") },
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { performSearch() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Поиск") }
                    OutlinedButton(
                        onClick = { clear() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Очистить") }
                }
                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null -> {
                        Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
                    }
                    searchResults.isEmpty() -> {
                        Text("Введите данные для поиска или ничего не найдено")
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(searchResults) { lector ->
                                TextButton(
                                    onClick = { onSelect(lector) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${lector.surname} ${lector.name} ${lector.patronymic}",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Divider()
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        },
        dismissButton = null
    )
}