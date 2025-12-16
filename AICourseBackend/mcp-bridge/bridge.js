// bridge.js
let EventSource = require('eventsource');
if (typeof EventSource !== 'function') {
    if (EventSource.EventSource) {
        EventSource = EventSource.EventSource;
    } else if (EventSource.default) {
        EventSource = EventSource.default;
    }
}
if (typeof EventSource !== 'function' && global.EventSource) {
    EventSource = global.EventSource;
}

const readline = require('readline');

// Адрес Ktor сервера
const SSE_URL = 'http://127.0.0.1:8080/sse';

// Логгер ошибок (пишем в stderr, чтобы не ломать JSON-RPC в stdout)
function log(msg) {
    console.error(`[Bridge] ${msg}`);
}

async function main() {
    log(`Connecting to ${SSE_URL}...`);

    let postUrl = null;
    let sessionId = null;

    // 1. Подключаемся к SSE (слушаем сервер)
    const es = new EventSource(SSE_URL);

    es.onopen = () => {
        log('SSE Connected');
    };

    es.onerror = (err) => {
        log('SSE Error. Is the Ktor server running?');
    };

    // 2. Ловим событие 'endpoint' - сервер сообщает, куда слать POST запросы
    es.addEventListener('endpoint', (event) => {
        const fullUrl = event.data; // Приходит что-то типа http://.../messages/UUID
        postUrl = fullUrl;

        // Вытащим SessionID из URL для красоты логов
        const parts = fullUrl.split('/');
        sessionId = parts[parts.length - 1];

        log(`Endpoint received! SessionID: ${sessionId}`);
        log(`POST URL: ${postUrl}`);
    });

    // 3. Ловим обычные сообщения от сервера (ответы ИИ)
    es.onmessage = (event) => {
        if (!event.data) return;

        // Просто печатаем JSON в stdout - Claude это прочитает
        console.log(event.data);
    };

    // 4. Слушаем Claude (stdin)
    const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout,
        terminal: false
    });

    rl.on('line', async (line) => {
        if (!line.trim()) return;

        if (!postUrl) {
            log('Wait! No endpoint received yet. Cannot send message.');
            return;
        }

        try {
            // Отправляем сообщение от Claude на сервер
            const response = await fetch(postUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: line
            });

            if (!response.ok) {
                log(`POST Error: ${response.status} ${response.statusText}`);
            }
        } catch (e) {
            log(`Network Error: ${e.message}`);
        }
    });
}

main();