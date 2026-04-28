package com.example.faceattend.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.faceattend.api.response.UserProfile
import com.example.faceattend.views.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AdminHomeScreen(
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onAdminGroups: () -> Unit,
    onAdminCourses: () -> Unit,
    onAdminStudents: () -> Unit,
    onAdminSubjects: () -> Unit
) {
    val viewModel: HomeViewModel = koinViewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    val drawerItems = listOf(
        DrawerItem("Управление группами", onAdminGroups),
        DrawerItem("Управление курсами", onAdminCourses),
        DrawerItem("Управление студентами", onAdminStudents),
        DrawerItem("Управление предметами", onAdminSubjects)
    )

    HomeScaffold(
        drawerItems = drawerItems,
        onEditProfile = onEditProfile,
        onLogout = onLogout
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .padding(16.dp),
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                    Text("Загрузка")
                }
                error != null -> ErrorContent(error = error!!, onRetry = { viewModel.loadProfile() })
                userProfile != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminProfileCard(profile = userProfile!!)
                        AdminButtons(
                            onGroups = onAdminGroups,
                            onCourses = onAdminCourses,
                            onStudents = onAdminStudents,
                            onSubjects = onAdminSubjects
                        )
                    }
                }
                else -> Text("Нет данных")
            }
        }
    }
}

@Composable
private fun AdminProfileCard(profile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Администратор",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Добро пожаловать",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "${profile.name} ${profile.patronymic}!",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Email: ${profile.email}")
        }
    }
}

@Composable
private fun AdminButtons(
    onGroups: () -> Unit,
    onCourses: () -> Unit,
    onStudents: () -> Unit,
    onSubjects: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onGroups, modifier = Modifier.fillMaxWidth()) {
            Text("Управление группами")
        }
        Button(onClick = onCourses, modifier = Modifier.fillMaxWidth()) {
            Text("Управление курсами")
        }
        Button(onClick = onStudents, modifier = Modifier.fillMaxWidth()) {
            Text("Управление студентами")
        }
        Button(onClick = onSubjects, modifier = Modifier.fillMaxWidth()) {
            Text("Управление предметами")
        }
    }
}