import asyncio
import logging
from aiogram import Bot, Dispatcher
from config import TELEGRAM_TOKEN
from handlers.start import register_start_handlers
from handlers.language import register_language_handlers
from handlers.top_content import register_top_content_handlers
from handlers.search import register_search_handlers
from handlers.favorites import register_favorites_handlers
from handlers.details import register_details_handlers
from handlers.callbacks import register_callback_handlers

# Налаштування логування
logging.basicConfig(level=logging.INFO)

# Ініціалізація бота та диспетчера
bot = Bot(token=TELEGRAM_TOKEN)
dp = Dispatcher(bot)

# Реєстрація обробників
register_start_handlers(dp)
register_language_handlers(dp)
register_top_content_handlers(dp)
register_search_handlers(dp)
register_favorites_handlers(dp)
register_details_handlers(dp)
register_callback_handlers(dp)

async def main():
    logging.info("Бот запущений...")
    await dp.start_polling()

if __name__ == "__main__":
    asyncio.run(main())