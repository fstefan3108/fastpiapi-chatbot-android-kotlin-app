package com.stefan.chatbotapp.data.models
import com.google.gson.annotations.SerializedName

data class SessionResponse(
    @SerializedName("session_id") val session_id: String
)

data class ChatRequest(
    val message: String,
    val session_id: String
)

data class ChatResponse(
    val role: String,
    val message: String,
    @SerializedName("session_id") val session_id: String,
    @SerializedName("website_id") val website_id: Int,
    val timestamp: String? = null
)

data class FullChatResponse(
    @SerializedName("user_message") val user_message: ChatResponse,
    @SerializedName("assistant_reply") val assistant_reply: ChatResponse
)