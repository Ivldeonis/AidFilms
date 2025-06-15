def format_movies_list(movies, lang='uk'):
    """Форматування списку фільмів"""
    header = {
        'uk': "🏆 Топ фільмів:\n\n",
        'ru': "🏆 Топ фильмов:\n\n",
        'en': "🏆 Top movies:\n\n"
    }.get(lang, "")
    items = []
    for movie in movies[:10]:
        title = movie.get('title', 'Невідомо')
        original_title = movie.get('original_title', title)
        year = movie.get('release_date', '')[:4] if movie.get('release_date') else 'Невідомо'
        rating = movie.get('vote_average', 0)
        genres = ", ".join([g['name'] for g in movie.get('genres', [])[:2]]) or 'Невідомо'
        item = f"🎬 <b>{title}</b> ({original_title})\n"
        item += f"📅 {year} | ⭐ {rating}/10 | 🎭 {genres}\n"
        item += f"🔗 /movie_{movie['id']}\n"
        items.append(item)
    return header + "\n".join(items)

def format_tv_shows_list(tv_shows, lang='uk'):
    """Форматування списку серіалів"""
    header = {
        'uk': "📺 Топ серіалів:\n\n",
        'ru': "📺 Топ сериалов:\n\n",
        'en': "📺 Top TV shows:\n\n"
    }.get(lang, "")
    items = []
    for show in tv_shows[:10]:
        title = show.get('name', 'Невідомо')
        original_title = show.get('original_name', title)
        year = show.get('first_air_date', '')[:4] if show.get('first_air_date') else 'Невідомо'
        rating = show.get('vote_average', 0)
        genres = ", ".join([g['name'] for g in show.get('genres', [])[:2]]) or 'Невідомо'
        item = f"📺 <b>{title}</b> ({original_title})\n"
        item += f"📅 {year} | ⭐ {rating}/10 | 🎭 {genres}\n"
        item += f"🔗 /tv_{show['id']}\n"
        items.append(item)
    return header + "\n".join(items)

def format_search_results(results, lang='uk'):
    """Форматування результатів пошуку"""
    header = {
        'uk': "🔍 Результати пошуку:\n\n",
        'ru': "🔍 Результаты поиска:\n\n",
        'en': "🔍 Search results:\n\n"
    }.get(lang, "")
    items = []
    for item in results[:5]:
        if item['media_type'] == 'movie':
            title = item.get('title', 'Невідомо')
            original_title = item.get('original_title', title)
            year = item.get('release_date', '')[:4] if item.get('release_date') else 'Невідомо'
            item_type = {
                'uk': "Фільм",
                'ru': "Фильм",
                'en': "Movie"
            }.get(lang, "Movie")
            prefix = "🎬"
            command = f"/movie_{item['id']}"
        else:
            title = item.get('name', 'Невідомо')
            original_title = item.get('original_name', title)
            year = item.get('first_air_date', '')[:4] if item.get('first_air_date') else 'Невідомо'
            item_type = {
                'uk': "Серіал",
                'ru': "Сериал",
                'en': "TV Show"
            }.get(lang, "TV Show")
            prefix = "📺"
            command = f"/tv_{item['id']}"
        rating = item.get('vote_average', 0)
        result_item = f"{prefix} <b>{title}</b> ({original_title}) - {item_type}\n"
        result_item += f"📅 {year} | ⭐ {rating}/10\n"
        result_item += f"🔗 {command}\n"
        items.append(result_item)
    return header + "\n".join(items)

def format_movie_details(movie, lang='uk'):
    """Форматування деталей фільму"""
    title = movie.get('title', 'Невідомо')
    original_title = movie.get('original_title', title)
    year = movie.get('release_date', '')[:4] if movie.get('release_date') else 'Невідомо'
    rating = movie.get('vote_average', 0)
    genres = ", ".join([g['name'] for g in movie.get('genres', [])]) or 'Невідомо'
    overview = movie.get('overview', 
                        'Опис відсутній.' if lang == 'uk' else 
                        'Описание отсутствует.' if lang == 'ru' else 
                        'No overview available.')
    response = f"🎬 <b>{title}</b> ({original_title})\n\n"
    response += f"📅 <b>{'Рік' if lang == 'uk' else 'Год' if lang == 'ru' else 'Year'}:</b> {year}\n"
    response += f"⭐ <b>{'Рейтинг' if lang in ['uk', 'ru'] else 'Rating'}:</b> {rating}/10\n"
    response += f"🎭 <b>{'Жанри' if lang in ['uk', 'ru'] else 'Genres'}:</b> {genres}\n\n"
    response += f"📖 <b>{'Опис' if lang == 'uk' else 'Описание' if lang == 'ru' else 'Overview'}:</b>\n{overview}\n\n"
    return response

def format_tv_show_details(tv_show, lang='uk'):
    """Форматування деталей серіалу"""
    title = tv_show.get('name', 'Невідомо')
    original_title = tv_show.get('original_name', title)
    year = tv_show.get('first_air_date', '')[:4] if tv_show.get('first_air_date') else 'Невідомо'
    rating = tv_show.get('vote_average', 0)
    genres = ", ".join([g['name'] for g in tv_show.get('genres', [])]) or 'Невідомо'
    overview = tv_show.get('overview', 
                         'Опис відсутній.' if lang == 'uk' else 
                         'Описание отсутствует.' if lang == 'ru' else 
                         'No overview available.')
    response = f"📺 <b>{title}</b> ({original_title})\n\n"
    response += f"📅 <b>{'Рік' if lang == 'uk' else 'Год' if lang == 'ru' else 'Year'}:</b> {year}\n"
    response += f"⭐ <b>{'Рейтинг' if lang in ['uk', 'ru'] else 'Rating'}:</b> {rating}/10\n"
    response += f"🎭 <b>{'Жанри' if lang in ['uk', 'ru'] else 'Genres'}:</b> {genres}\n\n"
    response += f"📖 <b>{'Опис' if lang == 'uk' else 'Описание' if lang == 'ru' else 'Overview'}:</b>\n{overview}\n\n"
    return response

def format_similar_movies(movies, lang='uk'):
    """Форматування схожих фільмів"""
    header = {
        'uk': "🎬 Схожі фільми:\n\n",
        'ru': "🎬 Похожие фильмы:\n\n",
        'en': "🎬 Similar movies:\n\n"
    }.get(lang, "")
    items = []
    for movie in movies[:5]:
        title = movie.get('title', 'Невідомо')
        original_title = movie.get('original_title', title)
        year = movie.get('release_date', '')[:4] if movie.get('release_date') else 'Невідомо'
        rating = movie.get('vote_average', 0)
        item = f"🎬 <b>{title}</b> ({original_title})\n"
        item += f"📅 {year} | ⭐ {rating}/10\n"
        item += f"🔗 /movie_{movie['id']}\n"
        items.append(item)
    return header + "\n".join(items)

def format_similar_tv_shows(tv_shows, lang='uk'):
    """Форматування схожих серіалів"""
    header = {
        'uk': "📺 Схожі серіали:\n\n",
        'ru': "📺 Похожие сериалы:\n\n",
        'en': "📺 Similar TV shows:\n\n"
    }.get(lang, "")
    items = []
    for show in tv_shows[:5]:
        title = show.get('name', 'Невідомо')
        original_title = show.get('original_name', title)
        year = show.get('first_air_date', '')[:4] if show.get('first_air_date') else 'Невідомо'
        rating = show.get('vote_average', 0)
        item = f"📺 <b>{title}</b> ({original_title})\n"
        item += f"📅 {year} | ⭐ {rating}/10\n"
        item += f"🔗 /tv_{show['id']}\n"
        items.append(item)
    return header + "\n".join(items)

def format_favorites(favorites, lang):
    """Форматування списку обраного"""
    if not favorites:
        return {
            'uk': "У вас ще немає збережених фільмів.",
            'ru': "У вас еще нет сохранённых фильмов.",
            'en': "You don't have any saved movies yet."
        }.get(lang, "Немає збережених")
    response = {
        'uk': "❤️ Ваше обране:\n\n",
        'ru': "❤️ Ваше избранное:\n\n",
        'en': "❤️ Your favorites:\n\n"
    }.get(lang, "❤️ Ваше обране:\n\n")
    for fav in favorites:
        item_id, item_type, title, original_title, year, rating, poster_path = fav
        if item_type == 'movie':
            response += f"🎬 <b>{title}</b> ({original_title})\n"
        else:
            response += f"📺 <b>{title}</b> ({original_title})\n"
        response += f"📅 {year} | ⭐ {rating}/10\n"
        response += f"🔗 /{item_type}_{item_id}\n\n"
    return response