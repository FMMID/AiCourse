# Файл: start_ollama.ps1

# 1. Настройка путей и хоста
# Указываем папку, где будут лежать (или лежат) веса моделей
$env:OLLAMA_MODELS="D:\Programms\Ollama\Models"

# "0.0.0.0" позволяет серверу принимать запросы извне (например, от Android Эмулятора)
$env:OLLAMA_HOST="0.0.0.0"

# (Опционально) Разрешаем запросы с любых источников (CORS), если вдруг будут проблемы с браузером/дебаггингом
$env:OLLAMA_ORIGINS="*"

Write-Host "============================================="
Write-Host "   STARTING OLLAMA SERVER FOR AI COURSE      "
Write-Host "============================================="
Write-Host "Models Directory : $env:OLLAMA_MODELS"
Write-Host "Binding Host     : $env:OLLAMA_HOST (Port 11434)"
Write-Host "---------------------------------------------"

# 2. Проверка и скачивание нужных моделей
# Сервер должен быть запущен для pull, но ollama serve блокирует консоль.
# Поэтому мы запускаем serve, а модели лучше проверить заранее или в другом окне.
# Но для удобства просто выведем напоминание.

Write-Host "Make sure you have executed the following commands explicitly if models are missing:"
Write-Host " > ollama pull nomic-embed-text:latest  (for EmbeddingService)"
Write-Host " > ollama pull llama3.2:latest          (for RerankerService)"
Write-Host "---------------------------------------------"

# 3. Запуск сервера
Write-Host "Ollama is running... Press Ctrl+C to stop."
ollama serve