package com.example.aicourse.presentation.chat.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.aicourse.domain.chat.model.RagMode

object RagUiUtils {
    @Composable
    fun getRagColor(mode: RagMode): Color = when (mode) {
        RagMode.DISABLED -> MaterialTheme.colorScheme.onSurfaceVariant
        RagMode.STANDARD -> MaterialTheme.colorScheme.primary
        RagMode.WITH_RERANKER -> MaterialTheme.colorScheme.tertiary
    }

    @Composable
    fun getRagIcon(mode: RagMode): ImageVector = when (mode) {
        RagMode.DISABLED -> Icons.AutoMirrored.Outlined.LibraryBooks
        RagMode.STANDARD -> Icons.AutoMirrored.Filled.LibraryBooks
        RagMode.WITH_RERANKER -> Icons.Rounded.AutoAwesome
    }
}