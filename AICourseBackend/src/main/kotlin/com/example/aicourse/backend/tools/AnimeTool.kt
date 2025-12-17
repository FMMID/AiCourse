package com.example.aicourse.backend.tools

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

//https://docs.api.jikan.moe/#/anime/getanimesearch

@Serializable
data class JikanResponse(
    val data: List<AnimeData>
)

@Serializable
data class AnimeData(
    val title: String,
    val score: Double? = 0.0,
    val synopsis: String? = "No description",
    val url: String,
    val year: Int? = null,
    val genres: List<Genre> = emptyList()
)

@Serializable
data class Genre(val name: String)

private val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            }
        )
    }
}

fun Server.registerAnimeTool() {
    addTool(
        name = "search_anime",
        description = "Search for anime by name, genre, or description. Returns a list of matching anime with details.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("query") {
                    put("type", "string")
                    put("description", "Search query (e.g. 'isekai', 'naruto', 'best romance')")
                }
            }
        )
    ) { callToolRequest ->
        val query = callToolRequest.arguments?.get("query")?.jsonPrimitive?.content ?: ""

        println("🔍 Searching anime for: $query")

        try {
            // Делаем реальный запрос к Jikan API
            val response: JikanResponse = httpClient.get("https://api.jikan.moe/v4/anime") {
                parameter("q", query)
                parameter("limit", 5)
                parameter("sfw", true)
            }.body()

            if (response.data.isEmpty()) {
                return@addTool CallToolResult(
                    content = listOf(TextContent(text = "No anime found for query: $query"))
                )
            }

            // Формируем красивый текст для LLM
            val resultText = buildString {
                appendLine("Found ${response.data.size} anime results:\n")
                response.data.forEach { anime ->
                    appendLine("Title: ${anime.title} (${anime.year ?: "N/A"})")
                    appendLine("Score: ${anime.score ?: "N/A"}/10")
                    appendLine("Genres: ${anime.genres.joinToString { it.name }}")
                    appendLine("Link: ${anime.url}")
                    appendLine("Synopsis: ${anime.synopsis?.take(200)}...") // Обрезаем длинное описание
                    appendLine("---")
                }
            }

            CallToolResult(
                content = listOf(TextContent(text = resultText))
            )
        } catch (e: Exception) {
            e.printStackTrace()
            CallToolResult(
                content = listOf(TextContent(text = "Error connecting to Anime API: ${e.message}"))
            )
        }
    }
}
