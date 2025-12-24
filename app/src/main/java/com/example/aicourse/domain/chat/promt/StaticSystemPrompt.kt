package com.example.aicourse.domain.chat.promt

interface StaticSystemPrompt<out R : BotResponse> : SystemPrompt<R> {

    val contentResourceId: Int?
}