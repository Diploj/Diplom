package com.example.faceattend.screens.home


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

data class DrawerItem(
    val text: String,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScaffold(
    drawerItems: List<DrawerItem>,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val performLogout = {
        showLogoutDialog = false
        onLogout()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Меню",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    drawerItems.forEach { item ->
                        DrawerItemButton(text = item.text, isDestructive = item.isDestructive) {
                            scope.launch { drawerState.close() }
                            item.onClick()
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    DrawerItemButton(text = "Выйти", isDestructive = true) {
                        scope.launch { drawerState.close() }
                        showLogoutDialog = true
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Главная") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
                        }
                    },
                    actions = {
                        IconButton(onClick = onEditProfile) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Редактировать профиль",
                                tint = Color.Blue)
                        }
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Выйти",
                                tint = Color.Red)
                        }
                    }
                )
            }
        ) { paddingValues ->
            content(paddingValues)
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Подтверждение выхода") },
            text = { Text("Вы уверены, что хотите выйти?") },
            confirmButton = {
                TextButton(onClick = performLogout) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Нет") }
            }
        )
    }
}

@Composable
private fun DrawerItemButton(text: String, isDestructive: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(text = text, modifier = Modifier.fillMaxWidth())
    }
}