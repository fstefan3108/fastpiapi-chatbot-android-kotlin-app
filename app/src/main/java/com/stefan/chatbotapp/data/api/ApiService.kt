package com.stefan.chatbotapp.data.api
import com.stefan.chatbotapp.data.models.TaskResponse
import com.stefan.chatbotapp.data.models.WebsiteRequest
import com.stefan.chatbotapp.data.models.WebsiteResponse
import com.stefan.chatbotapp.data.models.*
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @FormUrlEncoded
    @POST("v1/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    // Websites
    @GET("v1/website")
    suspend fun getWebsites(): List<WebsiteResponse>

    @POST("v1/website")
    suspend fun createWebsite(@Body request: WebsiteRequest): TaskResponse

    @DELETE("v1/website/{id}")
    suspend fun deleteWebsite(@Path("id") id: Int): retrofit2.Response<Unit>

    // Chat
    @POST("v1/chatbot/session")
    suspend fun createSession(): SessionResponse

    @POST("v1/chatbot/chat")
    suspend fun sendMessage(
        @Header("x-key") apiKey: String,
        @Body request: ChatRequest
    ): FullChatResponse
}