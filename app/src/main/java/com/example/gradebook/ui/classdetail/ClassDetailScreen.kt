package com.example.gradebook.ui.classdetail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gradebook.data.local.entities.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    classroomId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToGrades: () -> Unit,
    viewModel: ClassDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAddStudentDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importStudents(context, it, classroomId) }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportReport(context, it, classroomId) }
    }

    val exportPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportReportPdf(context, it, classroomId) }
    }

    LaunchedEffect(classroomId) {
        viewModel.loadClassDetails(classroomId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState is ClassDetailUiState.Success) {
                        Text((uiState as ClassDetailUiState.Success).classroom.name)
                    } else {
                        Text("Class Details")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAttendance) {
                        Icon(Icons.Default.EventAvailable, contentDescription = "Attendance")
                    }
                    IconButton(onClick = onNavigateToGrades) {
                        Icon(Icons.Default.Assignment, contentDescription = "Grades")
                    }
                    IconButton(onClick = { filePickerLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel")) }) {
                        Icon(Icons.Default.UploadFile, contentDescription = "Import Excel")
                    }
                    IconButton(onClick = { exportLauncher.launch("GradeBook_Report.xlsx") }) {
                        Icon(Icons.Default.TableChart, contentDescription = "Export Excel")
                    }
                    IconButton(onClick = { exportPdfLauncher.launch("GradeBook_Report.pdf") }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddStudentDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ClassDetailUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ClassDetailUiState.Error -> Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is ClassDetailUiState.Success -> {
                    if (state.students.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No students yet.")
                            Button(onClick = { filePickerLauncher.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel")) }) {
                                Text("Import from Excel")
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            items(state.students) { student ->
                                StudentItem(student)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddStudentDialog) {
        AddStudentDialog(
            onDismiss = { showAddStudentDialog = false },
            onConfirm = { name, seat ->
                viewModel.addStudent(classroomId, name, seat)
                showAddStudentDialog = false
            }
        )
    }
}

@Composable
fun StudentItem(student: Student) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = student.name, style = MaterialTheme.typography.bodyLarge)
                if (!student.seatNumber.isNullOrBlank()) {
                    Text(text = "Seat: ${student.seatNumber}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun AddStudentDialog(onDismiss: () -> Unit, onConfirm: (String, String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var seat by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Student") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = seat,
                    onValueChange = { seat = it },
                    label = { Text("Seat Number (Optional)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, seat.ifBlank { null }) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
