package com.stefan.chatbotapp.ui.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stefan.chatbotapp.data.TokenManager
import com.stefan.chatbotapp.data.models.ChatResponse
import com.stefan.chatbotapp.data.models.WebsiteResponse
import com.stefan.chatbotapp.data.repository.ChatRepository
import com.stefan.chatbotapp.data.repository.WebsiteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val websites: List<WebsiteResponse> = emptyList(),
    val selectedWebsite: WebsiteResponse? = null,
    val sessionId: String? = null,
    val messages: List<ChatResponse> = emptyList(),
    val isLoadingWebsites: Boolean = false,
    val isLoadingSession: Boolean = false,
    val isSending: Boolean = false,
    val isTyping: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val websiteRepository = WebsiteRepository(tokenManager)
    private val chatRepository = ChatRepository(tokenManager)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    init {
        loadWebsites()
    }

    private fun loadWebsites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingWebsites = true)
            websiteRepository.getWebsites().fold(
                onSuccess = { websites ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingWebsites = false,
                        websites = websites
                    )
                },
                onFailure = { e ->
                    Log.e("ChatViewModel", "Load websites error: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(
                        isLoadingWebsites = false,
                        errorMessage = "Failed to load websites."
                    )
                }
            )
        }
    }

    fun selectWebsite(website: WebsiteResponse) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedWebsite = website,
                isLoadingSession = true,
                messages = emptyList(),
                sessionId = null,
                errorMessage = null
            )
            chatRepository.createSession().fold(
                onSuccess = { session ->
                    Log.d("ChatViewModel", "Session ready: ${session.session_id}")
                    _uiState.value = _uiState.value.copy(
                        sessionId = session.session_id,
                        isLoadingSession = false
                    )
                },
                onFailure = { e ->
                    Log.e("ChatViewModel", "Session error: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(
                        isLoadingSession = false,
                        errorMessage = "Failed to start session: ${e.message}"
                    )
                }
            )
        }
    }

    fun sendMessage(message: String) {
        val state = _uiState.value
        val apiKey = state.selectedWebsite?.api_key ?: return
        val sessionId = state.sessionId ?: return

        // Optimistically add user message immediately
        val userMessage = ChatResponse(
            role = "user",
            message = message,
            session_id = sessionId,
            website_id = state.selectedWebsite.id,
            timestamp = null
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isSending = true,
            isTyping = true,
            errorMessage = null
        )

        viewModelScope.launch {
            chatRepository.sendMessage(apiKey, sessionId, message).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        isTyping = false,
                        messages = _uiState.value.messages + response.assistant_reply
                    )
                },
                onFailure = { e ->
                    Log.e("ChatViewModel", "Send error: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        isTyping = false,
                        errorMessage = "Failed to get response: ${e.message}"
                    )
                }
            )
        }
    }
}