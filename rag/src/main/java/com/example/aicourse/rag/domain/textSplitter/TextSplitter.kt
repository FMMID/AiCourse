package com.example.aicourse.rag.domain.textSplitter

interface TextSplitter {
    fun split(text: String, chunkSize: Int = 1000, overlap: Int = 200): List<String>
}