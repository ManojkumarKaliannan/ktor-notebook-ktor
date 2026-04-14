package com.manojka

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

lateinit var noteService: NoteService

fun Application.configureDatabases() {
    val databaseUrl = System.getenv("DATABASE_URL")

    val database = if (databaseUrl != null) {
        // Production — Railway PostgreSQL
        log.info("Connecting to PostgreSQL: $databaseUrl")
        Database.connect(
            url = databaseUrl,
            driver = "org.postgresql.Driver"
        )
    } else {
        // Local — H2 in-memory
        log.info("Using H2 in-memory database for local development")
        Database.connect(
            url = "jdbc:h2:mem:notesdb;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "root",
            password = ""
        )
    }

    noteService = NoteService(database)
}
