package com.example.faceattend.screens.adminScreens.selectors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import com.example.faceattend.api.response.SubjectDto
import com.example.faceattend.views.admin.selectors.SubjectSelectorViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SubjectSelectorDialog(
    onSelect: (SubjectDto) -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel: SubjectSelectorViewModel = koinViewModel()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    fun performSearch() {
        viewModel.search(searchQuery)
    }

    fun clear() {
        searchQuery = ""
        viewModel.clear()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите предмет") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Название предмета") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { performSearch() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Поиск")
                    }
                    OutlinedButton(
                        onClick = { clear() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Очистить")
                    }
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
                    searchQuery.isNotBlank() && searchResults.isEmpty() -> {
                        Text("Ничего не найдено")
                    }
                    searchResults.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(searchResults) { subject ->
                                TextButton(
                                    onClick = { onSelect(subject) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(subject.name, modifier = Modifier.fillMaxWidth())
                                }
                                Divider()
                            }
                        }
                    }
                    else -> {
                        Text("Введите название и нажмите «Поиск»")
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