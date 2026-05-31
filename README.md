# Lottery Backend (Hackathon)

MVP бэкенда лотерейных тиражей без Spring: REST API, PostgreSQL, слоистая архитектура.

## Архитектура

- **api** — HTTP-обработчики и DTO (`LotteryHttpHandler`)
- **service** — бизнес-логика (`DrawService`, `TicketService`)
- **repository** — JDBC-доступ к PostgreSQL
- **domain** — сущности и статусы
- **db** — пул соединений и инициализация схемы

## Модель данных

| Сущность | Статусы | Поля |
|----------|---------|------|
| Draw | `ACTIVE`, `COMPLETED` | выигрышная комбинация (6 чисел 1–49), даты |
| Ticket | `PENDING`, `WIN`, `LOSE` | комбинация пользователя, связь с тиражом |

Комбинация — 6 уникальных чисел от 1 до 49. Билет выигрывает при полном совпадении с выигрышной комбинацией (порядок не важен).

## API

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/draws` | Создать тираж |
| GET | `/draws` | Список активных тиражей |
| POST | `/draws/{id}/tickets` | Купить билет |
| POST | `/draws/{id}/complete` | Завершить тираж |
| GET | `/tickets/{id}` | Результат билета |

### Примеры

```bash
# Создать тираж
curl -X POST http://localhost:8080/draws

# Активные тиражи
curl http://localhost:8080/draws

# Купить билет
curl -X POST http://localhost:8080/draws/1/tickets \
  -H "Content-Type: application/json" \
  -d '{"numbers":[3,11,22,33,41,49]}'

# Завершить тираж
curl -X POST http://localhost:8080/draws/1/complete

# Проверить билет
curl http://localhost:8080/tickets/1
```

## Запуск локально

### Требования

- Java 17+
- Maven 3.9+
- PostgreSQL (по умолчанию: `localhost:5432`, БД `postgres`, user/password `postgres` / `123321`)

### Сборка и тесты

```bash
cd lottery
mvn clean test
mvn package
java -jar target/lottery-1.0-SNAPSHOT.jar
```

Переменные окружения (опционально): `SERVER_PORT`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

### Docker Compose

PostgreSQL + API + React UI в одной команде:

```bash
cd lottery
docker compose up --build
```

Откройте **http://localhost:8088** (или задайте `WEB_PORT=8080`, если порт свободен) — интерфейс; запросы к API идут через `/api` (nginx → `lottery-api`).

Сервисы:

| Сервис | Образ / сборка | Назначение |
|--------|----------------|------------|
| `postgres` | `postgres:latest` | База данных |
| `lottery-api` | `Dockerfile` (Java) | REST API :8080 (внутри сети) |
| `lottery-web` | `frontend/Dockerfile` (nginx) | UI + прокси API, порт **8080** снаружи |

Остановка: `docker compose down` (данные БД в volume `lottery_pg_data`).

Только API без UI:

```bash
docker compose up --build postgres lottery-api
```

Порт API наружу (для отладки) — раскомментируйте `ports: "8081:8080"` у `lottery-api` в `docker-compose.yml`.

## Frontend (React)

```bash
cd frontend
npm install
npm run dev
```

UI: http://localhost:5173 (прокси API на `http://localhost:8080` через `/api`).

Прямой URL API без прокси: `VITE_API_URL=http://localhost:8080 npm run dev`.

## Сценарий «Базовая лотерея»

1. Создать тираж → статус `ACTIVE`.
2. Получить список активных тиражей.
3. Купить билет → статус `PENDING`.
4. Завершить тираж → генерируется выигрышная комбинация, тираж `COMPLETED`, билеты → `WIN` / `LOSE`.
5. Проверить билет по `GET /tickets/{id}`.
