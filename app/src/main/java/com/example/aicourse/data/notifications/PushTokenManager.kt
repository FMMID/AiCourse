package com.example.aicourse.data.notifications

import android.util.Log
import com.example.aicourse.mcpclient.UserSession
import com.example.aicourse.data.notifications.model.RegistrationRequest
import com.google.firebase.messaging.FirebaseMessaging
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

//TODO отрефачить потом
object PushTokenManager {
    private const val SERVER_URL = "${UserSession.BASE_URL}register-push-token"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    fun register(userId: String) {
        // 1. Получаем токен от Firebase
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("PushTokenManager", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Токен получен
            val token = task.result
            Log.d("PushTokenManager", "FCM Token: $token")

            // 2. Отправляем на наш сервер
            sendTokenToServer(userId, token)
        }
    }

    private fun sendTokenToServer(userId: String, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.post(SERVER_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(RegistrationRequest(userId, token))
                }
                Log.d("PushTokenManager", "Server response: ${response.bodyAsText()}")
            } catch (e: Exception) {
                Log.e("PushTokenManager", "Failed to send token", e)
            }
        }
    }
}
