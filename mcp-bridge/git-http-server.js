// git-http-server.js
const express = require('express');
const { spawn } = require('child_process');
const bodyParser = require('body-parser');
const path = require('path'); // <--- 1. Добавляем модуль path

const app = express();
const PORT = 3000;

app.use(bodyParser.json());

// 2. Вычисляем абсолютный путь к корню проекта.
// __dirname - это папка, где лежит скрипт (mcp-bridge).
// '..' означает подняться на уровень выше, в корень проекта (AiCourse).
const repoPath = path.resolve(__dirname, '..');

console.log(`Setting GIT_DEFAULT_PATH to: ${repoPath}`);

// 3. Запускаем сервер с переменной окружения
const gitServerProcess = spawn('npx', ['-y', 'git-mcp-server'], {
    stdio: ['pipe', 'pipe', process.stderr],
    shell: true,
    env: {
        ...process.env, // Сохраняем все текущие системные переменные
        GIT_DEFAULT_PATH: repoPath // Добавляем путь к репозиторию
    }
});

console.log('Git MCP Server process started with PID:', gitServerProcess.pid);

app.get('/sse', (req, res) => {
    res.writeHead(200, {
        'Content-Type': 'text/event-stream',
        'Cache-Control': 'no-cache',
        'Connection': 'keep-alive'
    });

    console.log('Client connected to SSE');

    // Отправляем endpoint инициализации
    res.write(`event: endpoint\n`);
    res.write(`data: /message\n\n`);

    const onData = (data) => {
        const lines = data.toString().split('\n');
        lines.forEach(line => {
            if (line.trim()) {
                res.write(`event: message\n`);
                res.write(`data: ${line}\n\n`);
            }
        });
    };

    gitServerProcess.stdout.on('data', onData);

    req.on('close', () => {
        console.log('Client disconnected');
        gitServerProcess.stdout.off('data', onData);
    });
});

app.post('/message', (req, res) => {
    const message = req.body;
    gitServerProcess.stdin.write(JSON.stringify(message) + '\n');
    res.sendStatus(200);
});

app.listen(PORT, () => {
    console.log(`Gateway running at http://localhost:${PORT}/sse`);
    console.log(`For Android Emulator use: http://10.0.2.2:${PORT}/sse`);
});