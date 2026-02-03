package com.example.gradebook.ui.grades

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gradebook.data.local.entities.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeEntryScreen(
    classroomId: Int,
    onNavigateBack: () -> Unit,
    viewModel: GradeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scoreMap by viewModel.scoreMap.collectAsState()

    // Exam Metadata
    var subjectName by remember { mutableStateOf("") }
    var examName by remember { mutableStateOf("") }
    var maxScore by remember { mutableStateOf("") }

    LaunchedEffect(classroomId) {
        viewModel.loadData(classroomId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Grades") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        val max = maxScore.toDoubleOrNull()
                        if (subjectName.isNotBlank() && examName.isNotBlank() && max != null) {
                            viewModel.saveGrades(classroomId, subjectName, examName, max)
                            onNavigateBack()
                        }
                    }) {
                        Text("SAVE", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header: Exam Details
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject (e.g. Math)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = examName,
                            onValueChange = { examName = it },
                            label = { Text("Exam Name") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxScore,
                            onValueChange = { maxScore = it },
                            label = { Text("Max Score") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is GradeUiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is GradeUiState.Error -> Text("Error: ${state.message}")
                is GradeUiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.students) { student ->
                            GradeItem(
                                student = student,
                                score = scoreMap[student.id] ?: "",
                                onScoreChange = { viewModel.updateScore(student.id, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GradeItem(
    student: Student,
    score: String,
    onScoreChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = student.name, style = MaterialTheme.typography.bodyLarge)
            if (student.seatNumber != null) Text(text = "Seat: ${student.seatNumber}", style = MaterialTheme.typography.bodySmall)
        }
        
        OutlinedTextField(
            value = score,
            onValueChange = onScoreChange,
            modifier = Modifier.width(100.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            label = { Text("Score") }
        )
    }
}
