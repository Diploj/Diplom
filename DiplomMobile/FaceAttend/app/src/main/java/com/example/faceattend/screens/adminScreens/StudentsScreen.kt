package com.example.faceattend.screens.adminScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.faceattend.api.requests.StudentFilter
import com.example.faceattend.api.response.GroupDto
import com.example.faceattend.api.response.StudentProfile
import com.example.faceattend.screens.adminScreens.selectors.GroupSelectorDialog
import com.example.faceattend.views.admin.AdminStudentsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    onBack: () -> Unit
) {
    val viewModel: AdminStudentsViewModel = koinViewModel()
    val students by viewModel.students.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val filter by viewModel.filter.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<StudentProfile?>(null) }
    var lastSelectedGroup by remember { mutableStateOf<GroupDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadStudents()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Студенты") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.List, contentDescription = "Фильтр")
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
                        Button(onClick = { viewModel.loadStudents() }) {
                            Text("Повторить")
                        }
                    }
                }

                students.isEmpty() -> {
                    Text("Нет студентов", modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(students) { student ->
                            StudentItem(
                                student = student,
                                onEditGroup = {
                                    selectedStudent = student
                                    showGroupDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterStudentsDialog(
            currentFilter = filter,
            onApply = { filter ->
                viewModel.setFilter(filter)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    if (showGroupDialog && selectedStudent != null) {
        GroupSelectorDialog(
            lastSelectedGroup = lastSelectedGroup,
            onSelect = { group ->
                viewModel.setStudentGroup(
                    studentId = selectedStudent!!.userId,
                    groupId = group.id,
                    onSuccess = {
                        lastSelectedGroup = group
                        showGroupDialog = false
                        selectedStudent = null
                    },
                    onError = {}
                )
            },
            onDismiss = { showGroupDialog = false }
        )
    }
}

@Composable
fun StudentItem(
    student: StudentProfile,
    onEditGroup: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${student.surname} ${student.name} ${student.patronymic}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onEditGroup,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Edit,
                        contentDescription = "Изменить группу",
                        tint = Color.Blue,
                        modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))


            Text(
                text = student.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )


            if (!student.studentIdNumber.isNullOrBlank()) {
                Text(
                    text = "Студенческий: ${student.studentIdNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = if (student.groupId != null) "Группа ID: ${student.groupId}" else "Группа не назначена",
                style = MaterialTheme.typography.bodySmall,
                color = if (student.groupId != null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun FilterStudentsDialog(
    currentFilter: StudentFilter,
    onApply: (StudentFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentFilter.name ?: "") }
    var surname by remember { mutableStateOf(currentFilter.surname ?: "") }
    var patronymic by remember { mutableStateOf(currentFilter.patronymic ?: "") }
    var email by remember { mutableStateOf(currentFilter.email ?: "") }
    var studentIdNumber by remember { mutableStateOf(currentFilter.studentIdNumber ?: "") }
    var selectedGroup by remember { mutableStateOf<GroupDto?>(null) }
    var showGroupSelector by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        currentFilter.groupId?.let { groupId ->

        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Фильтр студентов") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    value = studentIdNumber,
                    onValueChange = { studentIdNumber = it },
                    label = { Text("Номер студенческого") },
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedGroup?.let { "Группа №${it.number} (${it.year})" } ?: "Группа не выбрана",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { showGroupSelector = true }) {
                        Text(if (selectedGroup != null) "Изменить" else "Выбрать")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val filter = StudentFilter(
                        name = name.takeIf { it.isNotBlank() },
                        surname = surname.takeIf { it.isNotBlank() },
                        patronymic = patronymic.takeIf { it.isNotBlank() },
                        email = email.takeIf { it.isNotBlank() },
                        studentIdNumber = studentIdNumber.takeIf { it.isNotBlank() },
                        groupId = selectedGroup?.id
                    )
                    onApply(filter)
                }
            ) { Text("Применить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )

    if (showGroupSelector) {
        GroupSelectorDialog(
            lastSelectedGroup = selectedGroup,
            onSelect = { group ->
                selectedGroup = group
                showGroupSelector = false
            },
            onDismiss = { showGroupSelector = false }
        )
    }
}