package com.example.gradebook.ui.classdetail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradebook.data.local.entities.Classroom
import com.example.gradebook.data.local.entities.Student
import com.example.gradebook.data.repository.ClassRepository
import com.example.gradebook.data.utils.ExcelParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ClassDetailViewModel @Inject constructor(
    private val repository: ClassRepository,
    private val attendanceRepository: com.example.gradebook.data.repository.AttendanceRepository,
    private val gradeRepository: com.example.gradebook.data.repository.GradeRepository,
    private val excelParser: ExcelParser,
    private val exportManager: com.example.gradebook.data.utils.ExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClassDetailUiState>(ClassDetailUiState.Loading)
    val uiState: StateFlow<ClassDetailUiState> = _uiState.asStateFlow()

    fun loadClassDetails(classroomId: Int) {
        viewModelScope.launch {
            val classroom = repository.getClassroom(classroomId)
            if (classroom == null) {
                _uiState.value = ClassDetailUiState.Error("Class not found")
                return@launch
            }

            repository.getStudents(classroomId)
                .catch { e -> _uiState.value = ClassDetailUiState.Error(e.message ?: "Error loading students") }
                .collect { students ->
                    _uiState.value = ClassDetailUiState.Success(classroom, students)
                }
        }
    }

    fun addStudent(classroomId: Int, name: String, seatNumber: String?) {
        viewModelScope.launch {
            repository.addStudentToClass(classroomId, name, seatNumber)
        }
    }

    fun importStudents(context: Context, uri: Uri, classroomId: Int) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                excelParser.parseStudents(context, uri, classroomId)
            }
            result.onSuccess { students ->
                students.forEach { student ->
                    repository.addStudentToClass(classroomId, student.name, student.seatNumber)
                }
            }
        }
    }

    fun exportReport(context: Context, uri: Uri, classroomId: Int) {
        viewModelScope.launch {
            // Fetch everything
            val studentsFlow = repository.getStudents(classroomId)
            val attendanceFlow = attendanceRepository.getAllAttendance(classroomId)
            val gradesFlow = gradeRepository.getAllGrades(classroomId)
            val classroom = repository.getClassroom(classroomId)

            // Combine or just collect one by one since it's a one-off action
            // We'll just collect locally.
            // Note: Flow collection is usually observing. For snapshot, first() is better.
            
            try {
                // simple snapshot:
                val students = studentsFlow.firstOrNull() ?: emptyList()
                val attendance = attendanceFlow.firstOrNull() ?: emptyList()
                val grades = gradesFlow.firstOrNull() ?: emptyList()
                
                withContext(Dispatchers.IO) {
                    val outputStream = context.contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                         exportManager.exportClassReport(outputStream, classroom?.name ?: "Report", students, attendance, grades)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun exportReportPdf(context: Context, uri: Uri, classroomId: Int) {
        viewModelScope.launch {
            val studentsFlow = repository.getStudents(classroomId)
            val attendanceFlow = attendanceRepository.getAllAttendance(classroomId)
            val gradesFlow = gradeRepository.getAllGrades(classroomId)
            val classroom = repository.getClassroom(classroomId)

            try {
                val students = studentsFlow.firstOrNull() ?: emptyList()
                val attendance = attendanceFlow.firstOrNull() ?: emptyList()
                val grades = gradesFlow.firstOrNull() ?: emptyList()
                
                withContext(Dispatchers.IO) {
                    val outputStream = context.contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        exportManager.exportClassReportPdf(outputStream, classroom?.name ?: "Report", students, attendance, grades)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
// Helper to get first item
suspend fun <T> kotlinx.coroutines.flow.Flow<T>.firstOrNull(): T? {
    var result: T? = null
    try {
        collect { 
            result = it
            throw Exception("Found") // break
        }
    } catch (e: Exception) {
        if (e.message != "Found") throw e
    }
    return result
}

sealed class ClassDetailUiState {
    object Loading : ClassDetailUiState()
    data class Success(val classroom: Classroom, val students: List<Student>) : ClassDetailUiState()
    data class Error(val message: String) : ClassDetailUiState()
}
