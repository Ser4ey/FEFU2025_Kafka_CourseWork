# News Review Service

Сервис для рецензирования новостей, отправленных корреспондентами. Сервис получает новости через Kafka, позволяет редакторам проверять их и отправляет результаты обратно корреспондентам.

## Установка

1. Клонируйте репозиторий
2. Убедитесь, что у вас установлена Java 17 или выше
3. Установите Gradle (если не установлен)

## Запуск

1. Запустите PostgreSQL и создайте базу данных:
```sql
CREATE DATABASE news_db;
```

2. Запустите Kafka

3. Запустите приложение:
```bash
./gradlew bootRun
```

## Структура проекта

```
newsreview/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/
│   │   │       └── newsreview/
│   │   │           ├── controller/    # REST контроллеры
│   │   │           ├── model/         # Модели данных
│   │   │           ├── repository/    # Репозитории для работы с БД
│   │   │           ├── service/       # Бизнес-логика
│   │   │           └── NewsReviewApplication.kt
│   │   └── resources/
│   │       ├── application.yml       # Конфигурация приложения
│   │       └── schema.sql            # Схема базы данных
│   └── test/                         # Тесты
├── build.gradle.kts                  # Конфигурация сборки
└── settings.gradle.kts              # Настройки проекта
```

## API Endpoints

- `GET /api/news/next` - Получить следующую новость для рецензирования
- `POST /api/news/review` - Отправить рецензию на новость
  - Параметры:
    - `newsId` - ID новости
    - `isAccepted` - Принята ли новость
    - `comment` - Комментарий редактора

## Модели данных

### Article
```kotlin
data class Article(
    val id: Long?,
    val newsId: String,
    val title: String,
    val content: String,
    val authorName: String,
    val publishTime: LocalDateTime
)
```

### NewsToReview
```kotlin
data class NewsToReview(
    val newsId: String,
    val authorTelegramId: Long,
    val authorName: String,
    val newsTitle: String,
    val newsContent: String
)
```

## Требования

- Java 17+
- PostgreSQL 13+
- Apache Kafka
- Gradle 7+

## Конфигурация

Основные настройки в `application.yml`:
```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/news_db
    username: postgres
    password: postgres
  kafka:
    bootstrap-servers: localhost:9092
```

## Разработка

Для запуска тестов:
```bash
./gradlew test
```

Для сборки проекта:
```bash
./gradlew build
``` 