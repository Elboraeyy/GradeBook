package com.example.gradebook.ui.grades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradebook.data.local.entities.GradeRecord
import com.example.gradebook.data.local.entities.Student
import com.example.gradebook.data.repository.ClassRepository
import com.example.gradebook.data.repository.GradeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GradeViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val gradeRepository: GradeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GradeUiState>(GradeUiState.Loading)
    val uiState: StateFlow<GradeUiState> = _uiState.asStateFlow()

    // Temporary map for holding scores: StudentID -> Score String
    private val _scoreMap = MutableStateFlow<Map<Int, String>>(emptyMap())
    val scoreMap: StateFlow<Map<Int, String>> = _scoreMap.asStateFlow()

    fun loadData(classroomId: Int) {
        viewModelScope.launch {
            classRepository.getStudents(classroomId).collect { students ->
                _uiState.value = GradeUiState.Success(students)
            }
        }
    }

    fun updateScore(studentId: Int, score: String) {
        val current = _scoreMap.value.toMutableMap()
        current[studentId] = score
        _scoreMap.value = current
    }

    fun saveGrades(classroomId: Int, subject: String, exam: String, maxScore: Double) {
        viewModelScope.launch {
            val list = _scoreMap.value.mapNotNull { (studentId, scoreStr) ->
                val score = scoreStr.toDoubleOrNull()
                if (score != null) {
                    GradeRecord(
                        studentId = studentId,
                        classroomId = classroomId,
                        subjectName = subject,
                        examName = exam,
                        score = score,
                        maxScore = maxScore,
                        date = System.currentTimeMillis()
                    )
                } else null
            }
            gradeRepository.saveGrades(list)
            // Handle success/nav back
        }
    }
}

sealed class GradeUiState {
    object Loading : GradeUiState()
    data class Success(val students: List<Student>) : GradeUiState()
    data class Error(val message: String) : GradeUiState()
}
