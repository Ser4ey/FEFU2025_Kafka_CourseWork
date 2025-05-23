import asyncio
import logging
from aiogram import Bot, Dispatcher
from aiogram.fsm.storage.memory import MemoryStorage

from src.config import settings
from src.bot.handlers import start, suggest_news
from src.kafka.consumer import consume_reviewed_news
from src.database.models import Base
from src.database.session import engine

async def main():
    # Настройка логирования
    logging.basicConfig(level=logging.INFO)
    
    # Создание таблиц
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    
    # Инициализация бота
    bot = Bot(token=settings.BOT_TOKEN)
    dp = Dispatcher(storage=MemoryStorage())
    
    # Регистрация роутеров
    dp.include_router(start.router)
    dp.include_router(suggest_news.router)
    
    # Запуск консьюмера Kafka в фоновом режиме
    asyncio.create_task(consume_reviewed_news(bot))
    
    # Запуск бота
    await dp.start_polling(bot)

if __name__ == "__main__":
    asyncio.run(main()) 