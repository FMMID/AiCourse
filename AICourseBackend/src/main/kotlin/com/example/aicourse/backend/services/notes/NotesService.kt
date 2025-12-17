package com.example.aicourse.backend.services.notes

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Note(val text: String, val timestamp: Long)

object NotesService {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val dataDir = File("users_data").apply { if (!exists()) mkdirs() }

    private fun getFileForUser(userId: String) = File(dataDir, "$userId.json")

    fun addNote(userId: String, text: String): String {
        val notes = getAllNotes(userId).toMutableList()
        notes.add(Note(text, System.currentTimeMillis()))
        getFileForUser(userId).writeText(json.encodeToString(notes))
        return "Заметка сохранена для $userId. Всего: ${notes.size}"
    }

    fun getAllNotes(userId: String): List<Note> {
        val file = getFileForUser(userId)
        if (!file.exists()) return emptyList()
        return try {
            json.decodeFromString<List<Note>>(file.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun listUsers(): List<String> {
        return dataDir.listFiles()?.map { it.nameWithoutExtension } ?: emptyList()
    }
}
