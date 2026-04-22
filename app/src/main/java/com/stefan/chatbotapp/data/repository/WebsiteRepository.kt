package com.stefan.chatbotapp.data.repository
import com.stefan.chatbotapp.data.TokenManager
import com.stefan.chatbotapp.data.api.RetrofitInstance
import com.stefan.chatbotapp.data.models.WebsiteResponse
import com.stefan.chatbotapp.data.models.WebsiteRequest

class WebsiteRepository(tokenManager: TokenManager) {

    private val api = RetrofitInstance.create(tokenManager)

    suspend fun getWebsites(): Result<List<WebsiteResponse>> {
        return try {
            Result.success(api.getWebsites())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addWebsite(url: String): Result<Unit> {
        return try {
            api.createWebsite(WebsiteRequest(url))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteWebsite(id: Int): Result<Unit> {
        return try {
            val response = api.deleteWebsite(id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Delete failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}