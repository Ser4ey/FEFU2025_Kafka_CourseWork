import json
from aiokafka import AIOKafkaProducer

from src.config import settings

async def get_producer():
    producer = AIOKafkaProducer(
        bootstrap_servers=settings.KAFKA_BOOTSTRAP_SERVERS,
        value_serializer=lambda v: json.dumps(v).encode('utf-8')
    )
    await producer.start()
    return producer

async def send_news_to_review(news_data: dict):
    producer = await get_producer()
    try:
        await producer.send_and_wait(
            topic=settings.KAFKA_NEWS_TO_REVIEW_TOPIC,
            value=news_data
        )
    finally:
        await producer.stop() 