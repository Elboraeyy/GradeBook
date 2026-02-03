package com.example.gradebook.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradebook.data.local.entities.Classroom
import com.example.gradebook.data.repository.ClassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ClassRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadClasses(teacherId: Int) {
        viewModelScope.launch {
            repository.getClassroomsForTeacher(teacherId)
                .catch { e -> _uiState.value = HomeUiState.Error(e.message ?: "Failed to load classes") }
                .collect { classes ->
                    _uiState.value = HomeUiState.Success(classes)
                }
        }
    }

    fun addClass(teacherId: Int, name: String, gradeLevel: String) {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is HomeUiState.Success) {
                // Optimistic or waiting? Let's rely on Flow update
                repository.addClassroom(teacherId, name, gradeLevel, "2025-2026")
            }
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val classes: List<Classroom>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
