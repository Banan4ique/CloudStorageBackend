# Cloud Storage Service

REST-сервис для управления файлами с авторизацией и интеграцией с фронтендом.

## Функциональность

- ✅ Авторизация пользователей
- ✅ Загрузка файлов
- ✅ Скачивание файлов
- ✅ Переименование файлов
- ✅ Удаление файлов
- ✅ Просмотр списка файлов
- ✅ Интеграция с фронтенд-приложением

## Технологии

- Java 22
- Spring Boot 3.3
- Spring Security
- PostgreSQL
- Docker & Docker Compose
- Testcontainers
- Liquibase

## Быстрый старт

### Требования
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- Node.js (для фронтенда)

### Запуск

1. **Сборка:**
```bash
docker-compose up -d
```
2. **Запуск [фронтенда](https://github.com/Banan4ique/CloudStorageFrontend)**
```bash
# В директории фронта
npm install
npm run serve
```

## API Документация

### Аутентификация
- POST /login –– вход в систему
- POST /logout –– выход из системы

### Действия с файлами
- POST /file?filename={имя_файла} –– загрузка файла
- GET /file?filename={имя_файла} –– скачивание файла
- PUT /file?filename={имя_файла} –– переименование файла
- DELETE /file?filename={имя_файла} –– удаление файла
- GET /list?limit={имя_файла} –– список файлов

## Тестовые данные
    1. login: user1
       password: password123

    2. login: user2
       password: password456