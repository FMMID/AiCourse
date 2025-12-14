package com.example.aicourse.data.chat.local.room.converters

import androidx.room.TypeConverter
import com.example.aicourse.domain.chat.model.TokenUsage
import com.example.aicourse.domain.chat.promt.BotResponse
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.settings.model.SettingsChatModel
import com.example.aicourse.domain.tools.ToolResult
import com.example.aicourse.domain.tools.context.model.ContextSummaryInfo
import kotlinx.serialization.encodeToString

/**
 * Room TypeConverters для преобразования сложных типов в String и обратно
 *
 * Использует PolymorphicJson для сериализации/десериализации всех domain моделей
 * Room автоматически применяет эти конвертеры когда видит соответствующие типы в Entity
 */
class RoomTypeConverters {

    private val json = PolymorphicJson.instance

    // ========== BotResponse ==========

    @TypeConverter
    fun fromBotResponse(value: BotResponse?): String? =
        value?.let { json.encodeToString<BotResponse>(it) }

    @TypeConverter
    fun toBotResponse(value: String?): BotResponse? =
        value?.let { json.decodeFromString<BotResponse>(it) }

    // ========== ToolResult ==========

    @TypeConverter
    fun fromToolResult(value: ToolResult?): String? =
        value?.let { json.encodeToString<ToolResult>(it) }

    @TypeConverter
    fun toToolResult(value: String?): ToolResult? =
        value?.let { json.decodeFromString<ToolResult>(it) }

    // ========== SystemPrompt ==========

    @TypeConverter
    fun fromSystemPrompt(value: SystemPrompt<*>?): String? =
        value?.let { json.encodeToString<SystemPrompt<*>>(it) }

    @TypeConverter
    fun toSystemPrompt(value: String?): SystemPrompt<*>? =
        value?.let { json.decodeFromString<SystemPrompt<*>>(it) }

    // ========== SettingsChatModel ==========

    @TypeConverter
    fun fromSettingsChatModel(value: SettingsChatModel?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toSettingsChatModel(value: String?): SettingsChatModel? =
        value?.let { json.decodeFromString(it) }

    // ========== TokenUsage ==========

    @TypeConverter
    fun fromTokenUsage(value: TokenUsage?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toTokenUsage(value: String?): TokenUsage? =
        value?.let { json.decodeFromString(it) }

    // ========== ContextSummaryInfo ==========

    @TypeConverter
    fun fromContextSummaryInfo(value: ContextSummaryInfo?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toContextSummaryInfo(value: String?): ContextSummaryInfo? =
        value?.let { json.decodeFromString(it) }
}
