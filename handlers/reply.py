from aiogram.types import ReplyKeyboardMarkup, KeyboardButton

def main_menu(lang='uk'):
    """Створення головного меню"""
    markup = ReplyKeyboardMarkup(resize_keyboard=True, row_width=2)
    if lang == 'uk':
        buttons = [
            KeyboardButton('Топ фільми 🎬'),
            KeyboardButton('Топ серіали 📺'),
            KeyboardButton('Пошук 🔍'),
            KeyboardButton('Обране ❤️'),
            KeyboardButton('Змінити мову 🌐')
        ]
    elif lang == 'ru':
        buttons = [
            KeyboardButton('Топ фильмы 🎬'),
            KeyboardButton('Топ сериалы 📺'),
            KeyboardButton('Поиск 🔍'),
            KeyboardButton('Избранное ❤️'),
            KeyboardButton('Изменить язык 🌐')
        ]
    else:  # en
        buttons = [
            KeyboardButton('Top movies 🎬'),
            KeyboardButton('Top TV shows 📺'),
            KeyboardButton('Search 🔍'),
            KeyboardButton('Favorites ❤️'),
            KeyboardButton('Change language 🌐')
        ]
    markup.add(*buttons)
    return markup

def language_menu():
    """Створення меню вибору мови"""
    markup = ReplyKeyboardMarkup(resize_keyboard=True, row_width=3)
    buttons = [
        KeyboardButton('Українська 🇺🇦'),
        KeyboardButton('Русский 🇷🇺'),
        KeyboardButton('English 🇬🇧')
    ]
    markup.add(*buttons)
    return markup