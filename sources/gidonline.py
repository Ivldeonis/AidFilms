import aiohttp
from bs4 import BeautifulSoup
from urllib.parse import urljoin
from typing import List, Dict, Optional

async def search(title: str, year: Optional[str] = None, headers: Dict = None, language: str = 'ru') -> List[Dict]:
    """Пошук відео на GidOnline з фільтрацією за роком"""
    base_url = "https://io.gidonline.fun"
    try:
        search_url = f"{base_url}/search/"
        data = {"search": title}
        async with aiohttp.ClientSession() as session:
            async with session.post(search_url, data=data, headers=headers, timeout=15) as response:
                response.raise_for_status()
                soup = BeautifulSoup(await response.text(), 'html.parser')
                results = []
                for item in soup.find_all('div', class_='b-content__inline_item'):
                    title_tag = item.find('div', class_='b-content__inline_item-link').find('a')
                    if not title_tag:
                        continue
                    item_title = title_tag.text.strip()
                    relative_url = title_tag['href']
                    film_url = urljoin(base_url, relative_url)
                    
                    # Парсимо рік із блоку <div class="misc">
                    misc_tag = item.find('div', class_='misc')
                    item_year = None
                    if misc_tag and misc_tag.text:
                        # Витягуємо рік (перші 4 цифри)
                        import re
                        year_match = re.search(r'\b(\d{4})\b', misc_tag.text)
                        if year_match:
                            item_year = year_match.group(1)
                    
                    # Фільтруємо за роком, якщо вказано
                    if year and item_year and item_year != year:
                        continue
                    
                    player_urls = await _get_gidonline_players(film_url, headers)
                    for player_url in player_urls:
                        results.append({
                            'title': item_title,
                            'url': player_url['url'] if player_url else film_url,
                            'source': 'GidOnline',
                            'player_name': player_url['name'] if player_url else 'Основний плеєр',
                            'year': item_year
                        })
                return results[:5]
    except Exception as e:
        print(f"Помилка пошуку на GidOnline: {e}")
        return []

async def _get_gidonline_players(url: str, headers: Dict) -> List[Dict]:
    """Отримання URL усіх плеєрів для GidOnline"""
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(url, headers=headers, timeout=15) as response:
                response.raise_for_status()
                soup = BeautifulSoup(await response.text(), 'html.parser')
                players = []
                
                # Знаходимо кнопки плеєрів
                buttons_div = soup.find('div', style='margin-left: 12px;')
                if buttons_div:
                    buttons = buttons_div.find_all('button', class_='button1')
                    for button in buttons:
                        button_text = button.text.strip()
                        # Витягуємо виклик функції onclick
                        onclick = button.get('onclick', '')
                        if 'myFunction' in onclick:
                            # Знаходимо відповідний iframe
                            iframe_id = onclick.replace('myFunction', '').replace('()', '').replace('1', '').replace('2', '')
                            iframe = soup.find('iframe', id=iframe_id) if iframe_id else soup.find('iframe')
                            player_url = iframe['src'] if iframe and iframe.get('src') else None
                            if player_url:
                                players.append({
                                    'name': button_text,
                                    'url': player_url
                                })
                
                # Якщо кнопок немає, додаємо основний iframe
                if not players:
                    iframe = soup.find('iframe')
                    player_url = iframe['src'] if iframe and iframe.get('src') else None
                    if player_url:
                        players.append({
                            'name': 'Основний плеєр',
                            'url': player_url
                        })
                
                print(f"Player URLs для {url}: {[p['url'] for p in players]}")
                return players
    except Exception as e:
        print(f"Помилка в _get_gidonline_players для {url}: {e}")
        return []