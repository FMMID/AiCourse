package com.example.aicourse.backend.services.notes

import com.example.aicourse.backend.services.ai.AiProvider
import kotlinx.coroutines.*

object NotesSummaryScheduler {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start() {
        scope.launch {
            while (isActive) {
                val userIds = NotesService.listUsers()
                userIds.forEach { userId ->
                    val notes = NotesService.getAllNotes(userId)
                    if (notes.isNotEmpty()) {
                        val aiServiceNotes = AiProvider.getAiServiceNotes()
                        val summary = aiServiceNotes.generateSummary(notes)
                        // 2. Лог или Пуш
                        println("=== AUTO SUMMARY FOR $userId ===")
                        println(summary)
                        println("================================")
                    }
                }
                delay(3600_000) // Раз в час
            }
        }
    }
}
