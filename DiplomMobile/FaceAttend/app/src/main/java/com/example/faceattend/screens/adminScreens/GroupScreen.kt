package com.example.faceattend.screens.adminScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.faceattend.api.response.GroupDto
import com.example.faceattend.utils.DateUtils.formatDate
import com.example.faceattend.views.admin.AdminGroupsViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    onBack: () -> Unit
) {
    val viewModel: AdminGroupsViewModel = koinViewModel()
    val groups by viewModel.groups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val filterNumber by viewModel.filterNumber.collectAsState()
    val filterYear by viewModel.filterYear.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var tempFilterNumber by remember { mutableStateOf("") }
    var tempFilterYear by remember { mutableStateOf("") }
    var newGroupNumber by remember { mutableStateOf("") }
    var newGroupYear by remember { mutableStateOf("") }
    var newGroupCreateDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var createError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadGroups()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Группы") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.List, contentDescription = "Фильтр")
                    }
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить", tint = Color.Green)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ошибка: $error", color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadGroups() }) {
                            Text("Повторить")
                        }
                    }
                }
                groups.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Нет групп")
                        if (filterNumber != null || filterYear != null) {
                            Text("Фильтр: номер=${filterNumber ?: "любой"}, год=${filterYear ?: "любой"}")
                            Button(onClick = { viewModel.setFilter(null, null) }) {
                                Text("Сбросить фильтр")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(groups) { group ->
                            GroupItem(
                                group = group,
                                onDelete = {
                                    viewModel.deleteGroup(group.id) { }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Диалог добавления группы
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                createError = null
            },
            title = { Text("Добавить группу") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newGroupNumber,
                        onValueChange = { newGroupNumber = it },
                        label = { Text("Номер группы") },
                        singleLine = true,
                        isError = createError != null
                    )
                    OutlinedTextField(
                        value = newGroupYear,
                        onValueChange = { newGroupYear = it },
                        label = { Text("Год поступления") },
                        singleLine = true,
                        isError = createError != null
                    )
                    OutlinedTextField(
                        value = newGroupCreateDate,
                        onValueChange = { newGroupCreateDate = it },
                        label = { Text("Дата создания (ГГГГ-ММ-ДД)") },
                        singleLine = true,
                        isError = createError != null
                    )
                    if (createError != null) {
                        Text(
                            text = createError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val number = newGroupNumber.toIntOrNull()
                        val year = newGroupYear.toIntOrNull()
                        if (number != null && year != null && newGroupCreateDate.isNotBlank()) {
                            createError = null
                            viewModel.createGroup(
                                number = number,
                                year = year,
                                createDate = "${newGroupCreateDate}T00:00:00Z",
                                onSuccess = {
                                    showCreateDialog = false
                                    newGroupNumber = ""
                                    newGroupYear = ""
                                    newGroupCreateDate = LocalDate.now().toString()
                                    createError = null
                                },
                                onError = { e ->
                                    createError = e
                                }
                            )
                        } else {
                            createError = "Заполните все поля корректно"
                        }
                    }
                ) { Text("Добавить") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    createError = null
                }) { Text("Отмена") }
            }
        )
    }

    // Диалог фильтра
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Фильтр групп") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempFilterNumber,
                        onValueChange = { tempFilterNumber = it },
                        label = { Text("Номер группы") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempFilterYear,
                        onValueChange = { tempFilterYear = it },
                        label = { Text("Год") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val number = tempFilterNumber.toIntOrNull()
                        val year = tempFilterYear.toIntOrNull()
                        viewModel.setFilter(number, year)
                        showFilterDialog = false
                    }
                ) { Text("Применить") }
            },
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun GroupItem(
    group: GroupDto,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* детали группы, если нужно */ },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Группа №${group.number} (${group.year} год)",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Дата создания: ${formatDate(group.createDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Red)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление группы") },
            text = { Text("Вы уверены, что хотите удалить группу №${group.number} (${group.year} год)?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }
}