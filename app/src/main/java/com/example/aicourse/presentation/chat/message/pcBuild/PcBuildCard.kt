package com.example.aicourse.presentation.chat.message.pcBuild

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aicourse.R
import com.example.aicourse.prompt.pc.PcBuildResponse
import com.example.aicourse.ui.theme.AiCourseTheme

/**
 * Карточка для отображения ответа ассистента по сборке ПК
 */
@Composable
fun PcBuildCard(
    response: PcBuildResponse,
    modifier: Modifier = Modifier
) {
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
                    text = if (response.isFinalBuild) "🖥️ PC Build" else "💬 Assistant",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (response.isFinalBuild && response.pcBuild != null) {
                PcBuildContent(pcBuild = response.pcBuild!!)
            } else {
                Text(
                    text = response.question ?: response.rawContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Preview
@Composable
private fun PcBuildCardDialoguePreview() {
    AiCourseTheme {
        PcBuildCard(
            PcBuildResponse(
                rawContent = "Какой у вас бюджет на сборку ПК?",
                isFinished = false,
                question = "Какой у вас бюджет на сборку ПК?",
                pcBuild = null
            )
        )
    }
}
