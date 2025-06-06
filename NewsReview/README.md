# NewsReview - Система рецензирования новостей

## Описание проекта

NewsReview - это веб-приложение для рецензирования новостей, построенное на архитектуре микросервисов с использованием Apache Kafka для обмена сообщениями. Система позволяет редакторам просматривать поступающие новости, принимать решения об их публикации и оставлять комментарии.

### Основные возможности

- 📰 Получение новостей для рецензирования из Kafka топика `news-to-review`
- ✅ Принятие или отклонение новостей с обязательным комментарием
- 📤 Отправка результатов рецензирования в топик `news-reviewed`
- 📊 Автоматическая публикация принятых новостей в топик `news-to-public`
- 💾 Сохранение опубликованных статей в базе данных PostgreSQL
- 🌐 Простой веб-интерфейс для работы редакторов

## Используемые технологии

### Backend
- **Kotlin** - основной язык программирования
- **Spring Boot 3.2.0** - фреймворк для создания приложения
- **Spring WebFlux** - реактивный веб-фреймворк
- **Spring Data R2DBC** - реактивная работа с базой данных
- **Spring Kafka** - интеграция с Apache Kafka
- **Reactor Kafka** - реактивная работа с Kafka

### Frontend
- **Thymeleaf** - шаблонизатор для веб-страниц
- **Bootstrap 5.3** - CSS фреймворк для стилизации
- **HTML5/CSS3/JavaScript** - базовые веб-технологии

### База данных
- **PostgreSQL** - реляционная база данных
- **R2DBC PostgreSQL** - реактивный драйвер для PostgreSQL

### Инфраструктура
- **Apache Kafka** - брокер сообщений
- **Gradle** - система сборки проекта
- **Docker** (рекомендуется для развертывания)

## Архитектура системы

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   news-to-review│───▶│   NewsReview     │───▶│  news-reviewed  │
│     (Kafka)     │    │   Application    │    │    (Kafka)      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │                          │
                              ▼                          ▼
                       ┌─────────────┐           ┌─────────────────┐
                       │ PostgreSQL  │           │ news-to-public  │
                       │ Database    │           │    (Kafka)      │
                       └─────────────┘           └─────────────────┘
```

## Структура проекта

```
src/
├── main/
│   ├── kotlin/org/example/newsreview/
│   │   ├── config/                 # Конфигурация приложения
│   │   │   ├── DatabaseConfig.kt   # Настройки базы данных
│   │   │   ├── KafkaConfig.kt      # Настройки Kafka
│   │   │   └── GlobalErrorHandler.kt # Обработка ошибок
│   │   ├── controller/             # Веб-контроллеры
│   │   │   ├── NewsController.kt   # REST API контроллер
│   │   │   └── WebController.kt    # Веб-интерфейс контроллер
│   │   ├── model/                  # Модели данных
│   │   │   ├── Article.kt          # Модель статьи
│   │   │   ├── NewsToReview.kt     # Модель новости для рецензирования
│   │   │   ├── NewsReviewed.kt     # Модель рецензированной новости
│   │   │   └── NewsToPublic.kt     # Модель новости для публикации
│   │   ├── repository/             # Репозитории для работы с БД
│   │   │   └── ArticleRepository.kt
│   │   ├── service/                # Бизнес-логика
│   │   │   ├── NewsService.kt      # Сервис работы с новостями
│   │   │   └── KafkaService.kt     # Сервис работы с Kafka
│   │   └── NewsReviewApplication.kt # Главный класс приложения
│   └── resources/
│       ├── application.yml         # Конфигурация приложения
│       ├── static/schema.sql       # SQL схема базы данных
│       └── templates/              # HTML шаблоны
│           ├── index.html          # Главная страница
│           └── error.html          # Страница ошибок
└── test/                          # Тесты
```

## Требования к системе

- **Java 17** или выше
- **Apache Kafka** (версия 2.8+)
- **PostgreSQL** (версия 12+)
- **Gradle** (версия 8.14+)

## Установка и запуск

### 1. Подготовка инфраструктуры

#### Запуск PostgreSQL и Apache Kafka
```bash
# Используя Docker Compose в корне проектов
docker-compose up -d
```
### 2. Сборка и запуск приложения

```bash
# Клонирование проекта (если необходимо)
git clone <repository-url>
cd NewsReview

# Сборка проекта
./gradlew build

# Запуск приложения
./gradlew bootRun
```



### 3. Проверка работы

Приложение будет доступно по адресу: http://localhost:8080

## Конфигурация

Основные настройки находятся в файле `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: news-review-service
  r2dbc:
    url: r2dbc:postgresql://localhost:5433/news_db
    username: postgres
    password: postgres
  kafka:
    bootstrap-servers: localhost:9094
    consumer:
      group-id: news-review-group
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

server:
  port: 8080
```

## API Endpoints

### Web Interface
- `GET /` - Главная страница для рецензирования новостей
- `POST /review` - Отправка результата рецензирования

### REST API
- `GET /api/news/current` - Получение текущей новости для рецензирования
- `POST /api/news/review` - Отправка результата рецензирования через API

## Структура данных

### Kafka Messages

#### news-to-review (входящие новости)
```json
{
  "newsId": "string",
  "authorTelegramId": 123456789,
  "authorName": "string",
  "newsTitle": "string",
  "newsContent": "string"
}
```

#### news-reviewed (результаты рецензирования)
```json
{
  "newsId": "string",
  "authorTelegramId": 123456789,
  "newsTitle": "string",
  "isAccepted": true,
  "redactorComment": "string"
}
```

#### news-to-public (новости для публикации)
```json
{
  "newsId": "string",
  "authorName": "string",
  "newsTitle": "string",
  "newsContent": "string"
}
```

### База данных

#### Таблица articles
```sql
CREATE TABLE articles (
    id SERIAL PRIMARY KEY,
    news_id VARCHAR(50) UNIQUE,
    title VARCHAR(500),
    content TEXT,
    author_name VARCHAR(255),
    publish_time TIMESTAMP NOT NULL
);
```

## Особенности реализации

1. **Реактивное программирование**: Использование Spring WebFlux и R2DBC для неблокирующих операций
2. **Надежность**: Новости не помечаются как прочитанные до завершения рецензирования
3. **Автоматическая публикация**: Принятые новости автоматически отправляются в топик для публикации
4. **Обработка ошибок**: Глобальная обработка ошибок с информативными сообщениями
5. **Веб-интерфейс**: Простой и интуитивный интерфейс для редакторов