import logging
from importlib import import_module
from pathlib import Path
from typing import List, Dict, Optional

# Налаштування логування
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class VideoSearch:
    def __init__(self, language: str = 'ru'):
        """Ініціалізація класу VideoSearch з мовою за замовчуванням"""
        self.language = language
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8',
            'Accept-Language': 'ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3'
        }
        self.sources = {}
        self._load_sources()

    def _load_sources(self):
        """Завантаження всіх джерел із папки sources"""
        sources_dir = Path(__file__).parent / "sources"
        for file_path in sources_dir.glob("*.py"):
            if file_path.name.startswith("_"):
                continue
            module_name = file_path.stem
            try:
                module = import_module(f"sources.{module_name}")
                if hasattr(module, "search"):
                    self.sources[module_name] = module.search
                    logger.info(f"Завантажено джерело: {module_name}")
            except Exception as e:
                logger.error(f"Помилка завантаження джерела {module_name}: {e}")

    async def search_all_sources(self, title: str, alternative_titles: Optional[str] = None, year: Optional[str] = None) -> List[Dict]:
        """Пошук по всіх джерелах для основної та альтернативних назв"""
        results = []
        try:
            # Пошук за основною назвою
            for source_name, search_func in self.sources.items():
                source_results = await search_func(title, year=year, headers=self.headers, language=self.language)
                if source_results:
                    results.extend(source_results)
                    logger.info(f"{source_name.capitalize()} знайшов {len(source_results)} результатів для '{title}': {source_results}")
                else:
                    logger.info(f"{source_name.capitalize()} не знайшов результатів для '{title}'")

                # Пошук за альтернативними назвами
                if alternative_titles and not source_results:
                    alt_titles = [alternative_titles] if isinstance(alternative_titles, str) else [t for t in alternative_titles if isinstance(t, str)]
                    for alt_title in alt_titles:
                        alt_results = await search_func(alt_title, year=year, headers=self.headers, language=self.language)
                        if alt_results:
                            results.extend(alt_results)
                            logger.info(f"{source_name.capitalize()} знайшов {len(alt_results)} результатів для альтернативної назви '{alt_title}': {alt_results}")
                            break

            return results[:10]
        except Exception as e:
            logger.error(f"Помилка в search_all_sources: {e}")
            return []

    def add_source(self, source_name: str, search_function):
        """Додавання нового джерела пошуку"""
        self.sources[source_name] = search_function
        logger.info(f"Додано нове джерело: {source_name}")