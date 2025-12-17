// bridge.js
let EventSource = require('eventsource');

// Полифилл для EventSource
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

// Адрес твоего сервера
const BASE_HOST = 'http://127.0.0.16:8080'; //change to own ip
const SSE_PATH = '/sse';
const SSE_URL = BASE_HOST + SSE_PATH;

// Логгер
function log(msg) {
    console.error(`[Bridge] ${msg}`);
}

async function main() {
    log(`Connecting to ${SSE_URL}...`);

    let postUrl = null;
    let messageQueue = []; // <--- 1. Карман для сообщений
    let isFlushing = false;

    // Функция отправки (вынесли отдельно)
    const sendMessage = async (line) => {
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
    };

    const es = new EventSource(SSE_URL);

    es.onopen = () => {
        log('SSE Connected');
    };

    es.onerror = (err) => {
        log('SSE Connection Error. Waiting for retry...');
    };

    es.addEventListener('endpoint', async (event) => {
        const urlOrPath = event.data;
        if (urlOrPath.startsWith('/')) {
            postUrl = BASE_HOST + urlOrPath;
        } else {
            postUrl = urlOrPath;
        }

        log(`Endpoint received. Ready to send! URL: ${postUrl}`);

        // <--- 2. Как только получили адрес, отправляем всё, что накопилось
        if (messageQueue.length > 0) {
            log(`Flushing ${messageQueue.length} queued messages...`);
            for (const line of messageQueue) {
                await sendMessage(line);
            }
            messageQueue = [];
        }
    });

    es.onmessage = (event) => {
        if (!event.data) return;
        // Важно: печатаем ответ сервера в stdout для Claude
        console.log(event.data);
    };

    const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout,
        terminal: false
    });

    rl.on('line', (line) => {
        if (!line.trim()) return;

        if (!postUrl) {
            // <--- 3. Если адреса нет, кладем в очередь, а не выкидываем
            log('Buffering message (waiting for endpoint)...');
            messageQueue.push(line);
            return;
        }

        // Если адрес есть - отправляем сразу
        sendMessage(line);
    });
}

main();