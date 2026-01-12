package com.example.aicourse.rag.di.config

/**
 * Конфигурация Ollama сервиса
 */
data class OllamaConfig(
    val baseUrl: String = "http://10.0.2.2:11434", // Для эмулятора
    val embeddingModelName: String = "nomic-embed-text:latest",
    val rerankerModelName: String = "qwen2.5:latest"
) {
    companion object {
        /**
         * Конфигурация для физического устройства
         */
        fun forDevice(ipAddress: String) = OllamaConfig(
            baseUrl = "http://$ipAddress:11434",
            embeddingModelName = "nomic-embed-text:latest",
            rerankerModelName = "qwen2.5:latest"
        )
    }
}
