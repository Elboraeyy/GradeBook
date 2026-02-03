package com.example.gradebook.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gradebook.data.repository.TeacherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: TeacherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(name: String, pin: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.loginTeacher(name, pin)
            _uiState.value = if (result.isSuccess) {
                AuthUiState.Success(result.getOrNull()?.id ?: 0)
            } else {
                AuthUiState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, schoolName: String, pin: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.registerTeacher(name, schoolName, pin)
            _uiState.value = if (result.isSuccess) {
                AuthUiState.Success(result.getOrNull()?.id ?: 0)
            } else {
                AuthUiState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val teacherId: Int) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
