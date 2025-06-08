from aiogram import Bot
from aiogram.types import BotCommand

async def set_bot_commands(bot: Bot):
    """
    Устанавливает команды бота в меню
    """
    commands = [
        BotCommand(command="start", description="Начать работу с ботом"),
        BotCommand(command="suggest_news", description="Предложить новость")
    ]
    
    await bot.set_my_commands(commands)