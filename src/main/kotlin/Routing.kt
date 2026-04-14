package com.manojka

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {

        // Health check
        get("/") {
            call.respond(mapOf("message" to "My Notebook API is running!"))
        }

        // GET all notes
        get("/api/notes") {
            call.respond(noteService.getAll())
        }

        // GET single note
        get("/api/notes/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("detail" to "Invalid ID"))
            val note = noteService.getById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("detail" to "Note not found"))
            call.respond(note)
        }

        // POST create note
        post("/api/notes") {
            val noteCreate = call.receive<NoteCreate>()
            val note = noteService.create(noteCreate)
            call.respond(HttpStatusCode.Created, note)
        }

        // PUT update note
        put("/api/notes/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("detail" to "Invalid ID"))
            val noteCreate = call.receive<NoteCreate>()
            val updated = noteService.update(id, noteCreate)
                ?: return@put call.respond(HttpStatusCode.NotFound, mapOf("detail" to "Note not found"))
            call.respond(updated)
        }

        // DELETE single note
        delete("/api/notes/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("detail" to "Invalid ID"))
            val deleted = noteService.delete(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("detail" to "Note not found"))
            }
        }

        // DELETE bulk
        delete("/api/notes") {
            val request = call.receive<BulkDeleteRequest>()
            val count = noteService.deleteBulk(request.ids)
            call.respond(BulkDeleteResponse(deleted = count, ids = request.ids))
        }
    }
}
