from aiogram.types import InlineKeyboardMarkup, InlineKeyboardButton
from database import Database

db = Database('favorites.db')

def create_fav_markup(user_id, item_id, item_type, lang):
    """Створення кнопок для обраного"""
    markup = InlineKeyboardMarkup()
    if db.is_favorite(user_id, item_id):
        markup.row(InlineKeyboardButton(
            text="❌ Видалити з обраного" if lang == 'uk' else 
                 "❌ Удалить из избранного" if lang == 'ru' else 
                 "❌ Remove favorite",
            callback_data=f"remove_fav_{item_id}_{item_type}"
        ))
    else:
        markup.row(InlineKeyboardButton(
            text="❤️ Додати в обране" if lang == 'uk' else 
                 "❤️ Добавить в избранное" if lang == 'ru' else 
                 "❤️ Add to favorites",
            callback_data=f"add_fav_{item_type}_{item_id}"
        ))
    similar_text = {
        'uk': "📺 Схожі" if item_type == 'tv' else "🎬 Схожі",
        'ru': "📺 Похожие" if item_type == 'tv' else "🎬 Похожие",
        'en': "📺 Similar" if item_type == 'tv' else "🎬 Similar"
    }.get(lang, "📺 Similar")
    markup.row(InlineKeyboardButton(
        text=similar_text,
        callback_data=f"similar_{item_type}_{item_id}"
    ))
    markup.add(InlineKeyboardButton(
        text="🎥 Переглянути" if lang == 'uk' else 
             "🎥 Смотреть" if lang == 'ru' else 
             "🎥 Watch",
        callback_data=f"watch_{item_type}_{item_id}"
    ))
    return markup