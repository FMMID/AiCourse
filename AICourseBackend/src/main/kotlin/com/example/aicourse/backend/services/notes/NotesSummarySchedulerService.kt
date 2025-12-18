package com.example.aicourse.backend.services.notes

import com.example.aicourse.backend.services.ai.AiProvider
import com.example.aicourse.backend.services.notification.FirebasePushService
import kotlinx.coroutines.*

object NotesSummarySchedulerService {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start() {
        scope.launch {
            while (isActive) {
                val userIds = NotesService.listUsers()
                userIds.forEach { userId ->
                    val notes = NotesService.getAllNotes(userId)
                    val activeNotes = notes.filter { !it.isCompleted }
                    println("userId: $userId notes:${activeNotes.size}")
                    if (activeNotes.isNotEmpty()) {
                        val aiServiceNotes = AiProvider.getAiServiceNotes()
                        val summary = aiServiceNotes.generateSummary(activeNotes)
                        val token = NotesService.getFcmToken(userId)

                        if (token != null) {
                            FirebasePushService.sendPush(token, "Твоя сводка дел 🤖", summary)
                            println("Push sent to $userId")
                        } else {
                            println("No FCM token for user $userId, skipping push")
                        }
                    }
                }
                delay(60_000) // Раз в час
            }
        }
    }
}
