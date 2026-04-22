package com.stefan.chatbotapp.data.repository
import com.stefan.chatbotapp.data.TokenManager
import com.stefan.chatbotapp.data.api.RetrofitInstance
import com.stefan.chatbotapp.data.models.RegisterRequest

class AuthRepository(private val tokenManager: TokenManager) {

    private val api = RetrofitInstance.create(tokenManager)

    suspend fun login(username: String, password: String): Result<Unit> {
        return try {
            val response = api.login(username, password)
            tokenManager.saveToken(response.access_token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<Unit> {
        return try {
            api.register(RegisterRequest(username, email, password))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}