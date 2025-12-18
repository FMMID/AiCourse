package com.example.aicourse.backend.plugins

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.sse.*
import kotlinx.serialization.json.Json

fun Application.configureHttp() {
    // Логирование запросов
    intercept(ApplicationCallPipeline.Monitoring) {
        val method = call.request.httpMethod.value
        val uri = call.request.uri
        println(">>> REQ: $method $uri")
    }

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("X-Requested-With")
        allowNonSimpleContentTypes = true
        allowCredentials = true
    }

    install(ContentNegotiation) {
        json(Json { prettyPrint = true; isLenient = true; ignoreUnknownKeys = true })
    }

    install(SSE)
}