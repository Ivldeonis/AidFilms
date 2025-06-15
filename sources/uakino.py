import aiohttp
from bs4 import BeautifulSoup
from urllib.parse import urljoin
from typing import List, Dict, Optional
import re

DEFAULT_LANGUAGE = 'uk'  # UAKino використовує українську мову

async def search(title: str, year: Optional[str] = None, headers: Dict = None, language: str = DEFAULT_LANGUAGE) -> List[Dict]:
    """Пошук відео на UAKino українською з фільтрацією за роком"""
    base_url = "https://uakino.me"
    try:
        search_url = f"{base_url}/search/"
        data = {"do": "search", "subaction": "search", "story": title}
        async with aiohttp.ClientSession() as session:
            async with session.post(search_url, data=data, headers=headers, timeout=15) as response:
                response.raise_for_status()
                html = await response.text()
                print(f"UAKino search response for '{title}': {html[:500]}")
                soup = BeautifulSoup(html, 'html.parser')
                results = []
                for item in soup.find_all('div', class_='movie-item'):
                    title_tag = item.find('a', class_='movie-title')
                    if not title_tag:
                        continue
                    item_title = title_tag.text.strip()
                    relative_url = title_tag.get('href')
                    if not relative_url or '/news/' in relative_url or '/franchise/' in relative_url:
                        continue
                    film_url = urljoin(base_url, relative_url)

                    # Парсимо рік із URL або мета-тегів
                    item_year = None
                    year_match = re.search(r'\((\d{4})\)', item_title)
                    if year_match:
                        item_year = year_match.group(1)
                    elif '/cartoon/features/' in film_url:
                        async with session.get(film_url, headers=headers) as film_response:
                            film_html = await film_response.text()
                            film_soup = BeautifulSoup(film_html, 'html.parser')
                            meta_year = film_soup.find('meta', property='og:title')
                            if meta_year and meta_year.get('content'):
                                year_match = re.search(r'\((\d{4})\)', meta_year['content'])
                                if year_match:
                                    item_year = year_match.group(1)

                    # Фільтруємо за роком
                    if year and item_year and item_year != year:
                        continue

                    # Перевіряємо часткове співпадіння назви
                    if title.lower() not in item_title.lower() and not any(word.lower() in item_title.lower() for word in title.split()):
                        continue

                    # Витягуємо iframe
                    player_url = await _get_uakino_player(film_url, headers)
                    results.append({
                        'title': item_title,
                        'url': player_url or film_url,
                        'source': 'UAKino',
                        'player_name': 'Основний плеєр',
                        'year': item_year
                    })
                print(f"UAKino found {len(results)} results for '{title}': {results}")
                return results[:1]  # Один результат
    except Exception as e:
        print(f"Помилка пошуку на UAKino: {e}")
        return []

async def _get_uakino_player(url: str, headers: Dict) -> Optional[str]:
    """Отримання iframe-плеєра з UAKino"""
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(url, headers=headers, timeout=15) as response:
                response.raise_for_status()
                html = await response.text()
                print(f"UAKino player page HTML for {url}: {html[:500]}")
                soup = BeautifulSoup(html, 'html.parser')
                iframe = soup.find('iframe')
                if iframe and iframe.get('src'):
                    return iframe['src']
                return None
    except Exception as e:
        print(f"Помилка в _get_uakino_player для {url}: {e}")
        return None