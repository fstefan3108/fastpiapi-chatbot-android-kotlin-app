package com.stefan.chatbotapp.data.models

data class WebsiteRequest(
    val url: String
)

data class WebsiteResponse(
    val id: Int,
    val url: String,
    val title: String,
    val api_key: String
)

data class TaskResponse(
    val task_id: String,
    val status: String
)