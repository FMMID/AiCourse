package com.example.aicourse.rag.domain.textSplitter

class RecursiveTextSplitter : TextSplitter {

    // Разделители в порядке приоритета:
    // 1. Двойной перенос (Абзацы)
    // 2. Одинарный перенос (Строки)
    // 3. Точка с пробелом (Предложения)
    // 4. Запятая, Точка с запятой (Части предложений)
    // 5. Пробел (Слова)
    // 6. Пустая строка (Посимвольно - на крайний случай)
    private val separators = listOf("\n\n", "\n", ". ", ", ", " ", "")

    override fun split(text: String, chunkSize: Int, overlap: Int): List<String> {
        return splitText(text, separators, chunkSize, overlap)
    }

    private fun splitText(
        text: String,
        separators: List<String>,
        chunkSize: Int,
        overlap: Int
    ): List<String> {
        val finalChunks = mutableListOf<String>()
        var separator = separators.last()
        var newSeparators = emptyList<String>()

        // 1. Ищем лучший разделитель для текущего текста
        for (i in separators.indices) {
            val s = separators[i]
            if (s == "") {
                separator = s
                break
            }
            // Используем Regex для корректного поиска (quote, чтобы спецсимволы не ломали поиск)
            if (text.contains(s)) {
                separator = s
                newSeparators = separators.subList(i + 1, separators.size)
                break
            }
        }

        // 2. Разбиваем текст по этому разделителю
        val splits = if (separator == "") {
            text.map { it.toString() } // Посимвольно
        } else {
            text.split(separator).filter { it.isNotEmpty() }
        }

        // 3. Собираем мелкие куски обратно в чанки нужного размера
        var currentDoc = mutableListOf<String>()
        var totalLength = 0

        for (split in splits) {
            val splitLen = split.length

            // Если добавление текущего куска превысит размер чанка
            if (totalLength + splitLen + (if (currentDoc.isNotEmpty()) separator.length else 0) > chunkSize) {
                // Если у нас уже есть накопленный чанк -> сохраняем его
                if (currentDoc.isNotEmpty()) {
                    val doc = currentDoc.joinToString(separator)
                    finalChunks.add(doc)

                    // ЛОГИКА OVERLAP (Смещения)
                    // Оставляем хвост текущего чанка для следующего, чтобы не терять контекст на границе
                    // Это упрощенная логика overlap: удаляем начало, пока не влезем в лимит overlap
                    while (totalLength > overlap || (totalLength > 0 && totalLength + splitLen > chunkSize)) {
                        if (currentDoc.isNotEmpty()) {
                            totalLength -= (currentDoc.first().length + separator.length)
                            currentDoc.removeAt(0) // Убираем старое начало
                        } else {
                            break
                        }
                    }
                }
            }

            // Добавляем текущий кусок
            currentDoc.add(split)
            totalLength += splitLen + (if (currentDoc.size > 1) separator.length else 0)
        }

        // Добавляем последний оставшийся кусок
        if (currentDoc.isNotEmpty()) {
            finalChunks.add(currentDoc.joinToString(separator))
        }

        // 4. Пост-обработка: Если какой-то кусок все равно получился слишком большим (например, длиннющий абзац без точек),
        // рекурсивно разбиваем его следующими разделителями
        val result = mutableListOf<String>()
        for (chunk in finalChunks) {
            if (chunk.length > chunkSize && newSeparators.isNotEmpty()) {
                result.addAll(splitText(chunk, newSeparators, chunkSize, overlap))
            } else {
                result.add(chunk)
            }
        }

        return result
    }
}