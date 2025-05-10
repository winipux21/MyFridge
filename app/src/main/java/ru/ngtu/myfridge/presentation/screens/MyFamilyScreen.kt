package ru.ngtu.myfridge.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import ru.ngtu.myfridge.data.db.FamilyEntity
import ru.ngtu.myfridge.presentation.ProductViewModel

data class Family(val id: String, val name: String, val users: Map<String, Boolean>, val creator: String? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFamilyScreen(
    viewModel: ProductViewModel,
    onBack: () -> Unit,
    onFamilySelected: (Family) -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
    val context = LocalContext.current
    val families by viewModel.allFamilies.collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var isJoining by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = true) {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Моя семья") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (families.isEmpty()) {
                Text("Вы не состоите ни в одной семье")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(families.size) { index ->
                        val familyEntity = families[index]
                        val family = Family(familyEntity.id, familyEntity.name, mapOf(userId to true), familyEntity.creatorId)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { onFamilySelected(family) }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(family.name)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { showCreateDialog = true }) {
                    Text("Создать семью")
                }
                Button(onClick = { showJoinDialog = true }) {
                    Text("Присоединиться")
                }
            }

            if (showCreateDialog) {
                var familyName by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showCreateDialog = false },
                    title = { Text("Создать семью") },
                    text = {
                        OutlinedTextField(
                            value = familyName,
                            onValueChange = { familyName = it },
                            label = { Text("Название семьи") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (familyName.isNotBlank()) {
                                coroutineScope.launch {
                                    val familyId = viewModel.createFamily(familyName, userId)
                                    Toast.makeText(context, "Семья создана! ID: $familyId", Toast.LENGTH_LONG).show()
                                    showCreateDialog = false
                                }
                            } else {
                                Toast.makeText(context, "Введите название семьи", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Создать")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreateDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }

            if (showJoinDialog) {
                var familyIdInput by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { if (!isJoining) showJoinDialog = false },
                    title = { Text("Присоединиться к семье") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = familyIdInput,
                                onValueChange = { familyIdInput = it },
                                label = { Text("ID семьи") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isJoining
                            )
                            if (isJoining) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (familyIdInput.isNotBlank() && !isJoining) {
                                    isJoining = true
                                    coroutineScope.launch {
                                        val success = viewModel.joinFamily(familyIdInput)
                                        isJoining = false
                                        if (success) {
                                            Toast.makeText(context, "Вы присоединились к семье!", Toast.LENGTH_SHORT).show()
                                            showJoinDialog = false
                                        } else {
                                            Toast.makeText(context, "Семья не найдена", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            enabled = !isJoining
                        ) {
                            Text("Присоединиться")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { if (!isJoining) showJoinDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}