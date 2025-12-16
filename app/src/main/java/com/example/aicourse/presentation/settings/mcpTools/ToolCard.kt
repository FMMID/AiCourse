package com.example.aicourse.presentation.settings.mcpTools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aicourse.R
import com.example.aicourse.ui.theme.AiCourseTheme
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema

/**
 * Displays an individual tool in a card.
 */
@Composable
fun ToolCard(
    tool: Tool,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            // Title or name as header
            Text(
                text = tool.title ?: tool.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // If title exists, show name as subtitle
            if (tool.title != null) {
                Text(
                    text = "${stringResource(R.string.mcp_tool_name_label)} ${tool.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Description if available
            tool.description?.let { description ->
                Text(
                    text = "${stringResource(R.string.mcp_tool_description_label)} $description",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Input schema
            ToolSchemaDisplay(schema = tool.inputSchema)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ToolCardPreview() {
    AiCourseTheme {
        ToolCard(
            tool = Tool(
                name = "search_database",
                title = "Database Search Tool",
                description = "Searches the internal database for matching records",
                inputSchema = ToolSchema(
                    properties = previewProperties
                )
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ToolCardMinimalPreview() {
    AiCourseTheme {
        ToolCard(
            tool = Tool(
                name = "simple_tool",
                title = null,
                description = null,
                inputSchema = ToolSchema()
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
