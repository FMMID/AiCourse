package com.example.aicourse.backend.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*

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
}