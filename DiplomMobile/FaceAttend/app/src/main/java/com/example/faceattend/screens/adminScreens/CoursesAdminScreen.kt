package com.example.faceattend.screens.adminScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.faceattend.api.requests.CourseCreateRequest
import com.example.faceattend.api.response.CourseDto
import com.example.faceattend.api.response.GroupDto
import com.example.faceattend.api.response.LectorProfile
import com.example.faceattend.api.response.SubjectDto
import com.example.faceattend.screens.adminScreens.selectors.GroupSelectorDialog
import com.example.faceattend.screens.adminScreens.selectors.LectorSelectorDialog
import com.example.faceattend.screens.adminScreens.selectors.SubjectSelectorDialog
import com.example.faceattend.utils.DateUtils.formatDate
import com.example.faceattend.views.admin.AdminCoursesViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesAdminScreen(
    onBack: () -> Unit
) {
    val viewModel: AdminCoursesViewModel = koinViewModel()
    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val filterSubject by viewModel.filterSubject.collectAsState()
    val filterLector by viewModel.filterLector.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showGroupsDialog by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf<CourseDto?>(null) }
    var courseGroups by remember { mutableStateOf<List<GroupDto>>(emptyList()) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    fun refreshGroups() {
        selectedCourse?.let { course ->
            viewModel.getCourseGroups(course.id,
                onResult = { groups -> courseGroups = groups },
                onError = { errorMsg -> snackbarMessage = errorMsg }
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCourses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Курсы") },
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
                        Icon(Icons.Default.Add, contentDescription = "Добавить",tint = Color.Green)
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = remember { SnackbarHostState() }) {
                snackbarMessage?.let { message ->
                    Snackbar(
                        action = {
                            TextButton(onClick = { snackbarMessage = null }) {
                                Text("OK")
                            }
                        }
                    ) {
                        Text(message)
                    }
                }
            }
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
                        Button(onClick = { viewModel.loadCourses() }) {
                            Text("Повторить")
                        }
                    }
                }
                courses.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Нет курсов")
                        if (filterSubject != null || filterLector != null) {
                            Text("Фильтр активен")
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
                        items(courses) { course ->
                            CourseCard(
                                course = course,
                                onManageGroups = {
                                    selectedCourse = course
                                    refreshGroups()
                                    showGroupsDialog = true
                                },
                                onDeleteCourse = {
                                    viewModel.deleteCourse(course.id,
                                        onSuccess = { snackbarMessage = "Курс удалён" },
                                        onError = { errorMsg -> snackbarMessage = errorMsg }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var selectedSubject by remember { mutableStateOf<SubjectDto?>(null) }
        var selectedLector by remember { mutableStateOf<LectorProfile?>(null) }
        var startDate by remember { mutableStateOf(LocalDate.now().toString()) }
        var endDate by remember { mutableStateOf(LocalDate.now().plusMonths(1).toString()) }
        var createError by remember { mutableStateOf<String?>(null) }
        var showSubjectSelector by remember { mutableStateOf(false) }
        var showLectorSelector by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Создать курс") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showSubjectSelector = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedSubject?.name ?: "Выбрать предмет")
                    }
                    OutlinedButton(
                        onClick = { showLectorSelector = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedLector?.let { "${it.surname} ${it.name} ${it.patronymic}" } ?: "Выбрать преподавателя")
                    }
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Дата начала (ГГГГ-ММ-ДД)") },
                        singleLine = true,
                        isError = createError != null
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("Дата окончания (ГГГГ-ММ-ДД)") },
                        singleLine = true,
                        isError = createError != null
                    )
                    if (createError != null) {
                        Text(createError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedSubject != null && selectedLector != null && startDate.isNotBlank() && endDate.isNotBlank()) {
                            val request = CourseCreateRequest(
                                subjectId = selectedSubject!!.id,
                                lectorId = selectedLector!!.userId,
                                startDate = "${startDate}T00:00:00Z",
                                endDate = "${endDate}T00:00:00Z"
                            )
                            viewModel.createCourse(
                                request = request,
                                onSuccess = { showCreateDialog = false },
                                onError = { createError = it }
                            )
                        } else {
                            createError = "Заполните все поля"
                        }
                    }
                ) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Отмена") }
            }
        )

        if (showSubjectSelector) {
            SubjectSelectorDialog(
                onSelect = { subject ->
                    selectedSubject = subject
                    showSubjectSelector = false
                },
                onDismiss = { showSubjectSelector = false }
            )
        }

        if (showLectorSelector) {
            LectorSelectorDialog(
                onSelect = { lector ->
                    selectedLector = lector
                    showLectorSelector = false
                },
                onDismiss = { showLectorSelector = false }
            )
        }
    }

    if (showFilterDialog) {
        var tempSubject by remember { mutableStateOf<SubjectDto?>(null) }
        var tempLector by remember { mutableStateOf<LectorProfile?>(null) }
        var showSubjectSelector by remember { mutableStateOf(false) }
        var showLectorSelector by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Фильтр курсов") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showSubjectSelector = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(tempSubject?.name ?: "Все предметы")
                    }
                    OutlinedButton(
                        onClick = { showLectorSelector = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(tempLector?.let { "${it.surname} ${it.name} ${it.patronymic}" } ?: "Все преподаватели")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setFilter(tempSubject?.id, tempLector?.userId)
                        showFilterDialog = false
                    }
                ) { Text("Применить") }
            },
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) { Text("Отмена") }
            }
        )

        if (showSubjectSelector) {
            SubjectSelectorDialog(
                onSelect = { subject ->
                    tempSubject = subject
                    showSubjectSelector = false
                },
                onDismiss = { showSubjectSelector = false }
            )
        }

        if (showLectorSelector) {
            LectorSelectorDialog(
                onSelect = { lector ->
                    tempLector = lector
                    showLectorSelector = false
                },
                onDismiss = { showLectorSelector = false }
            )
        }
    }

    if (showGroupsDialog && selectedCourse != null) {
        ManageCourseGroupsDialog(
            course = selectedCourse!!,
            groups = courseGroups,
            onAddGroup = { groupId ->
                viewModel.addGroupToCourse(selectedCourse!!.id, groupId,
                    onSuccess = { refreshGroups() },
                    onError = { errorMsg -> snackbarMessage = errorMsg }
                )
            },
            onRemoveGroup = { groupId ->
                viewModel.removeGroupFromCourse(selectedCourse!!.id, groupId,
                    onSuccess = { refreshGroups() },
                    onError = { errorMsg -> snackbarMessage = errorMsg }
                )
            },
            onDismiss = { showGroupsDialog = false }
        )
    }
}

@Composable
fun CourseCard(
    course: CourseDto,
    onManageGroups: () -> Unit,
    onDeleteCourse: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.subjectName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Преподаватель: ${course.lectorFullName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Даты: ${formatDate(course.startDate)} - ${formatDate(course.endDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = onManageGroups) {
                        Icon(Icons.Default.Menu, contentDescription = "Управление группами")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить курс", tint = Color.Red)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление курса") },
            text = { Text("Вы уверены, что хотите удалить курс «${course.subjectName}»?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteCourse()
                    }
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun ManageCourseGroupsDialog(
    course: CourseDto,
    groups: List<GroupDto>,
    onAddGroup: (Int) -> Unit,
    onRemoveGroup: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var showGroupSelector by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Группы курса: ${course.subjectName}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showGroupSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Добавить группу")
                }
                if (groups.isEmpty()) {
                    Text("Нет групп")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(groups) { group ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Группа №${group.number} (${group.year} год)")
                                IconButton(onClick = { onRemoveGroup(group.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Удалить",tint = Color.Red)
                                }
                            }
                            Divider()
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

    if (showGroupSelector) {
        GroupSelectorDialog(
            lastSelectedGroup = null,
            onSelect = { group ->
                onAddGroup(group.id)
                showGroupSelector = false
            },
            onDismiss = { showGroupSelector = false }
        )
    }
}
