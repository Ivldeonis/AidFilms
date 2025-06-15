import sqlite3
from sqlite3 import Error
import threading

class Database:
    _instance = None
    _lock = threading.Lock()
    
    def __new__(cls, db_file):
        if cls._instance is None:
            with cls._lock:
                if cls._instance is None:
                    cls._instance = super(Database, cls).__new__(cls)
                    cls._instance._initialized = False
        return cls._instance
    
    def __init__(self, db_file):
        if self._initialized:
            return
        self.db_file = db_file
        self.local = threading.local()
        self._initialized = True
    
    def get_connection(self):
        if not hasattr(self.local, 'conn'):
            self.local.conn = sqlite3.connect(self.db_file, check_same_thread=False)
            self.create_table()
        return self.local.conn
    
    def create_table(self):
        """Створення таблиці"""
        sql = """
        CREATE TABLE IF NOT EXISTS favorites (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            item_type TEXT NOT NULL,
            item_id INTEGER NOT NULL,
            title TEXT NOT NULL,
            original_title TEXT,
            year TEXT,
            rating REAL,
            poster_path TEXT,
            UNIQUE(user_id, item_id) ON CONFLICT REPLACE
        );
        """
        try:
            conn = self.get_connection()
            c = conn.cursor()
            c.execute(sql)
            conn.commit()
        except Error as e:
            print(f"Помилка створення таблиці: {e}")

    def add_favorite(self, user_id, item_type, item_id, title, original_title, year, rating, poster_path):
        """Додавання в обране"""
        sql = """INSERT INTO favorites(user_id, item_type, item_id, title, original_title, year, rating, poster_path)
                 VALUES(?,?,?,?,?,?,?,?)"""
        try:
            conn = self.get_connection()
            c = conn.cursor()
            c.execute(sql, (user_id, item_type, item_id, title, original_title, year, rating, poster_path))
            conn.commit()
            return True
        except Error as e:
            print(f"Помилка додавання в обране: {e}")
            return False

    def get_favorites(self, user_id):
        """Отримання списку обраного"""
        sql = """SELECT item_id, item_type, title, original_title, year, rating, poster_path 
                 FROM favorites WHERE user_id=?"""
        try:
            conn = self.get_connection()
            c = conn.cursor()
            c.execute(sql, (user_id,))
            return c.fetchall()
        except Error as e:
            print(f"Помилка отримання обраного: {e}")
            return []

    def remove_favorite(self, user_id, item_id):
        """Видалення з обраного"""
        sql = """DELETE FROM favorites WHERE user_id=? AND item_id=?"""
        try:
            conn = self.get_connection()
            c = conn.cursor()
            c.execute(sql, (user_id, item_id))
            conn.commit()
            return c.rowcount > 0
        except Error as e:
            print(f"Помилка видалення з обраного: {e}")
            return False

    def is_favorite(self, user_id, item_id):
        """Перевірка чи є в обраному"""
        sql = """SELECT 1 FROM favorites WHERE user_id=? AND item_id=?"""
        try:
            conn = self.get_connection()
            c = conn.cursor()
            c.execute(sql, (user_id, item_id))
            return c.fetchone() is not None
        except Error as e:
            print(f"Помилка перевірки обраного: {e}")
            return False