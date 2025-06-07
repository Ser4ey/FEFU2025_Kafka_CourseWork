import json
import asyncio
from aiokafka import AIOKafkaConsumer
from aiogram import Bot

from config import settings

async def consume_reviewed_news(bot: Bot):
    consumer = AIOKafkaConsumer(
        settings.KAFKA_NEWS_REVIEWED_TOPIC,
        bootstrap_servers=settings.KAFKA_BOOTSTRAP_SERVERS,
        value_deserializer=lambda m: json.loads(m.decode('utf-8'))
    )
    
    await consumer.start()
    try:
        async for message in consumer:
            news_data = message.value
            author_id = news_data["authorTelegramId"]
            
            status = "принята" if news_data["isAccepted"] else "отклонена"
            message_text = (
                f"Ваша новость '{news_data['newsTitle']}' была {status}.\n"
                f"ID новости: {news_data['newsId']}"
                f"Комментарий редактора: {news_data['redactorComment']}"
            )
            print(message_text)
            await bot.send_message(author_id, message_text)
    finally:
        await consumer.stop() 