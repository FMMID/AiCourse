// bridge.js
let EventSource = require('eventsource');

// Полифилл для EventSource (если библиотека экспортирует объект)
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
const BASE_HOST = 'http://127.0.0.1:8080';
const SSE_PATH = '/sse';
const SSE_URL = BASE_HOST + SSE_PATH;

// Логгер
function log(msg) {
    console.error(`[Bridge] ${msg}`);
}

async function main() {
    log(`Connecting to ${SSE_URL}...`);

    let postUrl = null;

    const es = new EventSource(SSE_URL);

    es.onopen = () => {
        log('SSE Connected');
    };

    es.onerror = (err) => {
        // EventSource часто кидает объект события вместо текста ошибки, поэтому логируем аккуратно
        log('SSE Connection Error (Is server running?)');
    };

    // 2. Ловим событие 'endpoint'
    es.addEventListener('endpoint', (event) => {
        const urlOrPath = event.data; // Сервер может прислать "/messages?..." или "http://..."

        // Превращаем относительный путь в абсолютный URL
        if (urlOrPath.startsWith('/')) {
            postUrl = BASE_HOST + urlOrPath;
        } else {
            postUrl = urlOrPath;
        }

        log(`Endpoint received: ${urlOrPath}`);
        log(`Full POST URL set to: ${postUrl}`);
    });

    es.onmessage = (event) => {
        if (!event.data) return;
        console.log(event.data);
    };

    const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout,
        terminal: false
    });

    rl.on('line', async (line) => {
        if (!line.trim()) return;

        if (!postUrl) {
            log('Wait! No endpoint received yet.');
            return;
        }

        try {
            const response = await fetch(postUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: line
            });

            if (!response.ok) {
                log(`POST Error: ${response.status} ${response.statusText}`);
                const text = await response.text();
                log(`Server response: ${text}`);
            }
        } catch (e) {
            log(`Network Error: ${e.message}`);
        }
    });
}

main();