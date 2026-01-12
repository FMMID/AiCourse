package com.example.aicourse.prompt

interface StaticSystemPrompt<out R : BotResponse> : SystemPrompt<R> {

    val contentResourceId: Int?
}