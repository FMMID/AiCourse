package com.example.aicourse.backend.services

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Note(
    val text: String,
    val timestamp: Long
)

object NotesService {
    private val dbFile = File("notes_db.json")

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun addNote(text: String): String {
        val currentNotes = getAllNotes().toMutableList()
        val newNote = Note(text, System.currentTimeMillis())
        currentNotes.add(newNote)

        saveNotes(currentNotes)
        return "Заметка успешно сохранена! Всего заметок: ${currentNotes.size}"
    }

    fun getAllNotes(): List<Note> {
        if (!dbFile.exists()) return emptyList()
        return try {
            val content = dbFile.readText()
            json.decodeFromString<List<Note>>(content)
        } catch (e: Exception) {
            println("Error reading notes: ${e.message}")
            emptyList()
        }
    }

    private fun saveNotes(notes: List<Note>) {
        val content = json.encodeToString(notes)
        dbFile.writeText(content)
    }
}
