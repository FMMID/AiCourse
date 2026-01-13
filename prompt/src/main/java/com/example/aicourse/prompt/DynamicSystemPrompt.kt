package com.example.aicourse.prompt

import android.content.Context

interface DynamicSystemPrompt<out R : BotResponse> : SystemPrompt<R> {

    /**
     * Выдает системный промтп для модели
     *
     * @return системный промптл для модели, null - если нет системного промпта
     */
    fun loadSystemPrompt(context: Context): String?
}