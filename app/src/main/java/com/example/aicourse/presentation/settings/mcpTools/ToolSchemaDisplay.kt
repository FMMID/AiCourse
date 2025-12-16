package com.example.aicourse.presentation.settings.mcpTools

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aicourse.R
import com.example.aicourse.ui.theme.AiCourseTheme
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.Json

/**
 * Displays the tool's input schema as formatted JSON with monospace font.
 */
@Composable
fun ToolSchemaDisplay(
    schema: ToolSchema?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
    ) {
        Text(
            text = stringResource(R.string.mcp_tool_schema_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .height(100.dp)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(4.dp)
        ) {
            Text(
                text = formatToolSchema(schema),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            )
        }
    }
}

private val jsonFormat = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

private fun formatToolSchema(schema: ToolSchema?): String {
    if (schema == null) return "{}"
    return try {
        jsonFormat.encodeToString(schema)
    } catch (e: Exception) {
        schema.toString()
    }
}

@Preview(showBackground = true)
@Composable
private fun ToolSchemaDisplayPreview() {
    AiCourseTheme {
        val sampleSchema = ToolSchema(
            properties = previewProperties
        )
        ToolSchemaDisplay(
            schema = sampleSchema
        )
    }
}
