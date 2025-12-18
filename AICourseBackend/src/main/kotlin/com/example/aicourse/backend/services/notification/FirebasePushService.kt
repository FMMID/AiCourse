package com.example.aicourse.backend.services.notification

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import java.io.FileInputStream

object FirebasePushService {

    fun start() {
        val serviceAccount = FileInputStream("firebase-service.json")
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()
        FirebaseApp.initializeApp(options)
    }

    fun sendPush(token: String, title: String, body: String) {
        val message = Message.builder()
            .setToken(token)
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .build()

        try {
            FirebaseMessaging.getInstance().send(message)
        } catch (e: Exception) {
            println("Push error: ${e.message}")
        }
    }
}