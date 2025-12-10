package com.example.aicourse.presentation.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.aicourse.R
import com.example.aicourse.domain.chat.model.TokenUsage
import com.example.aicourse.domain.chat.promt.SystemPrompt
import com.example.aicourse.domain.chat.promt.dynamicModel.DynamicModelPrompt
import com.example.aicourse.domain.chat.promt.dynamicSystemPrompt.DynamicSystemPrompt
import com.example.aicourse.domain.chat.promt.dynamicTemperature.DynamicTemperaturePrompt
import com.example.aicourse.domain.chat.promt.json.JsonOutputPrompt
import com.example.aicourse.domain.chat.promt.pc.BuildComputerAssistantPrompt
import com.example.aicourse.ui.theme.AiCourseTheme

@Composable
fun ActivePromptIndicator(
    activePrompt: SystemPrompt<*>,
    onReset: () -> Unit,
    modifier: Modifier = Modifier.Companion,
    tokenUsage: TokenUsage? = null,
    modelName: String? = null
) {
    Row(
        modifier = modifier.padding(vertical = dimensionResource(R.dimen.spacing_small)),
        horizontalArrangement = Arrangement.Start
    ) {
        AssistChip(
            onClick = onReset,
            label = {
                Text(
                    text = when (activePrompt) {
                        is JsonOutputPrompt -> "📊 JSON Mode"
                        is BuildComputerAssistantPrompt -> "🖥️ PC Build Mode"
                        is DynamicTemperaturePrompt -> {
                            val temp = activePrompt.temperature
                            "🌡️ DT Mode | Temp: $temp"
                        }

                        is DynamicModelPrompt -> {
                            buildString {
                                append("🤖 DM")
                                if (modelName != null) {
                                    append(" | ")
                                    val shortModelName = modelName.split("/").lastOrNull()?.take(20) ?: modelName
                                    append(shortModelName)
                                }
                                if (tokenUsage != null && tokenUsage.hasData()) {
                                    append("\n")
                                    append("tt: ${tokenUsage.promptTokens ?: 0} - ")
                                    append("ct: ${tokenUsage.completionTokens ?: 0} - ")
                                    append("tt: ${tokenUsage.totalTokens ?: 0}")
                                }
                            }
                        }

                        is DynamicSystemPrompt -> {
                            val activeName = activePrompt.getActivePromptName()
                            if (activeName != null) {
                                "🔧 Dynamic-system-$activeName"
                            } else {
                                "🔧 Dynamic Mode"
                            }
                        }

                        else -> "🤖 Custom Mode"
                    }
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Reset prompt",
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_tiny))
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                trailingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )
    }
}

@Preview
@Composable
private fun ActivePromptIndicatorJSONPreview() {
    AiCourseTheme {
        ActivePromptIndicator(
            activePrompt = JsonOutputPrompt(),
            onReset = {},
        )
    }
}