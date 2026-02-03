package com.example.gradebook.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradebook.data.local.entities.Attendance
import com.example.gradebook.data.local.entities.AttendanceStatus
import com.example.gradebook.data.local.entities.Student
import com.example.gradebook.data.repository.AttendanceRepository
import com.example.gradebook.data.repository.ClassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val classRepository: ClassRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AttendanceUiState>(AttendanceUiState.Loading)
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    // Temporary state to hold current session changes before saving
    private val _attendanceMap = MutableStateFlow<Map<Int, AttendanceStatus>>(emptyMap())
    val attendanceMap: StateFlow<Map<Int, AttendanceStatus>> = _attendanceMap.asStateFlow()

    fun loadData(classroomId: Int) {
        viewModelScope.launch {
            // default date today (midnight for simplicity or current time)
            // For logic, we often normalize date to day.
            val today = getStartOfDay()
            
            classRepository.getStudents(classroomId).collect { students ->
                // Check if attendance already exists for today?
                // For simplicity, we just load students and let user mark. 
                // In a real app, we'd merge with existing records if editing.
                
                // Initialize map with PRESENT for all students by default
                val initialMap = students.associate { it.id to AttendanceStatus.PRESENT }
                _attendanceMap.value = initialMap
                
                _uiState.value = AttendanceUiState.Success(students, today)
            }
        }
    }

    fun markStudent(studentId: Int, status: AttendanceStatus) {
        val current = _attendanceMap.value.toMutableMap()
        current[studentId] = status
        _attendanceMap.value = current
    }

    fun saveAttendance(classroomId: Int) {
        viewModelScope.launch {
            val date = getStartOfDay()
            val list = _attendanceMap.value.map { (studentId, status) ->
                Attendance(
                    studentId = studentId,
                    classroomId = classroomId,
                    date = date,
                    status = status
                )
            }
            attendanceRepository.saveAttendance(list)
            // Show success or navigate back logic handling
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

sealed class AttendanceUiState {
    object Loading : AttendanceUiState()
    data class Success(val students: List<Student>, val date: Long) : AttendanceUiState()
    data class Error(val message: String) : AttendanceUiState()
}
