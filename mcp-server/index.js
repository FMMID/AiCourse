import express from 'express';
import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { SSEServerTransport } from '@modelcontextprotocol/sdk/server/sse.js';
import { CallToolRequestSchema, ListToolsRequestSchema } from '@modelcontextprotocol/sdk/types.js';

const app = express();
const PORT = 3000;

// 1. Конфигурация MCP Сервера
const server = new Server(
  {
    name: "android-test-server",
    version: "1.0.0",
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

// 2. Список инструментов
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: "get_random_weather",
        description: "Get weather for a city (Mock)",
        inputSchema: {
          type: "object",
          properties: {
            city: { type: "string" },
          },
          required: ["city"],
        },
      },
      {
        name: "echo_message",
        description: "Echoes back whatever you send",
        inputSchema: {
          type: "object",
          properties: {
            msg: { type: "string" },
          },
        },
      },
    ],
  };
});

// 3. Выполнение инструментов
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const toolName = request.params.name;

  if (toolName === "get_random_weather") {
    const city = request.params.arguments?.city || "Unknown";
    const temp = Math.floor(Math.random() * 30);
    return {
      content: [{ type: "text", text: `Weather in ${city}: Sunny, ${temp}°C` }],
    };
  }

  if (toolName === "echo_message") {
    const msg = request.params.arguments?.msg || "Silence";
    return {
      content: [{ type: "text", text: `Echo: ${msg}` }],
    };
  }

  throw new Error(`Tool ${toolName} not found`);
});

// 4. Настройка Express (SSE Transport)
let transport;

app.get('/sse', async (req, res) => {
  console.log("-> New connection from Android!");
  transport = new SSEServerTransport("/messages", res);
  await server.connect(transport);
});

app.post('/messages', async (req, res) => {
  if (transport) {
    await transport.handlePostMessage(req, res);
  } else {
    res.status(404).send("Session not found");
  }
});

app.listen(PORT, () => {
  console.log(`\n🚀 MCP Server ready at http://localhost:${PORT}/sse`);
  console.log(`📱 Use http://10.0.2.2:${PORT}/sse in Android Emulator`);
});