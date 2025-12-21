package com.example.aicourse.presentation.settings.mcpTools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aicourse.BuildConfig
import com.example.aicourse.R
import com.example.aicourse.mcpclient.McpClientConfig
import com.example.aicourse.ui.theme.AiCourseTheme
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema

/**
 * Collapsible section for each MCP client.
 */
@Composable
fun McpClientSection(
    mcpClientConfig: McpClientConfig,
    tools: List<Tool>?,
    onDownloadClick: (McpClientConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember(mcpClientConfig, tools?.size) { mutableStateOf(tools != null) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_normal)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mcpClientConfig.serverUrl,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.weight(1f))

                // Expand/Collapse icon (only if tools are loaded)
                if (tools != null) {
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = stringResource(
                                if (isExpanded) R.string.mcp_section_collapse else R.string.mcp_section_expand
                            )
                        )
                    }
                }
            }

            // Download button (only visible when tools == null)
            if (tools == null) {
                Button(
                    onClick = { onDownloadClick(mcpClientConfig) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.mcp_get_tools_button))
                }
            }

            // Tools list (visible when expanded and tools are loaded)
            AnimatedVisibility(visible = isExpanded && tools != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                ) {
                    tools?.forEachIndexed { index, tool ->
                        ToolCard(
                            tool = tool,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // HorizontalDivider between tools (except after last)
                        if (index < tools.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun McpClientSectionNoToolsPreview() {
    AiCourseTheme {
        McpClientSection(
            mcpClientConfig = McpClientConfig(BuildConfig.MCP_NOTIFICATION_URL),
            tools = null,
            onDownloadClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun McpClientSectionExpandedPreview() {
    AiCourseTheme {
        McpClientSection(
            mcpClientConfig = McpClientConfig(BuildConfig.MCP_NOTE_URL),
            tools = listOf(
                Tool(
                    name = "get_weather",
                    title = "Get Weather",
                    description = "Gets current weather for a location",
                    inputSchema = ToolSchema(
                        properties = previewProperties
                    )
                ),
                Tool(
                    name = "get_forecast",
                    title = "Get Forecast",
                    description = "Gets weather forecast for next N days",
                    inputSchema = ToolSchema(
                        properties = previewProperties
                    )
                )
            ),
            onDownloadClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
