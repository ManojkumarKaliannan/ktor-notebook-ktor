# Notebook API — Ktor

A personal notebook REST API built with **Ktor** and **Exposed ORM**, demonstrating CRUD operations on a Notes resource. Uses H2 for local development and PostgreSQL (Railway) for production.

---

## Tech Stack

- [Ktor](https://ktor.io/) — web framework (Kotlin)
- [Exposed](https://github.com/JetBrains/Exposed) — ORM (like SQLAlchemy in Python)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) — JSON serialization
- [Netty](https://netty.io/) — ASGI server
- [H2](https://h2database.com/) — in-memory database (local dev)
- [PostgreSQL](https://www.postgresql.org/) — production database (Railway)

---

## Getting Started (Local)

### 1. Clone the repo

```bash
git clone https://github.com/ManojkumarKaliannan/ktor-notebook-ktor.git
cd ktor-notebook-ktor
```

### 2. Run the server

Open in Android Studio / IntelliJ IDEA and click the **Run** button on `Application.kt`.

Or via terminal:

```bash
./gradlew run
```

The API will be available at `http://localhost:8080`.

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | Health check |
| `GET` | `/api/notes` | Get all notes (newest first) |
| `GET` | `/api/notes/{id}` | Get a single note |
| `POST` | `/api/notes` | Create a new note |
| `PUT` | `/api/notes/{id}` | Update a note |
| `DELETE` | `/api/notes/{id}` | Delete a single note |
| `DELETE` | `/api/notes` | Delete multiple notes by IDs |

### Example Requests

```bash
# Get all notes
curl http://localhost:8080/api/notes

# Create a note
curl -X POST http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{"title": "My Topic", "content": "My story..."}'

# Update a note
curl -X PUT http://localhost:8080/api/notes/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Updated Topic", "content": "Updated story..."}'

# Delete multiple notes
curl -X DELETE http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{"ids": [1, 2]}'
```

---

## Project Structure

```
src/main/kotlin/
├── Application.kt    # App entry point
├── NoteSchema.kt     # Note table, data classes, NoteService
├── Databases.kt      # DB connection (H2 local / PostgreSQL production)
├── Routing.kt        # All API endpoints
└── Serialization.kt  # JSON setup
```

---

## Deploying to Railway

### 1. Push to GitHub

```bash
git add .
git commit -m "Initial commit"
git push
```

### 2. Create project on Railway

1. Go to [railway.app](https://railway.app) → **New Project**
2. Select **Deploy from GitHub repo**
3. Select `ktor-notebook-ktor`
4. Railway auto-detects Kotlin/Gradle and builds it

### 3. Add PostgreSQL

1. In your Railway project → **New** → **Database** → **PostgreSQL**
2. Click on the database → **Variables** tab
3. Copy `DATABASE_URL`
4. Go to your app service → **Variables** → add `DATABASE_URL`

### 4. Your API is live

Railway gives you a URL like:
```
https://ktor-notebook-ktor.up.railway.app
```

---

## Database

| Environment | Database | Notes |
|---|---|---|
| Local | H2 (in-memory) | Zero setup, data lost on restart |
| Production | PostgreSQL (Railway) | Permanent storage |

The app automatically picks the right database via the `DATABASE_URL` environment variable.

---

## License

MIT
