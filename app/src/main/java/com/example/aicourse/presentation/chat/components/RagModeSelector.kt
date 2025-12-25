package com.example.aicourse.presentation.chat.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aicourse.domain.chat.model.RagMode
import com.example.aicourse.ui.theme.AiCourseTheme

@Composable
fun RagModeSelector(
    currentMode: RagMode,
    onModeSelected: (RagMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            AnimatedContent(
                targetState = currentMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "RagIconAnim"
            ) { mode ->
                Icon(
                    imageVector = RagUiUtils.getRagIcon(mode),
                    contentDescription = "RAG Mode",
                    tint = RagUiUtils.getRagColor(mode)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Text(
                text = "Режим поиска",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
            HorizontalDivider()

            RagMenuItem(
                text = "Выключено",
                subText = null,
                icon = Icons.Rounded.SearchOff,
                isSelected = currentMode == RagMode.DISABLED,
                onClick = {
                    onModeSelected(RagMode.DISABLED)
                    expanded = false
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            RagMenuItem(
                text = "Быстрый поиск",
                subText = "Vector Search",
                icon = Icons.Rounded.Description,
                isSelected = currentMode == RagMode.STANDARD,
                onClick = {
                    onModeSelected(RagMode.STANDARD)
                    expanded = false
                },
                color = MaterialTheme.colorScheme.primary
            )

            RagMenuItem(
                text = "Умный поиск",
                subText = "AI Reranking (Slow)",
                icon = Icons.Rounded.AutoAwesome,
                isSelected = currentMode == RagMode.WITH_RERANKER,
                onClick = {
                    onModeSelected(RagMode.WITH_RERANKER)
                    expanded = false
                },
                color = MaterialTheme.colorScheme.tertiary
            )

            RagMenuItem(
                text = "Премиум поиск",
                subText = "Multi-Query + AI (Slowest)",
                icon = RagUiUtils.getRagIcon(RagMode.WITH_MULTIQUERY),
                isSelected = currentMode == RagMode.WITH_MULTIQUERY,
                onClick = {
                    onModeSelected(RagMode.WITH_MULTIQUERY)
                    expanded = false
                },
                color = RagUiUtils.getRagColor(RagMode.WITH_MULTIQUERY)
            )
        }
    }
}

@Composable
private fun RagMenuItem(
    text: String,
    subText: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    DropdownMenuItem(
        text = {
            Column {
                Text(text)
                if (subText != null) {
                    Text(
                        text = subText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, tint = color)
        },
        onClick = onClick,
        modifier = if (isSelected) Modifier.background(color.copy(alpha = 0.1f)) else Modifier,
        colors = if (isSelected) MenuDefaults.itemColors(textColor = color) else MenuDefaults.itemColors()
    )
}

@Preview(showBackground = true)
@Composable
private fun RagModeSelectorPreview() {
    AiCourseTheme {
        RagModeSelector(
            currentMode = RagMode.STANDARD,
            onModeSelected = {}
        )
    }
}