package com.example.aicourse.presentation.chat.message.jsonOutput

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aicourse.R
import com.example.aicourse.domain.chat.model.json.JsonOutputResponse
import com.example.aicourse.ui.theme.AiCourseTheme
import org.json.JSONObject

/**
 * Красивая карточка для отображения JSON ответа
 */
@Composable
fun JsonOutputCard(
    response: JsonOutputResponse,
    modifier: Modifier = Modifier.Companion
) {
    val formattedJson = formatJson(response)

    Card(
        modifier = modifier.widthIn(max = dimensionResource(R.dimen.message_item_max_width)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "📋 JSON Response",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formattedJson,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

/**
 * Форматирует JSON для красивого отображения
 */
private fun formatJson(response: JsonOutputResponse): String {
    return when {
        response.isSuccess() -> {
            buildString {
                appendLine("{")
                appendLine("  \"title\": \"${response.title}\",")
                appendLine("  \"body\": \"${response.body}\"")
                append("}")
            }
        }
        response.isError() -> {
            buildString {
                appendLine("{")
                appendLine("  \"error\": \"${response.error}\"")
                append("}")
            }
        }
        else -> {
            try {
                val json = JSONObject(response.rawContent)
                json.toString(2) // Indent = 2 spaces
            } catch (e: Exception) {
                response.rawContent
            }
        }
    }
}

@Preview
@Composable
private fun JsonOutputCardPreview() {
    AiCourseTheme {
        JsonOutputCard(
            JsonOutputResponse(
                rawContent = "TODO()",
                isValid = true,
                title = "Пример заголовка",
                body = "Подробное описание тела заголовка",
                error = "Вывод ошибки"
            )
        )
    }
}