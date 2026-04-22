package com.stefan.chatbotapp.ui.dashboard
import com.stefan.chatbotapp.data.models.WebsiteResponse
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stefan.chatbotapp.data.TokenManager
import com.stefan.chatbotapp.data.repository.WebsiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val websites: List<WebsiteResponse> = emptyList(),
    val errorMessage: String? = null,
    val isAddingWebsite: Boolean = false,
    val addWebsiteError: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val repository = WebsiteRepository(tokenManager)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadWebsites()
    }

    fun loadWebsites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            repository.getWebsites().fold(
                onSuccess = { websites ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        websites = websites
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load websites."
                    )
                }
            )
        }
    }

    fun addWebsite(url: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAddingWebsite = true, addWebsiteError = null)
            repository.addWebsite(url).fold(
                onSuccess = {
                    onSuccess()
                    pollUntilWebsiteAppears()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isAddingWebsite = false,
                        addWebsiteError = "Failed to add website. Check the URL."
                    )
                }
            )
        }
    }

    private fun pollUntilWebsiteAppears() {
        viewModelScope.launch {
            val previousCount = _uiState.value.websites.size
            var found = false
            repeat(60) { // 60 × 6 seconds = 360 seconds (6 minutes)
                if (found) return@repeat
                kotlinx.coroutines.delay(6000)
                repository.getWebsites().fold(
                    onSuccess = { websites ->
                        _uiState.value = _uiState.value.copy(websites = websites)
                        if (websites.size > previousCount) {
                            found = true
                            _uiState.value = _uiState.value.copy(isAddingWebsite = false)
                        }
                    },
                    onFailure = {}
                )
            }
            if (!found) {
                _uiState.value = _uiState.value.copy(
                    isAddingWebsite = false,
                    addWebsiteError = "Scraping is taking longer than expected. Pull to refresh manually."
                )
            }
        }
    }

    fun deleteWebsite(id: Int) {
        viewModelScope.launch {
            repository.deleteWebsite(id).fold(
                onSuccess = { loadWebsites() },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to delete website."
                    )
                }
            )
        }
    }
}