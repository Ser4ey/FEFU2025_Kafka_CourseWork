import json
import asyncio
from aiokafka import AIOKafkaConsumer
from aiogram import Bot

from config import settings

async def consume_reviewed_news(bot: Bot):
    def deserialize_value(value):
        try:
            return json.loads(value.decode('utf-8'))
        except json.JSONDecodeError as e:
            print(f"Ошибка декодирования JSON: {e}, данные: {value}")
            return None

    consumer = AIOKafkaConsumer(
        settings.KAFKA_NEWS_REVIEWED_TOPIC,
        bootstrap_servers=settings.KAFKA_BOOTSTRAP_SERVERS,
        group_id="news_reception_bot",  # Добавляем группу потребителей для сохранения смещения
        auto_offset_reset="earliest",  # Начинаем с самого раннего сообщения при первом запуске
        enable_auto_commit=True,  # Автоматически сохраняем смещение
        auto_commit_interval_ms=5000,  # Интервал сохранения смещения (5 секунд)
        value_deserializer=deserialize_value
    )
    
    await consumer.start()
    try:
        async for message in consumer:
            if message.value is None:
                print("Получено невалидно сообщение, пропускаем...")
                continue

            news_data = message.value
            author_id = news_data["authorTelegramId"]
            
            status = "принята" if news_data["isAccepted"] else "отклонена"
            message_text = (
                f"Ваша новость '{news_data['newsTitle']}' была {status}.\n"
                f"ID новости: {news_data['newsId']}\n"
                f"Комментарий редактора: {news_data['redactorComment']}"
            )
            print(message_text)
            await bot.send_message(author_id, message_text)
    finally:
        await consumer.stop()