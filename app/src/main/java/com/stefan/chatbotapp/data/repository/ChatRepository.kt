package com.stefan.chatbotapp.data.repository

import com.stefan.chatbotapp.data.TokenManager
import com.stefan.chatbotapp.data.api.RetrofitInstance
import com.stefan.chatbotapp.data.models.ChatRequest
import com.stefan.chatbotapp.data.models.FullChatResponse
import com.stefan.chatbotapp.data.models.SessionResponse
import android.util.Log

class ChatRepository(tokenManager: TokenManager) {

    private val api = RetrofitInstance.create(tokenManager)

    suspend fun createSession(): Result<SessionResponse> {
        return try {
            val session = api.createSession()
            Log.d("ChatRepository", "Session created: ${session.session_id}")
            Result.success(session)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Session error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        apiKey: String,
        sessionId: String,
        message: String
    ): Result<FullChatResponse> {
        return try {
            val response = api.sendMessage(
                apiKey = apiKey,
                request = ChatRequest(message = message, session_id = sessionId)
            )
            Log.d("ChatRepository", "Reply: ${response.assistant_reply.message}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Send error: ${e.message}", e)
            Result.failure(e)
        }
    }
}