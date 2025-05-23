# News Reception Point

Телеграм-бот для отправки новостей на рецензирование и получения результатов проверки.

## Установка

1. Клонируйте репозиторий
2. Создайте виртуальное окружение:
```bash
python -m venv venv
source venv/bin/activate  # для Linux/Mac
venv\Scripts\activate     # для Windows
```

3. Установите зависимости:
```bash
pip install -r requirements.txt
```

4. Скопируйте `.env.example` в `.env` и заполните необходимые переменные окружения:
```bash
cp .env.example .env
```

## Запуск

```bash
python src/main.py
```

## Структура проекта

```
NewsReceptionPoint/
├── src/
│   ├── bot/
│   │   ├── handlers/     # Обработчики команд бота
│   │   ├── keyboards/    # Клавиатуры
│   │   └── states/       # Состояния FSM
│   ├── database/         # Работа с базой данных
│   ├── kafka/           # Работа с Kafka
│   └── utils/           # Вспомогательные функции
├── config/              # Конфигурация
├── tests/              # Тесты
└── requirements.txt    # Зависимости
```

## Команды бота

- `/start` - Начало работы с ботом, регистрация
- `/suggest_news` - Предложить новость на проверку

## Требования

- Python 3.8+
- PostgreSQL
- Kafka 