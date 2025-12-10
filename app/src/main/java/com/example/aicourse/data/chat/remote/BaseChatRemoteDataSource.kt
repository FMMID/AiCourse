package com.example.aicourse.data.chat.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Базовый класс для удаленных источников данных чата
 * Содержит общую логику для всех Chat API провайдеров
 */
abstract class BaseChatRemoteDataSource : ChatRemoteDataSource {

    /**
     * TAG для логирования (переопределяется в наследниках)
     */
    protected abstract val logTag: String

    /**
     * Настроенный HTTP клиент с поддержкой JSON сериализации и логирования
     */
    protected val httpClient: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(logTag, message)
                }
            }
            level = LogLevel.HEADERS
        }
    }

    /**
     * Закрывает HTTP клиент и освобождает ресурсы
     */
    open fun close() {
        httpClient.close()
    }
}
