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
import com.example.aicourse.domain.chat.model.SystemPrompt
import com.example.aicourse.domain.chat.model.json.JsonOutputPrompt
import com.example.aicourse.domain.chat.model.pc.BuildComputerAssistantPrompt
import com.example.aicourse.ui.theme.AiCourseTheme

@Composable
fun ActivePromptIndicator(
    activePrompt: SystemPrompt<*>,
    onReset: () -> Unit,
    modifier: Modifier = Modifier.Companion
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