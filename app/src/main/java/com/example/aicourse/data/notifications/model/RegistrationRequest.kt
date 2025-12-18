package com.example.aicourse.data.notifications.model

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequest(
    val userId: String,
    val token: String
)