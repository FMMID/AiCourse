package com.example.aicourse.rag.domain.textSplitter

class SimpleTextSplitter : TextSplitter {
    override fun split(text: String, chunkSize: Int, overlap: Int): List<String> {
        val chunks = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val end = (start + chunkSize).coerceAtMost(text.length)
            chunks.add(text.substring(start, end))

            if (end == text.length) break

            // Сдвигаем назад на overlap, чтобы контекст не терялся на границах
            start += chunkSize - overlap
        }
        return chunks
    }
}