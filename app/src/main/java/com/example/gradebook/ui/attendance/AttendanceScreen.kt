package com.example.gradebook.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gradebook.data.local.entities.AttendanceStatus
import com.example.gradebook.data.local.entities.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    classroomId: Int,
    onNavigateBack: () -> Unit,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val attendanceMap by viewModel.attendanceMap.collectAsState()

    LaunchedEffect(classroomId) {
        viewModel.loadData(classroomId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Take Attendance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        viewModel.saveAttendance(classroomId)
                        onNavigateBack()
                    }) {
                        Text("SAVE", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is AttendanceUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is AttendanceUiState.Error -> Text("Error: ${state.message}", modifier = Modifier.align(Alignment.Center))
                is AttendanceUiState.Success -> {
                    LazyColumn(modifier = Modifier.padding(16.dp)) {
                        items(state.students) { student ->
                            AttendanceItem(
                                student = student,
                                status = attendanceMap[student.id] ?: AttendanceStatus.PRESENT,
                                onStatusChange = { newStatus ->
                                    viewModel.markStudent(student.id, newStatus)
                                }
                            )
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha=0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceItem(
    student: Student,
    status: AttendanceStatus,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = student.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AttendanceStatus.values().forEach { s ->
                val isSelected = status == s
                FilterChip(
                    selected = isSelected,
                    onClick = { onStatusChange(s) },
                    label = { Text(s.name.take(3)) }, // PRE, ABS, LAT...
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when(s) {
                            AttendanceStatus.PRESENT -> Color(0xFFE8F5E9) // Light Green
                            AttendanceStatus.ABSENT -> Color(0xFFFFEBEE) // Light Red
                            else -> MaterialTheme.colorScheme.primaryContainer
                        },
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }
    }
}
