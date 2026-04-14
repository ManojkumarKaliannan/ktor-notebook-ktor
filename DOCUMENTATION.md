# Notebook API — Full Documentation (Ktor)

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Tech Stack](#2-tech-stack)
3. [Project Structure](#3-project-structure)
4. [File Breakdown](#4-file-breakdown)
5. [Database Design](#5-database-design)
6. [API Endpoints](#6-api-endpoints)
7. [Request & Response Schemas](#7-request--response-schemas)
8. [How to Run Locally](#8-how-to-run-locally)
9. [How to Test the API](#9-how-to-test-the-api)
10. [Deployment Guide (Railway)](#10-deployment-guide-railway)
11. [Android Integration Guide](#11-android-integration-guide)
12. [Comparison with FastAPI version](#12-comparison-with-fastapi-version)

---

## 1. Project Overview

**ktor-notebook-ktor** is a personal notebook REST API built with Kotlin and Ktor.
Write your topics, stories and thoughts — all stored permanently in PostgreSQL on Railway.

Same API as the FastAPI version but built entirely in Kotlin.

---

## 2. Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Kotlin | 2.x | Programming language |
| Ktor | 3.x | Web framework |
| Exposed | latest | ORM — talks to DB using Kotlin objects |
| kotlinx.serialization | latest | JSON serialization/deserialization |
| Netty | latest | Server engine |
| H2 | latest | In-memory DB for local dev |
| PostgreSQL | latest | Production database |
| Railway | — | Cloud hosting + PostgreSQL |

---

## 3. Project Structure

```
ktor-notebook-ktor/
│
├── src/main/kotlin/
│   ├── Application.kt      → App entry point
│   ├── NoteSchema.kt       → Notes table + data classes + NoteService
│   ├── Databases.kt        → DB connection setup
│   ├── Routing.kt          → All API route handlers
│   └── Serialization.kt    → JSON configuration
│
├── src/main/resources/
│   ├── application.conf    → Server port and module config
│   └── logback.xml         → Logging config
│
├── build.gradle.kts        → Dependencies and build config
├── settings.gradle.kts     → Project name
├── gradle.properties       → Gradle settings
├── .gitignore
├── README.md
└── DOCUMENTATION.md        → This file
```

---

## 4. File Breakdown

### `Application.kt`
Entry point of the app. Calls the three setup functions in order.

```kotlin
fun Application.module() {
    configureSerialization()  // Setup JSON
    configureDatabases()      // Connect to DB
    configureRouting()        // Register routes
}
```

**Equivalent in FastAPI:** `main.py`

---

### `NoteSchema.kt`
Contains three things in one file:

**1. Table definition** (like `models.py`):
```kotlin
object Notes : Table("notes") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val content = text("content").nullable()
    val createdAt = long("created_at")
}
```

**2. Data classes** (like `schemas.py`):
```kotlin
@Serializable
data class Note(val id: Int, val title: String, val content: String?, val createdAt: Long)

@Serializable
data class NoteCreate(val title: String, val content: String? = null)
```

**3. NoteService** (handles all DB operations):
- `getAll()` — fetch all notes
- `getById(id)` — fetch one note
- `create(note)` — insert new note
- `update(id, note)` — update existing note
- `delete(id)` — delete one note
- `deleteBulk(ids)` — delete multiple notes

---

### `Databases.kt`
Connects to the right database based on environment:

```kotlin
val databaseUrl = System.getenv("DATABASE_URL")

val database = if (databaseUrl != null) {
    // Production → PostgreSQL (Railway)
    Database.connect(url = databaseUrl, driver = "org.postgresql.Driver")
} else {
    // Local → H2 in-memory
    Database.connect(url = "jdbc:h2:mem:notesdb;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
}
```

**Equivalent in FastAPI:** `database.py`

---

### `Routing.kt`
Defines all API endpoints.

**Equivalent in FastAPI:** route handlers in `main.py`

---

### `Serialization.kt`
Installs the JSON plugin so the API can send/receive JSON.

```kotlin
install(ContentNegotiation) { json() }
```

---

## 5. Database Design

### Table: `notes`

```
┌────┬──────────────┬───────────────────────┬────────────────┐
│ id │    title     │        content        │   created_at   │
│int │   varchar    │         text          │      long      │
│ PK │  NOT NULL    │       NULLABLE        │  epoch millis  │
├────┼──────────────┼───────────────────────┼────────────────┤
│  1 │ Welcome      │ This is my notebook..│ 1713093600000  │
│  2 │ First Story  │ Today was a great day│ 1713093600001  │
└────┴──────────────┴───────────────────────┴────────────────┘
```

Seeded automatically on first run if the table is empty.

> Note: `created_at` is stored as epoch milliseconds (Long) instead of DateTime for simplicity with H2 and PostgreSQL compatibility.

---

## 6. API Endpoints

### Base URLs
```
Local:      http://localhost:8080
Production: https://your-app.up.railway.app
```

---

### `GET /`
```json
{ "message": "My Notebook API is running!" }
```

---

### `GET /api/notes`
Returns all notes, newest first.

```json
[
  { "id": 2, "title": "First Story", "content": "...", "createdAt": 1713093600001 },
  { "id": 1, "title": "Welcome", "content": "...", "createdAt": 1713093600000 }
]
```

---

### `GET /api/notes/{id}`
Returns one note.

**404 response:**
```json
{ "detail": "Note not found" }
```

---

### `POST /api/notes`
**Request:**
```json
{ "title": "My Topic", "content": "My story..." }
```
**Response `201 Created`:**
```json
{ "id": 3, "title": "My Topic", "content": "My story...", "createdAt": 1713093700000 }
```

---

### `PUT /api/notes/{id}`
**Request:**
```json
{ "title": "Updated Topic", "content": "Updated story..." }
```
**Response `200 OK`:** returns updated note

---

### `DELETE /api/notes/{id}`
**Response `204 No Content`**

---

### `DELETE /api/notes`
**Request:**
```json
{ "ids": [1, 2, 3] }
```
**Response:**
```json
{ "deleted": 3, "ids": [1, 2, 3] }
```

---

## 7. Request & Response Schemas

### NoteCreate (POST / PUT body)
```json
{
  "title": "string (required)",
  "content": "string or null (optional)"
}
```

### Note (Response)
```json
{
  "id": "integer",
  "title": "string",
  "content": "string or null",
  "createdAt": "long (epoch milliseconds)"
}
```

### BulkDeleteRequest
```json
{ "ids": [1, 2, 3] }
```

### BulkDeleteResponse
```json
{ "deleted": 3, "ids": [1, 2, 3] }
```

---

## 8. How to Run Locally

### Prerequisites
- JDK 21
- Android Studio or IntelliJ IDEA

### Steps

```bash
# Clone
git clone https://github.com/ManojkumarKaliannan/ktor-notebook-ktor.git
cd ktor-notebook-ktor

# Run
./gradlew run
```

Or click the **Run** button on `Application.kt` in Android Studio.

Server runs at: `http://localhost:8080`

---

## 9. How to Test the API

### Option 1 — Browser
```
http://localhost:8080/api/notes
```

### Option 2 — Postman
- GET: `http://localhost:8080/api/notes`
- POST: Body → raw → JSON → `{"title": "test", "content": "hello"}`

### Option 3 — curl
```bash
# Get all
curl http://localhost:8080/api/notes

# Create
curl -X POST http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{"title": "My Topic", "content": "My story"}'

# Update
curl -X PUT http://localhost:8080/api/notes/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Updated", "content": "Updated content"}'

# Delete one
curl -X DELETE http://localhost:8080/api/notes/1

# Delete many
curl -X DELETE http://localhost:8080/api/notes \
  -H "Content-Type: application/json" \
  -d '{"ids": [1, 2]}'
```

---

## 10. Deployment Guide (Railway)

### Step 1 — Push to GitHub
```bash
git add .
git commit -m "Initial commit"
git push
```

### Step 2 — Deploy on Railway
1. Go to [railway.app](https://railway.app) → sign up with GitHub
2. **New Project** → **Deploy from GitHub repo**
3. Select `ktor-notebook-ktor`
4. Railway detects Kotlin/Gradle and builds automatically

### Step 3 — Add PostgreSQL
1. In project → **New** → **Database** → **Add PostgreSQL**
2. Click the DB → **Variables** → copy `DATABASE_URL`
3. Go to your app service → **Variables** → add `DATABASE_URL`

### Step 4 — Live
Railway gives you:
```
https://ktor-notebook-ktor.up.railway.app/api/notes
```

---

## 11. Android Integration Guide

```kotlin
// build.gradle.kts (app)
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
```

```kotlin
data class Note(
    val id: Int,
    val title: String,
    val content: String?,
    val createdAt: Long
)

data class NoteCreate(
    val title: String,
    val content: String? = null
)

data class BulkDeleteRequest(val ids: List<Int>)
```

```kotlin
interface NotesApi {
    @GET("api/notes")
    suspend fun getAllNotes(): List<Note>

    @GET("api/notes/{id}")
    suspend fun getNote(@Path("id") id: Int): Note

    @POST("api/notes")
    suspend fun createNote(@Body note: NoteCreate): Note

    @PUT("api/notes/{id}")
    suspend fun updateNote(@Path("id") id: Int, @Body note: NoteCreate): Note

    @DELETE("api/notes/{id}")
    suspend fun deleteNote(@Path("id") id: Int): Response<Unit>

    @DELETE("api/notes")
    suspend fun deleteNotesBulk(@Body request: BulkDeleteRequest): Response<Unit>
}
```

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://your-app.up.railway.app/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api = retrofit.create(NotesApi::class.java)
```

---

## 12. Comparison with FastAPI version

| | FastAPI (Python) | Ktor (Kotlin) |
|---|---|---|
| Framework | FastAPI | Ktor |
| ORM | SQLAlchemy | Exposed |
| Validation | Pydantic | kotlinx.serialization |
| Local DB | SQLite | H2 |
| Production DB | PostgreSQL (Supabase) | PostgreSQL (Railway) |
| Hosting | Render | Railway |
| Swagger UI | Auto built-in | Plugin (optional) |
| Entry point | `main.py` | `Application.kt` |
| Models | `models.py` | `NoteSchema.kt` |
| Schemas | `schemas.py` | data classes in `NoteSchema.kt` |
| DB setup | `database.py` | `Databases.kt` |
| Routes | `main.py` | `Routing.kt` |

---

*Documentation for ktor-notebook-ktor — Notebook API built with Ktor*
