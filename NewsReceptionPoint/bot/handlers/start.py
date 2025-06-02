from aiogram import Router, F
from aiogram.types import Message
from aiogram.filters import Command
from aiogram.fsm.context import FSMContext
from aiogram.fsm.state import State, StatesGroup

from database.models import Correspondent
from database.session import async_session

router = Router()

class Registration(StatesGroup):
    waiting_for_name = State()

@router.message(Command("start"))
async def cmd_start(message: Message, state: FSMContext):
    async with async_session() as session:
        correspondent = await session.get(Correspondent, message.from_user.id)
        
        if correspondent:
            await message.answer(
                "Добро пожаловать! Вот список доступных команд:\n"
                "/start - показать это сообщение\n"
                "/suggest_news - предложить новость"
            )
            return
            
        await message.answer("Добро пожаловать! Для регистрации, пожалуйста, введите ваше имя:")
        await state.set_state(Registration.waiting_for_name)

@router.message(Registration.waiting_for_name)
async def process_name(message: Message, state: FSMContext):
    async with async_session() as session:
        correspondent = Correspondent(
            telegram_id=message.from_user.id,
            username=message.text
        )
        session.add(correspondent)
        await session.commit()
        
    await message.answer(
        "Регистрация успешно завершена! Вот список доступных команд:\n"
        "/start - показать это сообщение\n"
        "/suggest_news - предложить новость"
    )
    await state.clear() 