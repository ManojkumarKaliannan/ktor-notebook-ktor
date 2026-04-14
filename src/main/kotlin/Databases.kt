package com.manojka

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

lateinit var noteService: NoteService

fun Application.configureDatabases() {
    val rawUrl = System.getenv("DATABASE_URL")

    val database = if (rawUrl != null) {
        // Convert postgres:// or postgresql:// → jdbc:postgresql://
        val jdbcUrl = when {
            rawUrl.startsWith("jdbc:") -> rawUrl
            rawUrl.startsWith("postgres://") -> rawUrl.replace("postgres://", "jdbc:postgresql://")
            rawUrl.startsWith("postgresql://") -> rawUrl.replace("postgresql://", "jdbc:postgresql://")
            else -> rawUrl
        }
        log.info("Connecting to PostgreSQL")
        Database.connect(
            url = jdbcUrl,
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
