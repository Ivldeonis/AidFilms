import aiohttp

class TMDbClient:
    def __init__(self, bearer_token):
        """Ініціалізація клієнта TMDb з токеном авторизації"""
        self.base_url = "https://api.themoviedb.org/3"
        self.headers = {
            "Authorization": f"Bearer {bearer_token}",
            "accept": "application/json"
        }

    async def get_top_movies(self, language='uk'):
        """Отримання списку топ фільмів"""
        url = f"{self.base_url}/movie/top_rated"
        params = {
            "language": language,
            "page": 1
        }
        async with aiohttp.ClientSession() as session:
            async with session.get(url, headers=self.headers, params=params) as response:
                if response.status == 200:
                    data = await response.json()
                    return data.get('results', [])
                return None

    async def get_top_tv_shows(self, language='uk'):
        """Отримання списку топ серіалів"""
        url = f"{self.base_url}/tv/top_rated"
        params = {
            "language": language,
            "page": 1
        }
        async with aiohttp.ClientSession() as session:
            async with session.get(url, headers=self.headers, params=params) as response:
                if response.status == 200:
                    data = await response.json()
                    return data.get('results', [])
                return None

    async def search_multi(self, query, language='uk'):
        """Пошук фільмів і серіалів за запитом"""
        url = f"{self.base_url}/search/multi"
        params = {
            "query": query,
            "language": language,
            "page": 1
        }
        async with aiohttp.ClientSession() as session:
            async with session.get(url, headers=self.headers, params=params) as response:
                if response.status == 200:
                    data = await response.json()
                    return data.get('results', [])
                return None

    async def get_movie_details(self, movie_id, language='uk'):
        """Отримання деталей фільму за ID"""
        url = f"{self.base_url}/movie/{movie_id}"
        params = {
            "language": language,
            "append_to_response": "credits,similar"
        }
        async with aiohttp.ClientSession() as session:
            async with session.get(url, headers=self.headers, params=params) as response:
                if response.status == 200:
                    return await response.json()
                return None

    async def get_tv_details(self, tv_id, language='uk'):
        """Отримання деталей серіалу за ID"""
        url = f"{self.base_url}/tv/{tv_id}"
        params = {
            "language": language,
            "append_to_response": "credits,similar"
        }
        async with aiohttp.ClientSession() as session:
            async with session.get(url, headers=self.headers, params=params) as response:
                if response.status == 200:
                    return await response.json()
                return None

    async def get_similar_movies(self, movie_id, language='uk'):
        """Отримання схожих фільмів за ID"""
        url = f"{self.base_url}/movie/{movie_id}/similar"
        params = {
            "language": language,
            "page": 1
        }
        async with aiohttp.ClientSession() as session:
            async with session.get(url, headers=self.headers, params=params) as response:
                if response.status == 200:
                    data = await response.json()
                    return data.get('results', [])
                return None

    async def get_similar_tv_shows(self, tv_id, language='uk'):
        """Отримання схожих серіалів за ID"""
        url = f"{self.base_url}/tv/{tv_id}/similar"
        params = {
            "language": language,
            "page": 1
        }
        async with aiohttp.ClientSession() as session:
            async with session.get(url, headers=self.headers, params=params) as response:
                if response.status == 200:
                    data = await response.json()
                    return data.get('results', [])
                return None

    def get_poster_url(self, poster_path, size='w500'):
        """Формування URL для постера"""
        if not poster_path:
            return None
        return f"https://image.tmdb.org/t/p/{size}{poster_path}"