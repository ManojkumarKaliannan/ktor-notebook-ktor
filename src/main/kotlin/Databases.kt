package com.manojka

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

lateinit var noteService: NoteService

fun Application.configureDatabases() {
    val rawUrl = System.getenv("DATABASE_URL")

    val database = if (rawUrl != null) {
        // Manually parse URL to handle special characters like $ in password
        // Format: postgresql://user:password@host:port/dbname
        val withoutScheme = rawUrl
            .removePrefix("postgresql://")
            .removePrefix("postgres://")

        // Split at last @ to separate credentials from host
        val atIndex = withoutScheme.lastIndexOf('@')
        val credentials = withoutScheme.substring(0, atIndex)
        val hostPart = withoutScheme.substring(atIndex + 1)

        // Split credentials at first : to get user and password
        val colonIndex = credentials.indexOf(':')
        val user = credentials.substring(0, colonIndex)
        val password = credentials.substring(colonIndex + 1)

        // Split host part to get host, port, dbname
        val hostAndPort = hostPart.substringBefore("/")
        val dbName = hostPart.substringAfter("/")
        val host = hostAndPort.substringBefore(":")
        val port = hostAndPort.substringAfter(":", "5432")

        val jdbcUrl = "jdbc:postgresql://$host:$port/$dbName"

        log.info("Connecting to PostgreSQL at $host:$port/$dbName as $user")
        Database.connect(
            url = jdbcUrl,
            driver = "org.postgresql.Driver",
            user = user,
            password = password
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
