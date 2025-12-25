package com.example.aicourse.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {

    @Serializable
    data object RagList : Screen

    @Serializable
    data class Chat(
        val chatId: String,
        val ragIndexIds: String? = null
    ) : Screen

    @Serializable
    data object Settings : Screen
}