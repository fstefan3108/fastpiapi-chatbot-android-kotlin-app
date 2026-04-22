package com.stefan.chatbotapp.ui.auth
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stefan.chatbotapp.data.TokenManager
import com.stefan.chatbotapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val repository = AuthRepository(tokenManager)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = repository.login(username, password)
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(isSuccess = true)
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState(
                        errorMessage = parseError(e)
                    )
                }
            )
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = repository.register(username, email, password)
            result.fold(
                onSuccess = {
                    _uiState.value = AuthUiState(isSuccess = true)
                },
                onFailure = { e ->
                    _uiState.value = AuthUiState(
                        errorMessage = parseError(e)
                    )
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }

    private fun parseError(e: Throwable): String {
        return when {
            e.message?.contains("Unable to resolve host") == true ->
                "Cannot reach server. Check your connection."
            e.message?.contains("401") == true ->
                "Invalid username or password."
            e.message?.contains("400") == true ->
                "Username or email already exists."
            else -> "Something went wrong. Please try again."
        }
    }
}