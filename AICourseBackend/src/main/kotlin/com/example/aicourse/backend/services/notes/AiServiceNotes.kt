package com.example.aicourse.backend.services.notes

interface AiServiceNotes {

    suspend fun generateSummary(notes: List<Note>): String
}