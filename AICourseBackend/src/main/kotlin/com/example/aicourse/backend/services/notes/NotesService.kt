package com.example.aicourse.backend.services.notes

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

const val FALLBACK_USER = "unknown_user"

@Serializable
data class Note(val text: String, val timestamp: Long)

@Serializable
data class UserData(
    val notes: MutableList<Note> = mutableListOf(),
    var fcmToken: String? = null // Поле для хранения токена
)

object NotesService {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val dataDir = File("users_data").apply { if (!exists()) mkdirs() }

    private fun getFileForUser(userId: String) = File(dataDir, "$userId.json")

    fun addNote(userId: String, text: String): String {
        val userData = loadUserData(userId)
        userData.notes.add(Note(text, System.currentTimeMillis()))
        saveUserData(userId, userData)
        return "Заметка сохранена для $userId. Всего: ${userData.notes.size}"
    }
    fun updateToken(userId: String, token: String) {
        val data = loadUserData(userId)
        data.fcmToken = token
        saveUserData(userId, data)
    }

    fun listUsers(): List<String> {
        return dataDir.listFiles()?.map { it.nameWithoutExtension } ?: emptyList()
    }

    fun getAllNotes(userId: String): List<Note> = loadUserData(userId).notes

    fun getFcmToken(userId: String): String? = loadUserData(userId).fcmToken

    private fun loadUserData(userId: String): UserData {
        val file = getFileForUser(userId)
        if (!file.exists()) return UserData()
        return try {
            json.decodeFromString<UserData>(file.readText())
        } catch (e: Exception) {
            UserData()
        }
    }

    private fun saveUserData(userId: String, data: UserData) {
        getFileForUser(userId).writeText(json.encodeToString(data))
    }
}
