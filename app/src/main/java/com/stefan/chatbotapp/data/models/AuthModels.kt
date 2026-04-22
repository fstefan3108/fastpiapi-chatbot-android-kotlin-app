package com.stefan.chatbotapp.data.models

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val id: Int,
    val username: String,
    val email: String
)

data class LoginResponse(
    val access_token: String,
    val token_type: String
)