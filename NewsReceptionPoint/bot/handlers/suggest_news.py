import uuid
from aiogram import Router, F
from aiogram.types import Message
from aiogram.filters import Command
from aiogram.fsm.context import FSMContext
from aiogram.fsm.state import State, StatesGroup

from database.models import Correspondent
from database.session import async_session
from kafka.producer import send_news_to_review

router = Router()

class NewsSuggestion(StatesGroup):
    waiting_for_title = State()
    waiting_for_content = State()

@router.message(Command("suggest_news"))
async def cmd_suggest_news(message: Message, state: FSMContext):
    async with async_session() as session:
        correspondent = await session.get(Correspondent, message.from_user.id)
        if not correspondent:
            await message.answer("Пожалуйста, сначала зарегистрируйтесь с помощью команды /start")
            return
            
    await message.answer("Пожалуйста, введите заголовок новости:")
    await state.set_state(NewsSuggestion.waiting_for_title)

@router.message(NewsSuggestion.waiting_for_title)
async def process_title(message: Message, state: FSMContext):
    await state.update_data(title=message.text)
    await message.answer("Теперь введите текст новости:")
    await state.set_state(NewsSuggestion.waiting_for_content)

@router.message(NewsSuggestion.waiting_for_content)
async def process_content(message: Message, state: FSMContext):
    data = await state.get_data()
    news_id = str(uuid.uuid4())
    
    async with async_session() as session:
        correspondent = await session.get(Correspondent, message.from_user.id)
        
        news_data = {
            "newsId": news_id,
            "authorTelegramId": correspondent.telegram_id,
            "authorName": correspondent.username,
            "newsTitle": data["title"],
            "newsContent": message.text
        }
        
        await send_news_to_review(news_data)
        
    await message.answer(f"Новость успешно отправлена на проверку! ID новости: {news_id}")
    await state.clear() 